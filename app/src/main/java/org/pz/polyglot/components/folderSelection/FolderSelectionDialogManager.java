package org.pz.polyglot.components.folderSelection;

import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.pz.polyglot.App;
import org.pz.polyglot.Config;
import org.pz.polyglot.Logger;
import org.pz.polyglot.utils.FolderValidationUtils;

import java.io.IOException;

public class FolderSelectionDialogManager {
    private static final int DIALOG_WIDTH = 580;
    private static final int DIALOG_HEIGHT = 450;

    public static boolean showFolderDialog(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/FolderSelectionDialog.fxml"));
            Parent root = loader.load();
            FolderSelectionDialogController controller = loader.getController();
            Stage dialog = new Stage();
            controller.setConfig(Config.getInstance());
            controller.setDialogStage(dialog);
            dialog.setTitle("Select Project Zomboid Folders");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setAlwaysOnTop(true);

            dialog.setOnCloseRequest(event -> {
            });

            Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setScene(scene);
            dialog.showAndWait();
            return FolderValidationUtils.hasValidFolders(Config.getInstance());
        } catch (IOException | IllegalStateException e) {
            Logger.error("Error opening folder selection dialog", e);
            return false;
        }
    }
}