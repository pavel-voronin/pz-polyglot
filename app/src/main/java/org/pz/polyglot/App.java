package org.pz.polyglot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.pz.polyglot.initialization.InitializationWindowManager;
import org.pz.polyglot.initialization.InitializationManager;

/**
 * Main application class for Polyglot.
 * Handles application startup, initialization, and provides singleton access.
 */
public class App extends Application {
    /**
     * Singleton instance of the application.
     */
    private static App instance;

    /**
     * Constructs the application and sets the singleton instance.
     */
    public App() {
        instance = this;
    }

    /**
     * Entry point for JavaFX application.
     * Initializes logging, shows initialization window, and starts background
     * initialization.
     *
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        Logger.enableAll();
        Logger.info("Starting application initialization");

        // Show initialization window before starting background initialization
        InitializationWindowManager initWindow = new InitializationWindowManager();
        initWindow.show();

        // Run initialization in a background thread to avoid blocking the UI thread
        Thread initThread = new Thread(() -> {
            try {
                InitializationManager initManager = new InitializationManager(stage, initWindow);
                boolean initialized = initManager.initialize();

                if (!initialized) {
                    Logger.error("Application initialization failed");
                    // Only update status if no error has been shown yet
                    if (!initWindow.hasError) {
                        Platform.runLater(() -> {
                            initWindow.updateCurrentStatus("Application initialization failed", true);
                        });
                    }
                    return;
                }

                Logger.info("Application started successfully");
            } catch (Exception e) {
                Logger.error("Critical error during initialization: " + e.getMessage());
                Platform.runLater(() -> {
                    initWindow.updateCurrentStatus("Critical error: " + e.getMessage(), true);
                });
            }
        });

        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * Returns the singleton instance of the application.
     *
     * @return the App instance
     */
    public static App getInstance() {
        return instance;
    }

    /**
     * Main entry point for launching the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}