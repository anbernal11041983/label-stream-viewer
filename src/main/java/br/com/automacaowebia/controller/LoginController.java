package br.com.automacaowebia.controller;

import br.com.automacaowebia.model.User;
import br.com.automacaowebia.service.LoginService;
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

    private double x;
    private double y;

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
        String userInput = username.getText();
        String passwordInput = password.getText();

        if (userInput.isEmpty() || passwordInput.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alerta");
            alert.setHeaderText(null);
            alert.setContentText("Usuário e senha obrigatório.");
            alert.showAndWait();
            return;
        }

        User user = new User();
        user.setUsername(userInput);
        user.setPassword(passwordInput);

        boolean authenticated = loginService.autenticar(user);

        if (authenticated) {
            //Alert alert = new Alert(Alert.AlertType.INFORMATION);
            //alert.setTitle("Success Message");
            //alert.setHeaderText(null);
            //alert.setContentText("Login Successful!");
            //alert.showAndWait();

            login_btn.getScene().getWindow().hide();

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/br/com/automacaowebia/dashboard.fxml"));
                Scene scene = new Scene(root);
                Stage stage = new Stage();

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
            } catch (Exception e) {
                logger.error("Error loading dashboard.fxml", e);
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alerta");
            alert.setHeaderText(null);
            alert.setContentText("Usuário ou senha inválido!");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dropShowAffect();
    }
}
