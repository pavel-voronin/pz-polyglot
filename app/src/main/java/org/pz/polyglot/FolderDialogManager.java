package org.pz.polyglot;

import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FolderDialogManager {
    private static final Logger logger = Logger.getLogger(FolderDialogManager.class.getName());
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 300;

    public static boolean showFolderDialog(Stage owner, AppConfig config) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/FolderSelectionDialog.fxml"));
            Parent root = loader.load();
            FolderSelectionDialogController controller = loader.getController();
            Stage dialog = new Stage();
            controller.setConfig(config);
            controller.setDialogStage(dialog);
            dialog.setTitle("Select Project Zomboid Folders");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT));
            dialog.showAndWait();
            return config.hasValidFolders();
        } catch (java.io.IOException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Error opening folder selection dialog", e);
            return false;
        }
    }
}
