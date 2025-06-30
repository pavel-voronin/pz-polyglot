package org.pz.polyglot.ui;

import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.logging.Logger;

import org.pz.polyglot.App;
import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.i18n.I18nManager;
import org.pz.polyglot.util.FolderValidationUtils;

import java.util.logging.Level;
import java.util.ResourceBundle;

public class FolderDialogManager {
    private static final Logger logger = Logger.getLogger(FolderDialogManager.class.getName());
    private static final int DIALOG_WIDTH = 580;
    private static final int DIALOG_HEIGHT = 450;

    public static boolean showFolderDialog(Stage owner) {
        try {
            ResourceBundle bundle = I18nManager.getInstance().getBundle();
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/FolderSelectionDialog.fxml"), bundle);
            Parent root = loader.load();
            FolderSelectionDialogController controller = loader.getController();
            Stage dialog = new Stage();
            controller.setConfig(AppConfig.getInstance());
            controller.setDialogStage(dialog);
            dialog.setTitle(bundle.getString("language-dialog.title"));
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setScene(scene);
            dialog.showAndWait();
            return FolderValidationUtils.hasValidFolders(AppConfig.getInstance());
        } catch (java.io.IOException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Error opening folder selection dialog", e);
            return false;
        }
    }
}
