package org.pz.polyglot.components.addKeyDialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.pz.polyglot.App;
import org.pz.polyglot.Logger;

import java.io.IOException;

/**
 * Manager for the Add Key dialog.
 * Handles showing the modal dialog for adding new translation keys.
 */
public class AddKeyDialogManager {
    private static final int DIALOG_WIDTH = 350;
    private static final int DIALOG_HEIGHT = 200;

    /**
     * Shows the Add Key dialog and returns the entered key if saved.
     * 
     * @param owner the parent stage
     * @return the entered key if saved, null if cancelled
     */
    public static String showAddKeyDialog(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AddKeyDialog.fxml"));
            Parent root = loader.load();
            AddKeyDialogController controller = loader.getController();

            Stage dialog = new Stage();
            controller.setDialogStage(dialog);
            dialog.setTitle("Add Translation Key");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setScene(scene);
            dialog.showAndWait();

            if (controller.isKeySaved()) {
                return controller.getEnteredKey();
            }
            return null;

        } catch (IOException e) {
            Logger.error("Error opening add key dialog: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
