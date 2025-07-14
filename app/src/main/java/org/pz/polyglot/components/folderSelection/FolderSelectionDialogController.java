package org.pz.polyglot.components.folderSelection;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.pz.polyglot.Config;
import org.pz.polyglot.utils.OsUtils;

/**
 * Controller for the folder selection dialog in the application.
 * Handles user interaction for selecting game, Steam mods, and cache folders.
 */
public class FolderSelectionDialogController {
    /**
     * Text field for Project Zomboid game folder path.
     */
    @FXML
    private TextField gameField;
    /**
     * Text field for Steam mods folder path.
     */
    @FXML
    private TextField steamField;
    /**
     * Text field for cache folder path.
     */
    @FXML
    private TextField cacheField;
    /**
     * Checkbox to allow editing the game folder path.
     */
    @FXML
    private CheckBox gameEditableCheckBox;
    /**
     * Checkbox to allow editing the Steam mods folder path.
     */
    @FXML
    private CheckBox steamEditableCheckBox;
    /**
     * Checkbox to allow editing the cache folder path.
     */
    @FXML
    private CheckBox cacheEditableCheckBox;
    /**
     * Button to browse for the game folder.
     */
    @FXML
    private Button gameBrowse;
    /**
     * Button to browse for the Steam mods folder.
     */
    @FXML
    private Button steamBrowse;
    /**
     * Button to browse for the cache folder.
     */
    @FXML
    private Button cacheBrowse;
    /**
     * Button to confirm folder selection.
     */
    @FXML
    private Button okButton;

    /**
     * Application configuration instance.
     */
    private Config config;
    /**
     * Stage for the dialog window.
     */
    private Stage dialogStage;
    /**
     * Flag indicating whether folders have been selected.
     */
    private boolean foldersSelected = false;

    /**
     * Sets the configuration and initializes fields with config values.
     * 
     * @param config the application configuration
     */
    public void setConfig(Config config) {
        this.config = config;
        if (config.getGamePath() != null)
            gameField.setText(config.getGamePath());
        if (config.getSteamModsPath() != null)
            steamField.setText(config.getSteamModsPath());
        if (config.getCachePath() != null)
            cacheField.setText(config.getCachePath());

        // Set checkbox values
        gameEditableCheckBox.setSelected(config.isGamePathEditable());
        steamEditableCheckBox.setSelected(config.isSteamModsPathEditable());
        cacheEditableCheckBox.setSelected(config.isCachePathEditable());
    }

    /**
     * Sets the dialog stage and configures close request handling.
     * Shows confirmation if folders are not selected.
     * 
     * @param stage the dialog stage
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
        this.dialogStage.setOnCloseRequest(e -> {
            if (!foldersSelected) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit Confirmation");
                alert.setHeaderText("Folders not selected");
                alert.setContentText("Are you sure you want to exit the application?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Platform.exit();
                } else {
                    e.consume(); // Prevent closing
                }
            }
        });
    }

    /**
     * Initializes the dialog UI components and listeners.
     * Sets tooltips, initial guesses, and validation listeners.
     */
    @FXML
    private void initialize() {
        // Set tooltips for better user experience
        gameField.setTooltip(new Tooltip("Select Project Zomboid game folder"));
        steamField.setTooltip(new Tooltip("Select Steam mods folder"));
        cacheField.setTooltip(new Tooltip("Select cache folder"));

        // Set initial path guesses
        if (gameField.getText().isEmpty()) {
            Optional<File> guess = OsUtils.guessGameFolder();
            guess.ifPresent(f -> gameField.setText(f.getAbsolutePath()));
        }
        if (steamField.getText().isEmpty()) {
            Optional<File> guess = OsUtils.guessSteamModsFolder();
            guess.ifPresent(f -> steamField.setText(f.getAbsolutePath()));
        }
        if (cacheField.getText().isEmpty()) {
            Optional<File> guess = OsUtils.guessCacheFolder();
            guess.ifPresent(f -> cacheField.setText(f.getAbsolutePath()));
        }

        okButton.setDisable(true); // Set initial state in code, not FXML
        validateFields();
        gameField.textProperty().addListener((obs, o, n) -> validateFields());
        steamField.textProperty().addListener((obs, o, n) -> validateFields());
        cacheField.textProperty().addListener((obs, o, n) -> validateFields());
    }

    /**
     * Handles browse button for game folder.
     * 
     * @param e action event
     */
    @FXML
    private void onGameBrowse(ActionEvent e) {
        chooseDirectory(gameField, "Select Project Zomboid game folder");
    }

    /**
     * Handles browse button for Steam mods folder.
     * 
     * @param e action event
     */
    @FXML
    private void onSteamBrowse(ActionEvent e) {
        chooseDirectory(steamField, "Select Steam mods folder");
    }

    /**
     * Handles browse button for cache folder.
     * 
     * @param e action event
     */
    @FXML
    private void onCacheBrowse(ActionEvent e) {
        chooseDirectory(cacheField, "Select cache folder");
    }

    /**
     * Opens a directory chooser dialog and sets the selected path to the given
     * field.
     * 
     * @param field the text field to update
     * @param title the dialog title
     */
    private void chooseDirectory(TextField field, String title) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle(title);
        String path = field.getText().trim();
        if (!path.isEmpty()) {
            File initial = new File(path);
            if (initial.exists() && initial.isDirectory())
                dc.setInitialDirectory(initial);
        }
        File f = dc.showDialog(dialogStage);
        if (f != null)
            field.setText(f.getAbsolutePath());
    }

    /**
     * Handles OK button click, saves selected paths and closes the dialog.
     * 
     * @param e action event
     */
    @FXML
    private void onOk(ActionEvent e) {
        config.setGamePath(gameField.getText());
        config.setSteamModsPath(steamField.getText());
        config.setCachePath(cacheField.getText());

        // Save checkbox values
        config.setGamePathEditable(gameEditableCheckBox.isSelected());
        config.setSteamModsPathEditable(steamEditableCheckBox.isSelected());
        config.setCachePathEditable(cacheEditableCheckBox.isSelected());

        foldersSelected = true;
        dialogStage.close();
    }

    /**
     * Validates that all folder fields are non-empty and point to existing
     * directories.
     * Disables OK button if validation fails.
     */
    private void validateFields() {
        // All fields must be non-empty and point to existing directories
        boolean valid = Stream.of(gameField, steamField, cacheField)
                .map(tf -> tf.getText().trim())
                .allMatch(text -> !text.isEmpty() && new File(text).exists() && new File(text).isDirectory());
        okButton.setDisable(!valid);
    }
}
