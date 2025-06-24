package br.com.automacaowebia.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.NetworkInterface;
import java.util.Collections;

public class MacAddressUtil {

    private static final Logger logger = LogManager.getLogger(MacAddressUtil.class);

    public static String getMacAddress() {
        try {
            logger.info("Varredura de interfaces de rede para obter MAC Address…");

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni == null) continue;

                byte[] macBytes = ni.getHardwareAddress();     // pode ser null
                String macStr = (macBytes == null) ? "null" :
                        bytesToHex(macBytes);

                logger.debug("Interface: {}  | up={} loopback={} virtual={}  | MAC={}",
                             ni.getDisplayName(), ni.isUp(), ni.isLoopback(), ni.isVirtual(), macStr);

                // Critério: primeira interface ligada e com MAC real
                if (ni.isUp() && !ni.isLoopback() && macBytes != null && macBytes.length > 0) {
                    logger.info("MAC Address selecionado: {}", macStr);
                    return macStr;
                }
            }

            logger.warn("Nenhum endereço MAC válido encontrado depois da varredura.");
            return "UNKNOWN";

        } catch (Exception e) {
            logger.error("Erro ao tentar obter o MAC Address: {}", e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    private static String bytesToHex(byte[] macBytes) {
        StringBuilder mac = new StringBuilder();
        for (byte b : macBytes) mac.append(String.format("%02X:", b));
        if (mac.length() > 0) mac.deleteCharAt(mac.length() - 1);
        return mac.toString();
    }
}
