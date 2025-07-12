package br.com.automacaowebia.printer;

import br.com.automacaowebia.model.Printer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Driver responsável por enviar quadros STX/ETX ao laser Codiline (JCZ) e
 * validar o ACK (0x02 0x06 … 0x03).
 */
public final class LaserDriver {

    private static final Logger log = LogManager.getLogger(LaserDriver.class);

    private static final byte STX1 = 0x02;
    private static final byte STX2 = 0x05;
    private static final byte ETX = 0x03;

    /* =========================================================================
     *  API pública
     * ========================================================================= */
    /**
     * Lista os templates, procura por HELLO.ncfm e, se encontrar, executa load
     * ▸ seta ▸ start. Caso contrário lança IOException.
     */
    public void testPrint(Printer printer, String text) throws IOException {

        final String REQUIRED_TEMPLATE = "HELLO.ncfm";

        log.info("Iniciando teste na impressora {} ({}:{})",
                printer.getNome(), printer.getIp(), printer.getPorta());

        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            /* --- 1) lista templates --- */
            List<String> files = fetchFileList(out, in);
            log.info("Templates disponíveis: {}", files);

            /* --- 2) verifica se HELLO.ncfm existe --- */
            if (!files.stream().anyMatch(f -> f.equalsIgnoreCase(REQUIRED_TEMPLATE))) {
                String msg = "Template requerido '" + REQUIRED_TEMPLATE
                        + "' NÃO encontrado na controladora";
                log.error(msg);
                throw new IOException(msg);
            }
            log.info("Template '{}' encontrado – prosseguindo.", REQUIRED_TEMPLATE);

            /* --- 3) load ▸ seta ▸ start --- */
            send(out, in, "load:" + REQUIRED_TEMPLATE, "load");

            String cmd = "seta:data#v1=" + text + "+pos#0|0|0|1"; // pos#0 aplica a todos
            send(out, in, cmd, "seta");
            send(out, in, "start:", "start");

            log.info("Impressão concluída usando template '{}' em {}:{}",
                    REQUIRED_TEMPLATE, printer.getIp(), printer.getPorta());
        }
    }

    public void print(Printer printer, String template, String text, Consumer<String> logCallback) throws IOException {

        log.info("Iniciando conexão com a impressora {} ({}:{})",
                printer.getNome(), printer.getIp(), printer.getPorta());
        if (logCallback != null) {
            logCallback.accept("▶ Iniciando conexão com a impressora" + printer.getNome()
                    + " (" + printer.getIp() + ":" + printer.getPorta() + ")");
        }

        try (Socket socket = new Socket(printer.getIp(), printer.getPorta())) {

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            /* --- 1) lista templates --- */
            List<String> files = fetchFileList(out, in);
            log.info("Templates disponíveis: {}", files);
            if (logCallback != null) {
                logCallback.accept("✔ Templates disponíveis: " + files);
            }

            /* --- 2) verifica se template existe --- */
            if (!files.stream().anyMatch(f -> f.equalsIgnoreCase(template))) {
                String msg = "Template requerido '" + template + "' NÃO encontrado na controladora";
                log.error(msg);
                throw new IOException(msg);
            }
            log.info("Template '{}' encontrado – prosseguindo.", template);

            /* --- 3) load ▸ seta ▸ start --- */
            send(out, in, "load:" + template, "load");

            String cmd = "seta:data#v1=" + text + "+pos#0|0|0|1"; // pos#0 aplica a todos
            send(out, in, cmd, "seta");

            send(out, in, "start:", "start");

            log.info("Impressão concluída usando template '{}' em {}:{}",
                    template, printer.getIp(), printer.getPorta());
            if (logCallback != null) {
                logCallback.accept("✅ Impressão concluída usando template '" + template + "' em " + printer.getIp() + ":" + printer.getPorta());
            }
        }
    }

    private static List<String> fetchFileList(OutputStream out, InputStream in, Consumer<String> logCallback) throws IOException {
        byte[] frame = frame("get_filelist");
        log.debug(">> [get_filelist] {}", toHex(frame));
        if (logCallback != null) {
            logCallback.accept(">> [get_filelist] " + toHex(frame));
        }
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
        log.debug("<< [get_filelist] {}", toHex(resp));
        if (logCallback != null) {
            logCallback.accept("<< [get_filelist] " + toHex(resp));
        }

        String ascii = new String(resp, StandardCharsets.US_ASCII);
        int idx = ascii.indexOf("get_filelist:");
        if (idx == -1) {
            return List.of();        // nada encontrado
        }
        String listPart = ascii.substring(idx + "get_filelist:".length(),
                ascii.length() - 1);        // remove ETX
        return Arrays.stream(listPart.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
    
        private static List<String> fetchFileList(OutputStream out, InputStream in) throws IOException {
        byte[] frame = frame("get_filelist");
        log.debug(">> [get_filelist] {}", toHex(frame));

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
        log.debug("<< [get_filelist] {}", toHex(resp));

        String ascii = new String(resp, StandardCharsets.US_ASCII);
        int idx = ascii.indexOf("get_filelist:");
        if (idx == -1) {
            return List.of();        // nada encontrado
        }
        String listPart = ascii.substring(idx + "get_filelist:".length(),
                ascii.length() - 1);        // remove ETX
        return Arrays.stream(listPart.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static void send(OutputStream out, InputStream in,
            String payload, String etapa) throws IOException {

        byte[] frame = frame(payload);
        log.debug(">> [{}] {}", etapa, toHex(frame));
        out.write(frame);
        readAck(in, etapa);
    }

    /**
     * Monta <STX1><STX2>payload<ETX><checksum>
     */
    private static byte[] frame(String payload) {
        byte[] data = payload.getBytes(StandardCharsets.US_ASCII);
        byte xor = 0;
        for (byte b : data) {
            xor ^= b;
        }

        return ByteBuffer.allocate(data.length + 4)
                .put(STX1).put(STX2).put(data).put(ETX).put(xor)
                .array();
    }

    /**
     * Lê resposta até ETX e verifica se começou por 0x02 0x06
     */
    private static void readAck(InputStream in, String etapa) throws IOException {
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

        if (resp.length < 2 || resp[0] != 0x02 || resp[1] != 0x06) {
            IOException ex = new IOException("Sem ACK em " + etapa
                    + ", resposta=" + toHex(resp));
            log.error(ex.getMessage());
            throw ex;
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
