package br.com.automacaowebia.service;

import br.com.automacaowebia.config.AppProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ImpressaoZPLService {

    private static final Logger logger = LogManager.getLogger(ImpressaoZPLService.class);

    private static final String ZPL_SERVER_URL = AppProperties.getInstance().get("zpl.server.url");

    /**
     * Gera uma imagem do ZPL e salva no arquivo indicado.
     *
     * @param zplContent Conteúdo do ZPL.
     * @param outputPath Caminho para salvar a imagem (Ex: etiqueta.png).
     * @return true se sucesso, false se erro.
     */
    public boolean gerarImagemZPL(String zplContent, String outputPath) {
        try {
            logger.info("Enviando ZPL para gerar imagem.");

            URL url = new URL(ZPL_SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setDoOutput(true);

            // Envia o conteúdo do ZPL
            try (var os = conn.getOutputStream()) {
                byte[] input = zplContent.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            logger.info("Response code: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Salva a imagem
                try (InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(outputPath)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                logger.info("Imagem salva em {}", outputPath);
                return true;
            } else {
                logger.error("Erro na geração da imagem. Código: {}", responseCode);
                return false;
            }

        } catch (Exception e) {
            logger.error("Erro ao gerar imagem ZPL", e);
            return false;
        }
    }

    /**
     * Simula a impressão (neste exemplo apenas gera a imagem, você pode adaptar para enviar direto para a impressora).
     */
    public boolean imprimir(String zplContent) {
        return gerarImagemZPL(zplContent, "etiqueta.png");
    }
}
