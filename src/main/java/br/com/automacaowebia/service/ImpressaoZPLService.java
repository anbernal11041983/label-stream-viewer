package br.com.automacaowebia.service;

import br.com.automacaowebia.config.AppProperties;
import br.com.automacaowebia.printer.ZebraSocketSender;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;

public class ImpressaoZPLService {

    private static final Logger logger = LogManager.getLogger(ImpressaoZPLService.class);

    private static final String ZPL_SERVER_URL = AppProperties.getInstance().get("zpl.server.url");
    private static final Map<String, ZebraSocketSender> pool = new ConcurrentHashMap<>();

    private static final int DPMM = 8;        // 203 dpi  -> 8 dpmm
    private static final int ROTATION = 0;
    private final HistoricoImpressaoService historicoImpressaoService = new HistoricoImpressaoService();

    public String personalizarZpl(String zplBruto, String sku) {
        logger.info("Iniciando personalização do ZPL.");

        if (zplBruto == null || zplBruto.isBlank()) {
            logger.error("O template ZPL está vazio ou nulo.");
            throw new IllegalArgumentException("O template ZPL não pode ser vazio.");
        }

        if (sku == null || sku.isBlank()) {
            logger.error("O SKU está vazio ou nulo.");
            throw new IllegalArgumentException("O SKU não pode ser vazio.");
        }

        logger.debug("Substituindo {{SKU}} por '{}'", sku);
        String zplFinal = zplBruto.replace("{{SKU}}", sku);

        logger.info("Personalização do ZPL concluída.");
        return zplFinal;
    }

    public Image gerarPreview(String zpl,
            double larguraMm,
            double alturaMm,
            String unidade, // “cm”, “mm” ou “in”
            String formato) {      // “png”, “pdf”, …

        // 1. Converta sempre para MILÍMETROS
        double wMm, hMm;
        switch (unidade.toLowerCase()) {
            case "cm" -> {
                wMm = larguraMm * 10;
                hMm = alturaMm * 10;
            }
            case "in" -> {
                wMm = larguraMm * 25.4;
                hMm = alturaMm * 25.4;
            }
            default -> {
                wMm = larguraMm;
                hMm = alturaMm;
            }   // já está em mm
        }

        // 2. Monte a URL usando mm
        String url = String.format(Locale.ENGLISH,
                "%s?dpmm=%d&width=%.2f&height=%.2f&rotation=%d&format=%s",
                ZPL_SERVER_URL, DPMM, wMm, hMm, ROTATION, formato);

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/plain; charset=US-ASCII");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(zpl.getBytes(StandardCharsets.US_ASCII));
            }

            if (con.getResponseCode() == 200) {
                return new Image(con.getInputStream());
            }
            logger.error("Labelary respondeu {}", con.getResponseCode());
        } catch (Exception e) {
            logger.error("Erro no preview", e);
        }
        return null;
    }

    public void imprimirNaZebra(String zpl,
            String printerIp,
            boolean salvarNoFlash,
            String modelo,
            String sku,
            int qtd) {

        String payload = preparePayload(zpl, qtd, salvarNoFlash);

        boolean ok = false;
        try {

            ZebraSocketSender sender = pool.computeIfAbsent(printerIp, ip -> {
                try {
                    String porta = "9100";
                    return new ZebraSocketSender(ip, Integer.parseInt(porta));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            sender.send(payload);
            ok = true;
            logger.info("ZPL enviado para {} ({} bytes)", printerIp, payload.length());

        } catch (Exception e) {
            logger.error("Falha ao imprimir na impressora {}", printerIp, e);
        }

        /* grava histórico somente se OK */
        if (ok) {
            historicoImpressaoService.salvarHistorico(modelo, sku, qtd, printerIp);
        }
    }

    private String preparePayload(String zpl, int copies, boolean salvarNoFlash) {
        // 1) remove ^PQ existente (evita cópias duplas)
        zpl = zpl.replaceAll("(?i)\\^PQ[0-9,]*", "");
        // 2) injeta nova quantidade antes do ^XZ final
        int idx = zpl.lastIndexOf("^XZ");
        String withQty = zpl.substring(0, idx)
                + "^PQ" + copies + "\n"
                + zpl.substring(idx);

        // 3) flash ou direto
        return salvarNoFlash ? "^XA^XF" + withQty + "^XZ"
                : withQty;
    }

}
