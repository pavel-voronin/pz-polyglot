package org.pz.polyglot.components.main;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.pz.polyglot.App;
import org.pz.polyglot.Logger;

import java.io.IOException;
import javafx.application.Platform;

public class MainWindowManager {
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 200;
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    public static MainController mainControllerInstance;

    public static void showMain(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Main.fxml"));
            Parent root = loader.load();
            mainControllerInstance = loader.getController();
            mainControllerInstance.setHostServices(App.getInstance().getHostServices());
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            stage.setTitle("PZ Polyglot");
            stage.setScene(scene);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
            stage.show();
        } catch (IOException | IllegalStateException e) {
            Logger.error("Error opening main window", e);
            Platform.exit();
        }
    }
}
