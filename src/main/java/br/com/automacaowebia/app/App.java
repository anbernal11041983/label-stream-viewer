package br.com.automacaowebia.app;

import br.com.automacaowebia.model.Dispositivo;
import br.com.automacaowebia.service.LicencaService;
import br.com.automacaowebia.util.MacAddressUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class App extends Application {

    private static final Logger logger = LogManager.getLogger(App.class);

    private double x;
    private double y;

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Iniciando aplicação...");

        // 🔗 Verificação de licença
        LicencaService licencaService = new LicencaService();
        String mac = MacAddressUtil.getMacAddress();

        logger.info("MAC Address detectado: {}", mac);

        Dispositivo dispositivo = licencaService.buscarDispositivo(mac);

        if (dispositivo == null) {
            licencaService.cadastrarDispositivo(mac);
            showAlertAndExit("Dispositivo não registrado.\nEntre em contato para ativação.");
            return;
        } else if ("BLOQUEADO".equalsIgnoreCase(dispositivo.getStatus())) {
            showAlertAndExit("Dispositivo bloqueado.\nEntre em contato para ativação.");
            return;
        } else if ("ATIVADO".equalsIgnoreCase(dispositivo.getStatus())) {
            logger.info("Dispositivo autorizado. Carregando sistema...");
            carregarTelaLogin(stage);
        } else {
            showAlertAndExit("Status inválido. Contate o suporte.");
            return;
        }
    }

    private void carregarTelaLogin(Stage stage) throws IOException {
        logger.info("Carregando tela de login...");
        Parent root = FXMLLoader.load(getClass().getResource("/br/com/automacaowebia/login-view.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Label Stream Viewer");

        // Habilita arrastar a janela sem barra
        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
        });

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        logger.info("Tela de login carregada com sucesso.");
    }

    private void showAlertAndExit(String message) {
        logger.warn(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Licenciamento");
        alert.setHeaderText("Ativação necessária");
        alert.setContentText(message);
        alert.showAndWait();
        logger.warn("Encerrando aplicação devido à restrição de licença.");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
