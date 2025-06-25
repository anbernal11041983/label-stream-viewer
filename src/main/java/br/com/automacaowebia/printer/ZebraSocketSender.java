package br.com.automacaowebia.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class ZebraSocketSender implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(ZebraSocketSender.class);
    
    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int SO_TIMEOUT_MS = 5_000;

    private final Socket socket;
    private final OutputStream out;

    public ZebraSocketSender(String ip, int port) throws IOException {
        socket = new Socket();
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(SO_TIMEOUT_MS);       
        socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS);
        out = socket.getOutputStream();
        log.info("Conexão aberta com a impressora {}", ip);
    }

    public synchronized void send(String zpl) throws IOException {
        out.write(zpl.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignore) {
        }
    }
}
