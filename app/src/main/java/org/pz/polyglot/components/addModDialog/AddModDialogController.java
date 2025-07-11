package org.pz.polyglot.components.addModDialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Add Mod dialog.
 * Manages the dialog for adding new mod directories.
 */
public class AddModDialogController {

    @FXML
    private TextField modNameField;
    @FXML
    private ComboBox<String> versionComboBox;
    @FXML
    private Button createButton;
    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private boolean modCreated = false;
    private String enteredModName;
    private String selectedVersion;

    /**
     * Sets the dialog stage.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns whether a mod was successfully created.
     */
    public boolean isModCreated() {
        return modCreated;
    }

    /**
     * Returns the entered mod name.
     */
    public String getEnteredModName() {
        return enteredModName;
    }

    /**
     * Returns the selected version.
     */
    public String getSelectedVersion() {
        return selectedVersion;
    }

    @FXML
    private void initialize() {
        // Setup version combo box
        versionComboBox.getItems().addAll("41", "42");
        versionComboBox.setValue("42"); // Default to version 41

        // Initially disable create button
        createButton.setDisable(true);

        // Listen to mod name field changes to enable/disable create button
        modNameField.textProperty().addListener((obs, oldText, newText) -> {
            validateInput();
        });

        // Set focus to the mod name field when dialog opens
        modNameField.requestFocus();
    }

    /**
     * Validates the input and enables/disables the create button.
     */
    private void validateInput() {
        String text = modNameField.getText().trim();
        boolean isValid = !text.isEmpty() && !text.isBlank();
        createButton.setDisable(!isValid);
    }

    @FXML
    private void onCreate() {
        String modName = modNameField.getText().trim();
        String version = versionComboBox.getValue();

        if (!modName.isEmpty() && !modName.isBlank() && version != null) {
            enteredModName = modName;
            selectedVersion = version;
            modCreated = true;
            dialogStage.close();
        }
    }

    @FXML
    private void onCancel() {
        modCreated = false;
        enteredModName = null;
        selectedVersion = null;
        dialogStage.close();
    }
}
