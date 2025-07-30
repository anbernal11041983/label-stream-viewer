package br.com.automacaowebia.printer;

import br.com.automacaowebia.model.Printer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class LaserDriver {

    private static final Logger log = LogManager.getLogger(LaserDriver.class);

    private static final byte STX1 = 0x02;
    private static final byte STX2 = 0x05;
    private static final byte ETX = 0x03;

    /* ============================================================
     *  Tipos auxiliares
     * ============================================================ */
    /**
     * Snapshot do status retornado por sys_sta:errsta
     */
    private static record SysStatus(boolean isMarking, int warn, int err) {

    }

    /* ============================================================
     *  API pública original (mantida)
     * ============================================================ */
    public void testPrint(Printer printer, String text) throws IOException {
        final String REQUIRED_TEMPLATE = "HELLO.ncfm";
        log.info("Iniciando teste na impressora {} ({}:{})",
                printer.getNome(), printer.getIp(), printer.getPorta());

        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            List<String> files = fetchFileList(out, in, null);
            log.info("Templates disponíveis: {}", files);

            if (files.stream().noneMatch(f -> f.equalsIgnoreCase(REQUIRED_TEMPLATE))) {
                throw new IOException("Template requerido '" + REQUIRED_TEMPLATE + "' NÃO encontrado");
            }

            sendAndReturn(out, in, "load:" + REQUIRED_TEMPLATE, "load");
            sendAndReturn(out, in, "seta:data#v1=" + text + "+pos#0|0|0|1", "seta");
            sendAndReturn(out, in, "start:", "start");
            log.info("Impressão concluída com template '{}'", REQUIRED_TEMPLATE);
        }
    }

    public void print(Printer printer, String template, String text, Consumer<String> cb) throws IOException {
        log.info("Conectando em {} ({}:{})", printer.getNome(), printer.getIp(), printer.getPorta());
        if (cb != null) {
            cb.accept("▶ Conectando em " + printer.getIp() + ":" + printer.getPorta());
        }

        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            List<String> files = fetchFileList(out, in, cb);
            if (files.stream().noneMatch(f -> f.equalsIgnoreCase(template))) {
                throw new IOException("Template '" + template + "' não encontrado");
            }

            sendAndReturn(out, in, "load:" + template, "load");
            sendAndReturn(out, in, "seta:data#v1=" + text + "+pos#0|0|0|1", "seta");
            sendAndReturn(out, in, "start:", "start");
            if (cb != null) {
                cb.accept("✅ Impressão concluída");
            }
        }
    }

    /**
     * Imprime um lote (qty) com atraso mínimo “espacamento” (ms) entre ciclos.
     *
     * @param printer impressora/laser
     * @param template arquivo .ncfm já gravado na controladora
     * @param qty quantidade de peças
     * @param cb callback para UI (TextArea) – pode ser null
     * @param espacamento ciclo mínimo desejado em ms (duração real + delay
     * extra)
     * @param vars mapa chave→valor para substituir no SETA (v1, v2 …)
     */
    public void printBatch(Printer printer,
            String template,
            int qty,
            Consumer<String> cb,
            int espacamento,
            Map<String, String> vars) throws IOException {

        if (cb != null) {
            cb.accept("▶ Conectando em " + printer.getIp() + ":" + printer.getPorta());
        }

        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {
            socket.setTcpNoDelay(true);
            OutputStream out = socket.getOutputStream();
            InputStream in = new BufferedInputStream(socket.getInputStream());

            /* ---------- 1) valida template ---------- */
            List<String> files = fetchFileList(out, in, cb);
            if (files.stream().noneMatch(f -> f.equalsIgnoreCase(template))) {
                throw new IOException("Template '" + template + "' não encontrado");
            }

            /* ---------- 2) SETA com variáveis ---------- */
            String cmdSeta = montarSetaComVariaveis(vars);
            if (cb != null) {
                cb.accept(">> Comando SETA: " + cmdSeta);
            }

            /* ---------- 3) sequência clear ▸ load ▸ seta ▸ start ---------- */
            sendAndReturn(out, in, "clearbuf", "clearbuf");
            sendAndReturn(out, in, "load:" + template, "load");
            String ack = sendAndReturn(out, in, cmdSeta, "seta");
            if (!ack.contains("seta:1")) {
                throw new IOException("SETA rejeitado: " + ack);
            }
            sendAndReturn(out, in, "start:", "start");

            /* ---------- 4) trimark repetitivo + espaçamento ---------- */
            for (int i = 1; i <= qty; i++) {

                /* ① DISPARO */
                long t0 = System.currentTimeMillis();
                String ini = String.format("➡ Início da marcação #%d", i);
                log.info(ini);
                if (cb != null) {
                    cb.accept(ini);
                }

                sendAndReturn(out, in, "trimark", "trimark");

                /* ② AGUARDA laser concluir */
                while (getSysStatus(out, in).isMarking) {
                    Thread.sleep(100);          // polling a cada 100 ms
                }
                long duracao = System.currentTimeMillis() - t0;

                /* ③ APLICA DELAY até completar “espacamento” */
                long delayExtra = espacamento - duracao;
                if (delayExtra > 0) {
                    log.debug("⏳ Delay extra {} ms", delayExtra);
                    if (cb != null) {
                        cb.accept("⏳ Delay " + delayExtra + " ms");
                    }
                    Thread.sleep(delayExtra);
                }

                /* ④ LOG DE FIM */
                long cicloTotal = System.currentTimeMillis() - t0;
                String fim = String.format(
                        "✅ Marca #%d concluída – gravação %4d ms | ciclo %4d ms",
                        i, duracao, cicloTotal);
                log.info(fim);
                if (cb != null) {
                    cb.accept(fim);
                }
            }

            /* ---------- 5) monitoramento global ---------- */
            int lastCount = -1, lastWarn = 0;
            while (true) {
                Thread.sleep(200);                       // polling
                int countNow = getCount(out, in);
                SysStatus st = getSysStatus(out, in);

                if (countNow != lastCount) {
                    if (cb != null) {
                        cb.accept("📦 " + countNow + "/" + qty);
                    }
                    lastCount = countNow;
                }
                if (st.warn > lastWarn) {
                    log.warn("⚠ Avisos: {}", st.warn);
                    if (cb != null) {
                        cb.accept("⚠ Avisos: " + st.warn);
                    }
                    lastWarn = st.warn;
                }
                if (st.err > 0) {
                    log.error("⛔ Erro (err={}) – stop", st.err);
                    sendAndReturn(out, in, "stop:", "stop");
                    throw new IOException("Erro da impressora (err=" + st.err + ")");
                }
                if (countNow >= qty) {
                    break;              // lote finalizado
                }
            }

            if (cb != null) {
                cb.accept("✅ Lote concluído (" + qty + " peças)");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread interrompida", ie);
        }
    }

    private static String montarSetaComVariaveis(Map<String, String> vars) {
        StringBuilder sb = new StringBuilder("seta:data#");

        vars.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                sb.append(k).append('=').append(v).append(";");
            }
        });

        // remove o último "; " se existir
        int len = sb.length();
        if (len > 0 && sb.charAt(len - 1) == ';') {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    private static String sendAndReturn(OutputStream out, InputStream in,
            String payload, String etapa) throws IOException {

        byte[] frame = frame(payload);
        log.debug(">> [{}] {}", etapa, toHex(frame));
        out.write(frame);
        out.flush();

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            buf.write(b);
            if (b == ETX) {           // ETX encontrado
                int xor = in.read();  // ← lê sempre 1 byte  (checksum)
                if (xor != -1) {
                    buf.write(xor);
                }
                break;
            }
        }

        byte[] resp = buf.toByteArray();
        log.debug("<< [{}] {}", etapa, toHex(resp));

        /* ---------- SUCESSO ---------- */
        if (resp.length >= 2 && resp[0] == STX1 && resp[1] == 0x06) {
            // acha a posição real do ETX (0x03)
            int etxIdx = -1;
            for (int i = 2; i < resp.length; i++) {
                if (resp[i] == ETX) {        // ETX encontrado
                    etxIdx = i;
                    break;
                }
            }
            if (etxIdx == -1) {              // segurança: se ETX não encontrado
                etxIdx = resp.length - 1;    // usa último byte como limite
            }
            // extrai somente o payload ASCII (entre ACK e ETX)
            return new String(resp, 2, etxIdx - 2, StandardCharsets.US_ASCII);
        }
        /* ---------- FALHA: pega motivo em ASCII para log ---------- */
        String asciiDetail = (resp.length >= 4)
                ? new String(resp, 2, resp.length - 3, StandardCharsets.US_ASCII) // só payload
                : "(payload vazio)";

        String msg = "ACK ausente em " + etapa
                + " – quadro: " + toHex(resp)
                + " – detalhe ASCII: " + asciiDetail;

        log.error(msg);              // já fica registrado no log
        throw new IOException(msg);  // propaga p/ sua UI
    }

    private static void setLimit(OutputStream out, InputStream in,
            int qty, Consumer<String> cb) throws IOException {
        sendAndReturn(out, in, "setlimitcount:" + qty, "setlimitcount");
        if (cb != null) {
            cb.accept("✔ Lote configurado: " + qty);
        }
    }

    private static int getCount(OutputStream out, InputStream in) throws IOException {
        String resp = sendAndReturn(out, in, "getcount:", "getcount");
        int idx = resp.indexOf("getcount:");
        if (idx == -1) {
            return -1;
        }
        String num = resp.substring(idx + "getcount:".length())
                 .replace(";", "")      // ← elimina o ; caso exista
                 .trim();
        return Integer.parseInt(num.trim());
    }

    private static SysStatus getSysStatus(OutputStream out, InputStream in) throws IOException {
        String resp = sendAndReturn(out, in, "sys_sta:errsta", "sys_sta");
        int idx = resp.indexOf("sys_sta:");
        if (idx == -1) {
            return new SysStatus(false, 0, 0);
        }

        String[] parts = resp.substring(idx + "sys_sta:".length(), resp.length() - 1)
                .split(",");
        boolean marking = false;
        int warn = 0, err = 0;
        for (String p : parts) {
            String[] kv = p.split("=");
            if (kv.length != 2) {
                continue;
            }
            switch (kv[0]) {
                case "ismark" ->
                    marking = "1".equals(kv[1]);
                case "warn" ->
                    warn = Integer.parseInt(kv[1]);
                case "err" ->
                    err = Integer.parseInt(kv[1]);
            }
        }
        return new SysStatus(marking, warn, err);
    }

    /* ============================================================
     *  Métodos reutilizados do driver original
     * ============================================================ */
    private static List<String> fetchFileList(OutputStream out, InputStream in,
            Consumer<String> cb) throws IOException {
        String etapa = "get_filelist";
        String resp = sendAndReturn(out, in, "get_filelist", etapa);
        if (cb != null) {
            cb.accept("<< [" + etapa + "] " + resp);
        }

        int idx = resp.indexOf("get_filelist:");
        if (idx == -1) {
            return List.of();
        }
        String list = resp.substring(idx + "get_filelist:".length(), resp.length() - 1);
        return Arrays.stream(list.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<String> fetchFileList(OutputStream out, InputStream in) throws IOException {
        return fetchFileList(out, in, null);
    }

    /* ============================================================
     *  Helpers de framing e logging
     * ============================================================ */
    private static byte[] frame(String payload) {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.US_ASCII);

        // monta 02 05 <payload> 03
        byte[] body = new byte[payloadBytes.length + 3];
        body[0] = STX1;                // 0x02
        body[1] = STX2;                // 0x05
        System.arraycopy(payloadBytes, 0, body, 2, payloadBytes.length);
        body[body.length - 1] = ETX;   // 0x03

        // XOR de tudo entre 0x02 e 0x03 (índices 1 .. body.length‑2)
        byte xor = 0;
        for (int i = 1; i < body.length - 1; i++) {
            xor ^= body[i];
        }

        // devolve array body + XOR
        byte[] frame = Arrays.copyOf(body, body.length + 1);
        frame[frame.length - 1] = xor;
        return frame;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public void printHex(String comando, Consumer<String> cb) {
        byte[] frame = frame(comando);
        String hex = toHex(frame);
        if (cb != null) {
            cb.accept("🟩 HEX gerado para o comando '" + comando + "':");
            cb.accept(hex);
        }
        log.info("🟩 HEX gerado para '{}': {}", comando, hex);
    }
}
