package org.pz.polyglot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.pz.polyglot.initialization.InitializationWindowManager;
import org.pz.polyglot.initialization.InitializationManager;

public class App extends Application {
    private static App instance;

    public App() {
        instance = this;
    }

    @Override
    public void start(Stage stage) {
        Logger.enableAll();
        Logger.info("Starting application initialization");

        // Show initialization window first
        InitializationWindowManager initWindow = new InitializationWindowManager();
        initWindow.show();

        // Run initialization in background thread to not block UI
        Thread initThread = new Thread(() -> {
            try {
                InitializationManager initManager = new InitializationManager(stage, initWindow);
                boolean initialized = initManager.initialize();

                if (!initialized) {
                    Logger.error("Application initialization failed");
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

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}