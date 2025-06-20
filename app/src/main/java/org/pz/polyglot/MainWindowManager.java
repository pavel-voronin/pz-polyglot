package org.pz.polyglot;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainWindowManager {
    private static final Logger logger = Logger.getLogger(MainWindowManager.class.getName());
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 200;
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    public static void showMain(Stage stage, AppConfig config) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            stage.setTitle("PZ Polyglot");
            stage.setScene(scene);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
            stage.show();
        } catch (java.io.IOException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Error opening main window", e);
            javafx.application.Platform.exit();
        }
    }
}
