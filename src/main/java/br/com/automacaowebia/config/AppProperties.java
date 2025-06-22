package br.com.automacaowebia.config;

import java.io.InputStream;
import java.util.Properties;

public class AppProperties {

    private static final String CONFIG_FILE = "application.properties";
    private static AppProperties instance = new AppProperties();
    private Properties properties;

    private AppProperties() {
        properties = new Properties();
        try {
            InputStream input = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("Arquivo " + CONFIG_FILE + " não encontrado no classpath.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao carregar o arquivo " + CONFIG_FILE, e);
        }
    }

    public static AppProperties getInstance() {
        return instance;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
