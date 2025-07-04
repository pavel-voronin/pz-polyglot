package org.pz.polyglot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.logging.Logger;

import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.i18n.I18nManager;
import org.pz.polyglot.pz.languages.PZLanguageManager;
import org.pz.polyglot.pz.sources.PZSources;
import org.pz.polyglot.pz.translations.PZTranslationManager;
import org.pz.polyglot.ui.FolderDialogManager;
import org.pz.polyglot.ui.MainWindowManager;
import org.pz.polyglot.util.FolderValidationUtils;

public class App extends Application {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) {
        // App initialization

        I18nManager.getInstance().setLocale(AppConfig.getInstance().getLanguage());

        MainWindowManager.showMain(stage);
        if (!FolderValidationUtils.hasValidFolders(AppConfig.getInstance())) {
            boolean selected = FolderDialogManager.showFolderDialog(stage);
            if (!selected) {
                logger.warning("No valid folders selected. Exiting application.");
                Platform.exit();
            }
        }

        // PZ related initialization

        PZSources.getInstance();
        PZLanguageManager.load();
        PZTranslationManager.loadFilesFromSources();
        // Refresh translations table after loading
        javafx.application.Platform.runLater(() -> {
            if (org.pz.polyglot.ui.MainWindowManager.mainControllerInstance != null) {
                org.pz.polyglot.ui.MainWindowManager.mainControllerInstance.populateTranslationsTable();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
