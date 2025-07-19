package br.com.automacaowebia.controller;

import br.com.automacaowebia.config.AppInfo;
import br.com.automacaowebia.model.Dispositivo;
import br.com.automacaowebia.model.User;
import br.com.automacaowebia.service.LicencaService;
import br.com.automacaowebia.service.LoginService;
import br.com.automacaowebia.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LoginController implements Initializable {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @FXML
    private Label c_logo;

    @FXML
    private Label f_logo;

    @FXML
    private Button login_btn;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private AnchorPane login_form;
    @FXML
    private Label lblVersao;

    @FXML
    private CheckBox chkRemember;

    private double x;
    private double y;

    private static final String PREF_NODE = "br.com.automacaowebia.login";
    private static final String PREF_USER = "rememberUser";
    private static final String PREF_PASS = "rememberPass";

    private final Preferences prefs = Preferences.userRoot().node(PREF_NODE);
    private final LoginService loginService = new LoginService();

    public void textfieldDesign() {
        if (username.isFocused()) {
            username.setStyle("-fx-background-color:#fff;" + "-fx-border-width:2px");
            password.setStyle("-fx-background-color:transparent;" + "-fx-border-width:1px");
        } else if (password.isFocused()) {
            username.setStyle("-fx-background-color:transparent;" + "-fx-border-width:1px");
            password.setStyle("-fx-background-color:#fff;" + "-fx-border-width:2px");
        }
    }

    public void dropShowAffect() {
        DropShadow original = new DropShadow(20, Color.valueOf("#ae44a5"));
        f_logo.setEffect(original);
        //c_logo.setEffect(original);
    }

    public void onExit() {
        System.exit(0);
    }

    public void login() {

        /* ---------- validações simples ---------- */
        String userInput = username.getText();
        String passwordInput = password.getText();

        if (userInput.isEmpty() || passwordInput.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Usuário e senha obrigatórios.").showAndWait();
            return;
        }

        /* ---------- autenticação ---------- */
        User tentativa = new User();
        tentativa.setUsername(userInput);
        tentativa.setPassword(passwordInput);

        User authenticatedUser = loginService.autenticar(tentativa);

        if (authenticatedUser == null) {
            new Alert(Alert.AlertType.ERROR, "Usuário ou senha inválidos!").showAndWait();
            return;
        }

        /* ---------- grava na sessão ---------- */
        Session sess = Session.getInstance();
        sess.setUser(authenticatedUser);

        // Dispositivo & fingerprint vieram do App.start()
        Dispositivo disp = sess.getDispositivoAtual();

        boolean bloqueado = disp != null && "BLOQUEADO".equalsIgnoreCase(disp.getStatus());
        boolean admin = "ADMIN".equalsIgnoreCase(authenticatedUser.getPerfil());

        /* ---------- regras de dispositivo ---------- */
        if (bloqueado && !admin) {             // operador → nega acesso
            new Alert(Alert.AlertType.ERROR,
                    "Dispositivo bloqueado.\nEntre em contato com o administrador.")
                    .showAndWait();
            return;
        }

        /*  Se BLOQUEADO + ADMIN: apenas avisa e continua.
    O admin poderá desbloquear pelo módulo de Dispositivos.                 */
        if (bloqueado && admin) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Dispositivo bloqueado. Desbloqueie no menu Dispositivos.")
                    .showAndWait();
        }

        if (chkRemember.isSelected()) {
            prefs.put(PREF_USER, userInput);
            prefs.put(PREF_PASS, passwordInput);   
        } else {
            prefs.remove(PREF_USER);
            prefs.remove(PREF_PASS);
        }

        abrirDashboard();
    }

    private void abrirDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/br/com/automacaowebia/dashboard.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();

            root.setOnMousePressed(e -> {
                x = e.getSceneX();
                y = e.getSceneY();
            });
            root.setOnMouseDragged(e -> {
                stage.setX(e.getScreenX() - x);
                stage.setY(e.getScreenY() - y);
            });

            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.show();

            // fecha tela de login
            login_btn.getScene().getWindow().hide();

            logger.info("Usuário logado: {} | Perfil: {}",
                    Session.getInstance().getUser().getNome(),
                    Session.getInstance().getUser().getPerfil());

        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard.fxml", e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dropShowAffect();
        lblVersao.setText("Versão: " + AppInfo.getVersion());

        String savedUser = prefs.get(PREF_USER, "");
        String savedPass = prefs.get(PREF_PASS, "");
        if (!savedUser.isEmpty() && !savedPass.isEmpty()) {
            username.setText(savedUser);
            password.setText(savedPass);
            chkRemember.setSelected(true);
        }
    }
}
