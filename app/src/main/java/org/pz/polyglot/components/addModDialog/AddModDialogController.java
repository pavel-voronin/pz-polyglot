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
    /**
     * Text field for entering the mod name.
     */
    @FXML
    private TextField modNameField;

    /**
     * Combo box for selecting the mod version.
     */
    @FXML
    private ComboBox<String> versionComboBox;

    /**
     * Button to confirm mod creation.
     */
    @FXML
    private Button createButton;

    /**
     * Button to cancel mod creation.
     */
    @FXML
    private Button cancelButton;

    /**
     * The dialog stage associated with this controller.
     */
    private Stage dialogStage;

    /**
     * Indicates whether a mod was successfully created.
     */
    private boolean modCreated = false;

    /**
     * The mod name entered by the user.
     */
    private String enteredModName;

    /**
     * The version selected by the user.
     */
    private String selectedVersion;

    /**
     * Sets the dialog stage associated with this controller.
     *
     * @param dialogStage the stage to set
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns whether a mod was successfully created.
     *
     * @return true if mod was created, false otherwise
     */
    public boolean isModCreated() {
        return modCreated;
    }

    /**
     * Returns the mod name entered by the user.
     *
     * @return the entered mod name
     */
    public String getEnteredModName() {
        return enteredModName;
    }

    /**
     * Returns the version selected by the user.
     *
     * @return the selected version
     */
    public String getSelectedVersion() {
        return selectedVersion;
    }

    /**
     * Initializes the dialog controller after its root element has been completely
     * processed.
     */
    @FXML
    private void initialize() {
        // Populate version combo box with available versions
        versionComboBox.getItems().addAll("41", "42");
        versionComboBox.setValue("42"); // Default to version 42

        // Disable create button until valid input is provided
        createButton.setDisable(true);

        // Enable/disable create button based on mod name field changes
        modNameField.textProperty().addListener((obs, oldText, newText) -> validateInput());

        // Set focus to the mod name field when dialog opens
        modNameField.requestFocus();
    }

    /**
     * Validates the mod name input and enables/disables the create button
     * accordingly.
     */
    private void validateInput() {
        String text = modNameField.getText().trim();
        boolean isValid = !text.isEmpty() && !text.isBlank();
        createButton.setDisable(!isValid);
    }

    /**
     * Handles the create button action. Stores entered values and closes the dialog
     * if input is valid.
     */
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

    /**
     * Handles the cancel button action. Resets values and closes the dialog.
     */
    @FXML
    private void onCancel() {
        modCreated = false;
        enteredModName = null;
        selectedVersion = null;
        dialogStage.close();
    }
}
