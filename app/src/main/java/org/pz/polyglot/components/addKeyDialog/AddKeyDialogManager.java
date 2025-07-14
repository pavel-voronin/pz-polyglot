package org.pz.polyglot.components.addKeyDialog;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.pz.polyglot.App;
import org.pz.polyglot.Logger;

/**
 * Manager for the Add Key dialog.
 * <p>
 * Responsible for displaying the modal dialog for adding new translation keys.
 */
public class AddKeyDialogManager {

    /**
     * The width of the Add Key dialog window in pixels.
     */
    private static final int DIALOG_WIDTH = 350;

    /**
     * The height of the Add Key dialog window in pixels.
     */
    private static final int DIALOG_HEIGHT = 200;

    /**
     * Displays the Add Key dialog as a modal window.
     * <p>
     * If the user saves a new key, returns the entered key value. If the dialog is
     * cancelled, returns {@code null}.
     *
     * @param owner the parent {@link Stage} for the dialog
     * @return the entered key if saved, or {@code null} if cancelled
     */
    public static String showAddKeyDialog(Stage owner) {
        try {
            // Load the FXML layout and controller for the dialog
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AddKeyDialog.fxml"));
            Parent root = loader.load();
            AddKeyDialogController controller = loader.getController();

            // Create and configure the dialog stage
            Stage dialog = new Stage();
            controller.setDialogStage(dialog);
            dialog.setTitle("Add Translation Key");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            // Set the scene and show the dialog modally
            Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setScene(scene);
            dialog.showAndWait();

            // Return the entered key if saved, otherwise null
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
