package io.github.joshua;

import java.util.Arrays;

import io.github.joshua.user.UserMenu;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main {

    public static boolean isConsoleMode(String[] args) {
        return Arrays.asList(args).contains("--console");
    }

    public static void main(String[] args) {
        if (isConsoleMode(args)) {
            new UserMenu().initialize();
            return;
        }

        Application.launch(JavaFxBootstrap.class, args);
    }

    public static final class JavaFxBootstrap extends Application {
        public JavaFxBootstrap() {}

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
}
