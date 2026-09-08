package io.github.joshua.ui;

import io.github.joshua.Main;
import io.github.joshua.user.UserAuth;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void login() {
        try {
            if (new UserAuth().login(usernameField.getText().trim(), passwordField.getText())) {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/io/github/joshua/ui/Dashboard.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(Main.class.getResource("/io/github/joshua/ui/app.css").toExternalForm());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Business Sales Core | Panel principal");
                stage.setMaximized(true);
            } else {
                errorLabel.setText("Usuario o contraseña incorrectos.");
                passwordField.clear();
            }
        } catch (RuntimeException exception) {
            errorLabel.setText("No se pudo leer la configuración. Revisa el archivo .env.");
        } catch (Exception exception) {
            errorLabel.setText("No se pudo abrir el panel principal.");
        }
    }

    @FXML
    private void exit(Node source) {
        ((Stage) source.getScene().getWindow()).close();
    }
}