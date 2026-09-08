package io.github.joshua;
import java.util.Arrays;

import io.github.joshua.user.UserMenu;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--console")) {
            new UserMenu().initialize();
            return;
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/io/github/joshua/ui/Login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Main.class.getResource("/io/github/joshua/ui/app.css").toExternalForm());
        stage.setTitle("Business Sales Core");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }
}
