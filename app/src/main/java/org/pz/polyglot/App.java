package org.pz.polyglot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.logging.Logger;

import org.pz.polyglot.components.folderSelection.FolderSelectionDialogManager;
import org.pz.polyglot.components.main.MainWindowManager;
import org.pz.polyglot.i18n.I18nManager;
import org.pz.polyglot.models.languages.PZLanguageManager;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.models.translations.PZTranslationManager;
import org.pz.polyglot.util.FolderValidationUtils;

public class App extends Application {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static App instance;

    public App() {
        instance = this;
    }

    @Override
    public void start(Stage stage) {
        // App initialization

        I18nManager.getInstance().setLocale(AppConfig.getInstance().getLanguage());

        MainWindowManager.showMain(stage);
        if (!FolderValidationUtils.hasValidFolders(AppConfig.getInstance())) {
            boolean selected = FolderSelectionDialogManager.showFolderDialog(stage);
            if (!selected) {
                logger.warning("No valid folders selected. Exiting application.");
                Platform.exit();
            }
        }

        // PZ related initialization

        PZSources.getInstance();
        PZLanguageManager.load();
        PZTranslationManager.loadFilesFromSources();
        State.getInstance().requestTableRebuild();
    }

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
