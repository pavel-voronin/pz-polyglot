package org.pz.polyglot.components.main;

import java.io.IOException;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.pz.polyglot.App;
import org.pz.polyglot.Logger;

/**
 * Manages the main application window lifecycle and configuration.
 */
public class MainWindowManager {
    /** Minimum width of the main window. */
    private static final int MIN_WIDTH = 300;
    /** Minimum height of the main window. */
    private static final int MIN_HEIGHT = 200;
    /** Default width of the main window. */
    private static final int DEFAULT_WIDTH = 1200;
    /** Default height of the main window. */
    private static final int DEFAULT_HEIGHT = 800;

    /** Reference to the main controller instance. */
    public static MainController mainControllerInstance;

    /**
     * Shows and configures the main application window.
     *
     * @param stage the primary stage to display
     */
    public static void showMain(Stage stage) {
        try {
            // Load the main FXML layout and initialize controller
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Main.fxml"));
            Parent root = loader.load();
            mainControllerInstance = loader.getController();
            // Provide HostServices to the controller for platform integration
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
            // Exit the application if the main window cannot be shown
            Platform.exit();
        }
    }
}
