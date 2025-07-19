package br.com.automacaowebia.printer;

import br.com.automacaowebia.model.Printer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
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

    /* ============================================================
     *  NOVO – impressão em lote com monitoramento
     * ============================================================ */
    public void printBatch(Printer printer,
            String template,
            String textoV1,
            int qty,
            Consumer<String> cb,
            int espacamento) throws IOException {

        if (cb != null) {
            cb.accept("▶ Conectando em " + printer.getIp() + ":" + printer.getPorta());
        }

        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // 1) valida template
            List<String> files = fetchFileList(out, in, cb);
            if (files.stream().noneMatch(f -> f.equalsIgnoreCase(template))) {
                throw new IOException("Template '" + template + "' não encontrado");
            }

            // 2) monta SETA
            String cmdSeta = montarSetaComVariaveis(
                    textoV1,
                    "Data de Fabricacao:",
                    "Data de Validade:",
                    "01/01/2025 [d]",
                    "01/06/2025 [d]",
                    "9994567890123");

            if (cb != null) {
                cb.accept(">> Comando SETA: " + cmdSeta);
            }

            // 3) sequência clear ▸ load ▸ seta ▸ start
            sendAndReturn(out, in, "clearbuf", "clearbuf");
            sendAndReturn(out, in, "load:" + template, "load");
            String ack = sendAndReturn(out, in, cmdSeta, "seta");
            if (!ack.contains("seta:1")) {
                throw new IOException("SETA rejeitado: " + ack);
            }

            sendAndReturn(out, in, "start:", "start");

            // 4) trimark N vezes com espaçamento
            for (int i = 1; i <= qty; i++) {
                sendAndReturn(out, in, "trimark", "trimark");
                if (cb != null) {
                    cb.accept("🛈 Disparo software #" + i);
                }

                while (getSysStatus(out, in).isMarking) {
                    try {
                        Thread.sleep(espacamento);  // espera concluir a marcação atual
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Thread interrompida durante espaçamento", ex);
                    }
                }
            }

            // 5) loop de monitoramento
            int lastCount = -1, lastWarn = 0;
            while (true) {
                try {
                    Thread.sleep(200); // polling rápido
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Thread interrompida durante monitoramento", ex);
                }

                int countNow = getCount(out, in);
                SysStatus stat = getSysStatus(out, in);

                if (countNow != lastCount) {
                    if (cb != null) {
                        cb.accept("📦 " + countNow + "/" + qty);
                    }
                    lastCount = countNow;
                }

                if (stat.warn > lastWarn) {
                    log.warn("⚠ Avisos: {}", stat.warn);
                    if (cb != null) {
                        cb.accept("⚠ Avisos: " + stat.warn);
                    }
                    lastWarn = stat.warn;
                }

                if (stat.err > 0) {
                    log.error("⛔ Erro (err={}): stop", stat.err);
                    sendAndReturn(out, in, "stop:", "stop");
                    throw new IOException("Erro da impressora (err=" + stat.err + ")");
                }

                if (countNow >= qty) {
                    break; // lote concluído
                }
            }

            if (cb != null) {
                cb.accept("✅ Lote concluído (" + qty + " peças)");
            }

        }
    }

    /* ============================================================
     *  Funções utilitárias (novas ou aprimoradas)
     * ============================================================ */
    private static String montarSetaComVariaveis(
            String t1, String t2, String t3,
            String t4, String t5, String b1) {

        StringBuilder sb = new StringBuilder("seta:data#");

        if (t1 != null && !t1.isEmpty()) {
            sb.append("T1=").append(t1).append("; ");
        }
        if (t2 != null && !t2.isEmpty()) {
            sb.append("T2=").append(t2).append("; ");
        }
        if (t3 != null && !t3.isEmpty()) {
            sb.append("T3=").append(t3).append("; ");
        }
        if (t4 != null && !t4.isEmpty()) {
            sb.append("T4=").append(t4).append("; ");
        }
        if (t5 != null && !t5.isEmpty()) {
            sb.append("T5=").append(t5).append("; ");
        }
        if (b1 != null && !b1.isEmpty()) {
            sb.append("B1=").append(b1).append("; ");
        }

        // Remove último "; " se existir
        int len = sb.length();
        if (len >= 2 && sb.substring(len - 2).equals("; ")) {
            sb.delete(len - 2, len);
        }

        //sb.append("+pos#0|0|0|1");
        return sb.toString();
    }

    private static String sendAndReturn(OutputStream out, InputStream in,
            String payload, String etapa) throws IOException {

        byte[] frame = frame(payload);
        log.debug(">> [{}] {}", etapa, toHex(frame));
        out.write(frame);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            buf.write(b);
            if (b == ETX) {
                break;
            }
        }
        byte[] resp = buf.toByteArray();
        log.debug("<< [{}] {}", etapa, toHex(resp));

        if (resp.length < 2 || resp[0] != STX1 || resp[1] != 0x06) {
            throw new IOException("ACK ausente em " + etapa + " – resp=" + toHex(resp));
        }
        return new String(resp, StandardCharsets.US_ASCII);
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
        String num = resp.substring(idx + "getcount:".length(), resp.length() - 1);
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
        byte[] data = payload.getBytes(StandardCharsets.US_ASCII);
        byte xor = 0;
        for (byte b : data) {
            xor ^= b;
        }
        return ByteBuffer.allocate(data.length + 4)
                .put(STX1).put(STX2)
                .put(data)
                .put(ETX)
                .put(xor)
                .array();
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
