package br.com.automacaowebia.printer;

import br.com.automacaowebia.model.Printer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ContinuousPrinter implements Runnable {

    private final Printer printer;
    private final String template;
    private final int espacamentoMs;
    private final Map<String, String> vars;
    private final Consumer<String> cb;
    private static final Logger log = LogManager.getLogger(ContinuousPrinter.class); // ⭐

    private static final byte STX1 = 0x02;
    private static final byte STX2 = 0x05;
    private static final byte ETX = 0x03;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;

    private volatile Integer qtyAlvo = null;

    public ContinuousPrinter(Printer printer,
            String template,
            int espacamentoMs,
            Map<String, String> vars,
            Consumer<String> cb) {
        this.printer = printer;
        this.template = template;
        this.espacamentoMs = espacamentoMs;
        this.vars = vars;
        this.cb = cb;
    }

    public ContinuousPrinter(Printer printer,
            String template,
            int espacamentoMs,
            Map<String, String> vars,
            Consumer<String> cb,
            Integer qty) {
        this(printer, template, espacamentoMs, vars, cb);
        this.qtyAlvo = (qty != null && qty > 0) ? qty : null;
    }

    public synchronized void start() { // ⭐
        start(null);
    }

    /**
     * Start com quantidade → para sozinho ao atingir qty
     */
    public synchronized void start(Integer qty) {
        if (running.get()) {
            return;
        }
        this.qtyAlvo = (qty != null && qty > 0) ? qty : null;
        running.set(true);
        worker = new Thread(this, "laser-job");
        worker.start();
    }

    /**
     * Acionado pelo botão “Stop” — para sempre (mesmo com quantidade)
     */
    public synchronized void stop() {
        running.set(false);
        if (worker != null) {
            worker.interrupt();
        }
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            /* 1) valida template */
            List<String> files = fetchFileList(out, in, cb);
            if (files.stream().noneMatch(f -> f.equalsIgnoreCase(template))) {
                throw new IOException("Template '" + template + "' não encontrado");
            }

            /* 2) SETA com variáveis */
            String cmdSeta = montarSetaComVariaveis(vars);
            sendAndReturn(out, in, "clearbuf", "clearbuf");
            sendAndReturn(out, in, "load:" + template, "load");
            if (!sendAndReturn(out, in, cmdSeta, "seta").contains("seta:1")) {
                throw new IOException("SETA rejeitado");
            }
            sendAndReturn(out, in, "start:", "start");

            /* 3) Loop contínuo ou por quantidade (dinâmico) */
            int contador = 0;
            while (running.get() && (qtyAlvo == null || contador < qtyAlvo)) {
                contador++;

                long t0 = System.currentTimeMillis();
                if (cb != null) {
                    cb.accept("➡ Início da marcação #" + contador);
                }

                sendAndReturn(out, in, "trimark", "trimark");

                /* espera terminar a marcação */
                while (getSysStatus(out, in).isMarking()) { // ⭐ () no accessor
                    Thread.sleep(100);
                }
                long duracao = System.currentTimeMillis() - t0;
                long delay = espacamentoMs - duracao;
                if (delay > 0) {
                    Thread.sleep(delay);
                }

                if (cb != null) {
                    cb.accept(String.format(
                            "✅ Marca #%d concluída – gravação %d ms | ciclo %d ms",
                            contador, duracao, System.currentTimeMillis() - t0));
                }

                /* checa erros/avisos entre ciclos */
                SysStatus st = getSysStatus(out, in);
                if (st.err() > 0) { // ⭐ accessor
                    sendAndReturn(out, in, "stop:", "stop");
                    throw new IOException("Erro da impressora (err=" + st.err() + ')');
                }
            }

            // Se saiu por atingir a quantidade, informa
            if (qtyAlvo != null && cb != null) { // ⭐
                cb.accept("✅ Lote de " + qtyAlvo + " peças concluído");
            }

            /* 4) Sempre enviar STOP ao sair do loop */
            sendAndReturn(out, in, "stop:", "stop");
            if (cb != null) {
                cb.accept("🛑 Impressão interrompida");
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            if (cb != null) {
                cb.accept("🛑 Thread interrompida");
            }
        } catch (IOException ioe) {
            if (cb != null) {
                cb.accept("⛔ Falha: " + ioe.getMessage());
            }
        } finally {
            running.set(false); // ⭐ garante estado consistente ao terminar
        }
    }

    private static record SysStatus(boolean isMarking, int warn, int err) {

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

    private static String montarSetaComVariaveis(Map<String, String> vars) {
        return "seta:data#"
                + vars.entrySet().stream()
                        .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";"));
    }

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

}
