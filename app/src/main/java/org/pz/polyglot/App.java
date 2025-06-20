package org.pz.polyglot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.logging.Logger;

import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.i18n.I18nManager;
import org.pz.polyglot.ui.FolderDialogManager;
import org.pz.polyglot.ui.MainWindowManager;

public class App extends Application {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) {
        AppConfig config = AppConfig.getInstance();

        String langCode = config.getLanguage();
        I18nManager.getInstance().setLocale(langCode);

        MainWindowManager.showMain(stage, config);
        if (!config.hasValidFolders()) {
            boolean selected = FolderDialogManager.showFolderDialog(stage, config);
            if (!selected) {
                logger.warning("No valid folders selected. Exiting application.");
                Platform.exit();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
