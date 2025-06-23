package br.com.automacaowebia.service;

import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZplCacheService {

    private static final Logger logger = LogManager.getLogger(ZplCacheService.class);

    public static void salvarZpl(String chave, String zpl) {
        logger.info("Salvando ZPL no cache para chave: {}", chave);
        CacheService.put(chave + "_ZPL", zpl);
    }

    public static String getZpl(String chave) {
        logger.info("Buscando ZPL no cache para chave: {}", chave);
        return (String) CacheService.get(chave + "_ZPL");
    }

    public static void salvarPreview(String chave, Image image) {
        logger.info("Salvando Preview no cache para chave: {}", chave);
        CacheService.put(chave + "_IMG", image);
    }

    public static Image getPreview(String chave) {
        logger.info("Buscando Preview no cache para chave: {}", chave);
        return (Image) CacheService.get(chave + "_IMG");
    }

    public static void limpar(String chave) {
        logger.info("Removendo cache para chave: {}", chave);
        CacheService.remove(chave + "_ZPL");
        CacheService.remove(chave + "_IMG");
    }

    public static void clear() {
        logger.info("Limpando todo o cache.");
        CacheService.clear();
    }
}
