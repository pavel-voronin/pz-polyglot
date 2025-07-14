package org.pz.polyglot.components.folderSelection;

import java.io.IOException;

import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.pz.polyglot.App;
import org.pz.polyglot.Config;
import org.pz.polyglot.Logger;
import org.pz.polyglot.utils.FolderValidationUtils;

/**
 * Manages the display and logic of the folder selection dialog for Project
 * Zomboid folders.
 */
public class FolderSelectionDialogManager {
    /**
     * The width of the folder selection dialog in pixels.
     */
    private static final int DIALOG_WIDTH = 580;
    /**
     * The height of the folder selection dialog in pixels.
     */
    private static final int DIALOG_HEIGHT = 450;

    /**
     * Shows the folder selection dialog and returns whether valid folders were
     * selected.
     *
     * @param owner the parent stage for the dialog
     * @return true if valid folders are selected, false otherwise
     */
    public static boolean showFolderDialog(Stage owner) {
        try {
            // Load the FXML for the dialog
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/FolderSelectionDialog.fxml"));
            Parent root = loader.load();
            FolderSelectionDialogController controller = loader.getController();
            Stage dialog = new Stage();
            // Pass configuration and dialog stage to the controller
            controller.setConfig(Config.getInstance());
            controller.setDialogStage(dialog);
            dialog.setTitle("Select Project Zomboid Folders");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setAlwaysOnTop(true);

            // No-op close request handler (can be extended for custom logic)
            dialog.setOnCloseRequest(event -> {
            });

            Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setScene(scene);
            dialog.showAndWait();
            // Validate selected folders after dialog closes
            return FolderValidationUtils.hasValidFolders(Config.getInstance());
        } catch (IOException | IllegalStateException e) {
            Logger.error("Error opening folder selection dialog", e);
            return false;
        }
    }
}