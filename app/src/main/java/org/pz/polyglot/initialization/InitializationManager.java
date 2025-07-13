package org.pz.polyglot.initialization;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.pz.polyglot.Config;
import org.pz.polyglot.Logger;
import org.pz.polyglot.State;
import org.pz.polyglot.components.folderSelection.FolderSelectionDialogManager;
import org.pz.polyglot.components.main.MainWindowManager;
import org.pz.polyglot.models.languages.PZLanguageManager;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.models.translations.PZTranslationManager;
import org.pz.polyglot.utils.FolderValidationUtils;

/**
 * Manages the multi-phase application initialization process.
 * Ensures proper dependency order and clear separation of concerns.
 */
public class InitializationManager {
    private final Stage primaryStage;
    private final InitializationWindowManager initWindow;
    private Config config;
    private State state;

    public InitializationManager(Stage primaryStage, InitializationWindowManager initWindow) {
        this.primaryStage = primaryStage;
        this.initWindow = initWindow;
    }

    /**
     * Main initialization entry point.
     * Orchestrates the entire initialization process.
     */
    public boolean initialize() {
        try {
            // Phase 1: Basic configuration loading
            if (!initializeBaseConfiguration()) {
                return false;
            }

            // Phase 2: Folder validation and configuration
            if (!validateAndConfigureFolders()) {
                return false;
            }

            // Phase 3: Domain models initialization
            if (!initializeDomainModels()) {
                return false;
            }

            // Phase 4: Configuration validation and sync
            if (!validateAndSyncConfiguration()) {
                return false;
            }

            // Phase 5: Application state initialization
            if (!initializeApplicationState()) {
                return false;
            }

            // Phase 6: Final setup and show main window
            finalizeInitialization();

            Logger.info("Application initialization completed successfully");
            return true;

        } catch (Exception e) {
            Logger.error("Critical error during initialization: " + e.getMessage());
            Platform.runLater(() -> {
                initWindow.updateCurrentStatus("Critical error: " + e.getMessage(), true);
            });
            return false;
        }
    }

    /**
     * Phase 1: Load basic configuration without domain-dependent defaults.
     * This creates minimal viable config that allows UI to show.
     */
    private boolean initializeBaseConfiguration() {
        Logger.info("Phase 1: Initializing base configuration");
        Platform.runLater(() -> initWindow.addStatusLine("Loading configuration"));

        try {
            config = Config.getInstance();

            Platform.runLater(() -> initWindow.updateCurrentStatus("done", false));
            return true;
        } catch (Exception e) {
            Logger.error("Failed to initialize base configuration: " + e.getMessage());
            Platform.runLater(() -> initWindow.updateCurrentStatus("failed: " + e.getMessage(), true));
            return false;
        }
    }

    /**
     * Phase 2: Validate and configure folders.
     * This must happen before domain models can be loaded.
     */
    private boolean validateAndConfigureFolders() {
        Logger.info("Phase 2: Validating and configuring folders");
        Platform.runLater(() -> initWindow.addStatusLine("Validating folders"));

        try {
            // Check if we need folder configuration
            if (!FolderValidationUtils.hasValidFolders(config)) {
                Logger.info("Invalid folders detected, showing folder selection dialog");
                Platform.runLater(() -> {
                    initWindow.updateCurrentStatus("configuration required", false);
                    initWindow.addStatusLine("Waiting for folder configuration");
                });

                final boolean[] selected = new boolean[1];
                final boolean[] dialogCompleted = new boolean[1];

                Platform.runLater(() -> {
                    try {
                        selected[0] = FolderSelectionDialogManager.showFolderDialog(primaryStage);
                    } catch (Exception e) {
                        Logger.error("Error during folder selection: " + e.getMessage());
                        selected[0] = false;
                    } finally {
                        dialogCompleted[0] = true;
                        synchronized (this) {
                            this.notify();
                        }
                    }
                });

                synchronized (this) {
                    while (!dialogCompleted[0]) {
                        this.wait();
                    }
                }

                if (!selected[0]) {
                    Logger.warning("No valid folders selected. Exiting application.");
                    Platform.runLater(() -> initWindow.updateCurrentStatus("cancelled by user", true));
                    return false;
                }

                // Folders have been configured, config should be updated automatically
                Logger.info("Folders configured successfully");
                Platform.runLater(() -> initWindow.updateCurrentStatus("configured", false));
            } else {
                Platform.runLater(() -> initWindow.updateCurrentStatus("valid", false));
            }

            return true;
        } catch (Exception e) {
            Logger.error("Failed to validate and configure folders: " + e.getMessage());
            Platform.runLater(() -> initWindow.updateCurrentStatus("failed: " + e.getMessage(), true));
            return false;
        }
    }

    /**
     * Phase 3: Initialize domain models that depend on configured folders.
     * This phase loads all domain data from the file system.
     */
    private boolean initializeDomainModels() {
        Logger.info("Phase 3: Initializing domain models");

        try {
            // Initialize PZ sources (requires valid folders)
            Platform.runLater(() -> initWindow.addStatusLine("Loading sources"));
            PZSources.getInstance();
            Platform.runLater(() -> initWindow.updateCurrentStatus("done", false));

            // Load language definitions
            Platform.runLater(() -> initWindow.addStatusLine("Loading language definitions"));
            PZLanguageManager.load();
            Platform.runLater(() -> initWindow.updateCurrentStatus("done", false));

            // Load translation files from sources
            Platform.runLater(() -> initWindow.addStatusLine("Loading translation files"));
            PZTranslationManager.loadFilesFromSources();
            Platform.runLater(() -> initWindow.updateCurrentStatus("done", false));

            Logger.info("Domain models initialized successfully");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to initialize domain models: " + e.getMessage());
            Platform.runLater(() -> initWindow.updateCurrentStatus("failed: " + e.getMessage(), true));
            return false;
        }
    }

    /**
     * Phase 4: Validate configuration against loaded domain models and sync.
     * This ensures config contains all necessary data with proper defaults.
     */
    private boolean validateAndSyncConfiguration() {
        Logger.info("Phase 4: Validating and syncing configuration");
        Platform.runLater(() -> initWindow.addStatusLine("Validating configuration"));

        try {
            ConfigValidator validator = new ConfigValidator(config);

            // Validate and update sources
            validator.validateAndUpdateSources();

            // Validate and update languages
            validator.validateAndUpdateLanguages();

            // Validate and update translation types
            validator.validateAndUpdateTranslationTypes();

            Platform.runLater(() -> initWindow.updateCurrentStatus("done", false));
            Logger.info("Configuration validation and sync completed");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to validate and sync configuration: " + e.getMessage());
            Platform.runLater(() -> initWindow.updateCurrentStatus("failed: " + e.getMessage(), true));
            return false;
        }
    }

    /**
     * Phase 5: Initialize application state with validated configuration.
     * State becomes the single source of truth from this point forward.
     */
    private boolean initializeApplicationState() {
        Logger.info("Phase 5: Initializing application state");
        Platform.runLater(() -> initWindow.addStatusLine("Initializing application state"));

        try {
            // Initialize state with validated config
            state = State.getInstance();

            // State constructor already loads from config, but we need to ensure it's fresh
            // Request initial table rebuild to populate UI
            state.requestTableRebuild();

            Platform.runLater(() -> initWindow.updateCurrentStatus("done", false));
            Logger.info("Application state initialized successfully");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to initialize application state: " + e.getMessage());
            Platform.runLater(() -> initWindow.updateCurrentStatus("failed: " + e.getMessage(), true));
            return false;
        }
    }

    /**
     * Phase 6: Finalize initialization by enabling auto-save and showing main
     * window.
     */
    private void finalizeInitialization() {
        Logger.info("Phase 6: Finalizing initialization");
        Platform.runLater(() -> initWindow.addStatusLine("Starting application"));

        // Enable config auto-save now that everything is stable
        config.enableAutoSave();

        Platform.runLater(() -> {
            initWindow.updateCurrentStatus("done", false);
        });

        // Show main window and hide initialization window
        Platform.runLater(() -> {
            initWindow.hide();
            MainWindowManager.showMain(primaryStage);
        });

        Logger.info("Initialization finalization completed");
    }
}