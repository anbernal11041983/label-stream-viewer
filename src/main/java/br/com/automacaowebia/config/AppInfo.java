package br.com.automacaowebia.config;


public class AppInfo {

    public static String getVersion() {
        try {
            var props = new java.util.Properties();
            props.load(AppInfo.class.getResourceAsStream(
                    "/META-INF/maven/br.com.automacaowebia/label-stream-viewer/pom.properties"));
            return props.getProperty("version", "desconhecida");
        } catch (Exception e) {
            return "dev";
        }
    }
}
