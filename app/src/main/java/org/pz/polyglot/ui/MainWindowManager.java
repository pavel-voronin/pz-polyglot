package org.pz.polyglot.ui;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.logging.Logger;

import org.pz.polyglot.App;
import org.pz.polyglot.i18n.I18nManager;

import java.util.logging.Level;
import javafx.application.Platform;

public class MainWindowManager {
    private static final I18nManager i18n = I18nManager.getInstance();
    private static final Logger logger = Logger.getLogger(MainWindowManager.class.getName());
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 200;
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    public static void showMain(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Main.fxml"), i18n.getBundle());
            Parent root = loader.load();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            stage.setTitle(i18n.getString("app.title"));
            stage.setScene(scene);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
            stage.show();
        } catch (java.io.IOException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Error opening main window", e);
            Platform.exit();
        }
    }
}
