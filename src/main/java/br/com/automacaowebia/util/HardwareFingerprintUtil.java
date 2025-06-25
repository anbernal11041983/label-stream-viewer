package br.com.automacaowebia.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class HardwareFingerprintUtil {
    private static final Logger logger = LogManager.getLogger(HardwareFingerprintUtil.class);

    /** Consulta o MAC Address do adaptador físico da placa-mãe */
    public static String getMotherboardMac() {
        return runWmicQuery(
                "nic where \"AdapterTypeId=0 and PhysicalAdapter=true and PNPDeviceID like 'PCI%' and MACAddress is not null\" get MACAddress /value",
                "MACAddress"
        );
    }

    /** Consulta o número de série da placa-mãe */
    public static String getBaseboardSerial() {
        return runWmicQuery("baseboard get SerialNumber /value", "SerialNumber");
    }

    /** Gera fingerprint SHA-256 = SHA(mac|serial) em hexadecimal */
    public static String getHardwareFingerprint() {
        String mac = getMotherboardMac();
        String serial = getBaseboardSerial();
        String raw = mac + "|" + serial;

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(raw.getBytes(StandardCharsets.UTF_8));
            return IntStream.range(0, hash.length)
                    .mapToObj(i -> String.format("%02X", hash[i]))
                    .collect(Collectors.joining());
        } catch (Exception e) {
            logger.error("Falha ao gerar fingerprint", e);
            return raw; // fallback
        }
    }

    /** Executa comando WMIC e extrai o campo desejado */
    private static String runWmicQuery(String query, String fieldName) {
        try {
            Process process = new ProcessBuilder("cmd.exe", "/c", "wmic " + query)
                    .redirectErrorStream(true)
                    .start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            for (String line : output.split("\\R")) {
                line = line.trim();
                if (line.startsWith(fieldName + "=")) {
                    String value = line.substring(line.indexOf('=') + 1).trim().toUpperCase(Locale.ROOT);
                    logger.info("{} encontrado: {}", fieldName, value);
                    return value;
                }
            }
        } catch (IOException e) {
            logger.error("Erro ao executar comando WMIC para {}", fieldName, e);
        }

        logger.warn("{} não encontrado via WMIC.", fieldName);
        return "UNKNOWN";
    }
}
