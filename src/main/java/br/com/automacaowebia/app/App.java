package br.com.automacaowebia.app;

import br.com.automacaowebia.model.Dispositivo;
import br.com.automacaowebia.service.LicencaService;
import br.com.automacaowebia.session.Session;
import br.com.automacaowebia.util.HardwareFingerprintUtil;
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

        if (!testarConexaoComBanco()) {
            showAlertAndExit("Não foi possível conectar ao banco de dados.\nVerifique se o banco está online.");
            return;
        }

        LicencaService licencaService = new LicencaService();
        String fingerprint = HardwareFingerprintUtil.getHardwareFingerprint();
        logger.info("Fingerprint detectado: {}", fingerprint);

        Dispositivo dispositivo = licencaService.buscarDispositivo(fingerprint);

        // ↴  Se não existir, cadastra e continua para login (status BLOQUEADO)
        if (dispositivo == null) {
            licencaService.cadastrarDispositivo(fingerprint);
            dispositivo = licencaService.buscarDispositivo(fingerprint);
        }

        /*  ------------  Salva no Session para usar depois  ------------ */
        Session.getInstance().setDispositivoAtual(dispositivo);
        Session.getInstance().setFingerprintAtual(fingerprint);

        /* Sempre carrega a tela de login a partir daqui */
        carregarTelaLogin(stage);
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
        alert.setTitle("Alerta");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(0);
    }

    private boolean testarConexaoComBanco() {
        try (var conn = br.com.automacaowebia.config.Database.getInstance().connectDB()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.error("Erro ao testar conexão com o banco de dados.", e);
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
