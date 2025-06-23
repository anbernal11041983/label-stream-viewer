package br.com.automacaowebia.service;

import br.com.automacaowebia.config.AppProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javafx.scene.image.Image;

public class ImpressaoZPLService {

    private static final Logger logger = LogManager.getLogger(ImpressaoZPLService.class);

    private static final String ZPL_SERVER_URL = AppProperties.getInstance().get("zpl.server.url");

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

    public Image gerarPreview(String zplFinal, double largura, double altura, String unidade, String formatoSaida) {
        logger.info("Iniciando geração do preview da etiqueta.");

        try {
            // Conversão de unidades (cm, mm, inches) para polegadas
            double wIn, hIn;
            switch (unidade.toLowerCase()) {
                case "cm":
                    wIn = largura / 2.54;
                    hIn = altura / 2.54;
                    break;
                case "mm":
                    wIn = largura / 25.4;
                    hIn = altura / 25.4;
                    break;
                default:     // "inches"
                    wIn = largura;
                    hIn = altura;
                    break;
            }

            // Monta a URL com os parâmetros, incluindo formato dinâmico
            String urlStr = String.format(Locale.ENGLISH,
                    "%s?dpmm=%d&width=%.2f&height=%.2f&rotation=%d&format=%s",
                    ZPL_SERVER_URL, 8, wIn, hIn, 0, formatoSaida);

            logger.info("Montando URL para requisição: {}", urlStr);

            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain"); // ✅ Correto
            connection.setDoOutput(true);

            // Envia o ZPL
            try (var output = connection.getOutputStream()) {
                output.write(zplFinal.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            logger.info("Response code do servidor de preview: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                Image image = new Image(inputStream);
                logger.info("Preview da etiqueta gerado com sucesso.");
                return image;
            } else {
                logger.error("Falha ao gerar o preview da etiqueta. Response code: {}", responseCode);
                return null;
            }

        } catch (Exception e) {
            logger.error("Erro ao gerar preview da etiqueta ZPL.", e);
            return null;
        }
    }

    public void imprimirNaZebra(String zpl, String value, boolean selected) {
        System.out.println("Simulando a Impressora"+value);
    }

}
