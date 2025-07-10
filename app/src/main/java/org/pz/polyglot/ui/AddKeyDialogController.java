package org.pz.polyglot.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Add Key dialog.
 * Manages the dialog for adding new translation keys.
 */
public class AddKeyDialogController {

    @FXML
    private TextField keyField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private boolean keySaved = false;
    private String enteredKey;

    /**
     * Sets the dialog stage.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns whether a key was successfully saved.
     */
    public boolean isKeySaved() {
        return keySaved;
    }

    /**
     * Returns the entered key.
     */
    public String getEnteredKey() {
        return enteredKey;
    }

    @FXML
    private void initialize() {
        // Initially disable save button
        saveButton.setDisable(true);

        // Listen to key field changes to enable/disable save button
        keyField.textProperty().addListener((obs, oldText, newText) -> {
            validateInput();
        });

        // Set focus to the key field when dialog opens
        keyField.requestFocus();
    }

    /**
     * Validates the input and enables/disables the save button.
     */
    private void validateInput() {
        String text = keyField.getText().trim();
        boolean isValid = !text.isEmpty() && !text.isBlank();
        saveButton.setDisable(!isValid);
    }

    @FXML
    private void onSave() {
        String key = keyField.getText().trim();
        if (!key.isEmpty() && !key.isBlank()) {
            enteredKey = key;
            keySaved = true;
            dialogStage.close();
        }
    }

    @FXML
    private void onCancel() {
        keySaved = false;
        enteredKey = null;
        dialogStage.close();
    }
}
