package org.pz.polyglot.components.addKeyDialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Add Key dialog.
 * Handles user interactions for adding new translation keys.
 */
public class AddKeyDialogController {

    /**
     * Text field for entering the translation key.
     */
    @FXML
    private TextField keyField;

    /**
     * Button to save the entered key.
     */
    @FXML
    private Button saveButton;

    /**
     * Button to cancel the dialog.
     */
    @FXML
    private Button cancelButton;

    /**
     * The stage representing this dialog window.
     */
    private Stage dialogStage;

    /**
     * Indicates whether a key was successfully saved.
     */
    private boolean keySaved = false;

    /**
     * Stores the entered key if saved.
     */
    private String enteredKey;

    /**
     * Sets the dialog stage for this controller.
     * 
     * @param dialogStage the stage representing the dialog window
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns whether a key was successfully saved.
     * 
     * @return true if the key was saved, false otherwise
     */
    public boolean isKeySaved() {
        return keySaved;
    }

    /**
     * Returns the entered key if saved.
     * 
     * @return the entered key, or null if not saved
     */
    public String getEnteredKey() {
        return enteredKey;
    }

    /**
     * Initializes the dialog controller. Sets up listeners and initial state.
     */
    @FXML
    private void initialize() {
        saveButton.setDisable(true);
        keyField.textProperty().addListener((obs, oldText, newText) -> validateInput());
        keyField.requestFocus();
    }

    /**
     * Validates the key field input and enables/disables the save button
     * accordingly.
     * The key must not be empty or blank.
     */
    private void validateInput() {
        String text = keyField.getText().trim();
        boolean isValid = !text.isEmpty() && !text.isBlank();
        saveButton.setDisable(!isValid);
    }

    /**
     * Handles the save button action. Saves the entered key if valid and closes the
     * dialog.
     */
    @FXML
    private void onSave() {
        String key = keyField.getText().trim();
        if (!key.isEmpty() && !key.isBlank()) {
            enteredKey = key;
            keySaved = true;
            dialogStage.close();
        }
    }

    /**
     * Handles the cancel button action. Discards the entered key and closes the
     * dialog.
     */
    @FXML
    private void onCancel() {
        keySaved = false;
        enteredKey = null;
        dialogStage.close();
    }
}
