package org.pz.polyglot;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class FolderSelectionDialogController {
    @FXML
    private TextField gameField;
    @FXML
    private TextField steamField;
    @FXML
    private TextField userField;
    @FXML
    private Button gameBrowse;
    @FXML
    private Button steamBrowse;
    @FXML
    private Button userBrowse;
    @FXML
    private Button okButton;

    private AppConfig config;
    private Stage dialogStage;
    private boolean foldersSelected = false;

    public void setConfig(AppConfig config) {
        this.config = config;
        if (config.getGameFolder() != null)
            gameField.setText(config.getGameFolder().getAbsolutePath());
        if (config.getSteamModsFolder() != null)
            steamField.setText(config.getSteamModsFolder().getAbsolutePath());
        if (config.getUserModsFolder() != null)
            userField.setText(config.getUserModsFolder().getAbsolutePath());
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
        this.dialogStage.setOnCloseRequest(e -> {
            if (!foldersSelected) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit Confirmation");
                alert.setHeaderText("Folders not selected");
                alert.setContentText("Are you sure you want to exit the application?");
                java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                    Platform.exit();
                } else {
                    e.consume(); // Prevent closing
                }
            }
        });
    }

    @FXML
    private void initialize() {
        // Try to guess folders if fields are empty
        if (gameField.getText().isEmpty()) {
            Optional<File> guess = OsUtils.guessGameFolder();
            guess.ifPresent(f -> gameField.setText(f.getAbsolutePath()));
        }
        if (steamField.getText().isEmpty()) {
            Optional<File> guess = OsUtils.guessSteamModsFolder();
            guess.ifPresent(f -> steamField.setText(f.getAbsolutePath()));
        }
        if (userField.getText().isEmpty()) {
            Optional<File> guess = OsUtils.guessUserModsFolder();
            guess.ifPresent(f -> userField.setText(f.getAbsolutePath()));
        }
        okButton.setDisable(true); // Set initial state in code, not FXML
        validateFields();
        gameField.textProperty().addListener((obs, o, n) -> validateFields());
        steamField.textProperty().addListener((obs, o, n) -> validateFields());
        userField.textProperty().addListener((obs, o, n) -> validateFields());
    }

    @FXML
    private void onGameBrowse(ActionEvent e) {
        chooseDirectory(gameField, "Select Project Zomboid game folder");
    }

    @FXML
    private void onSteamBrowse(ActionEvent e) {
        chooseDirectory(steamField, "Select Steam mods folder");
    }

    @FXML
    private void onUserBrowse(ActionEvent e) {
        chooseDirectory(userField, "Select user mods folder");
    }

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

    @FXML
    private void onOk(ActionEvent e) {
        config.setGameFolder(new File(gameField.getText()));
        config.setSteamModsFolder(new File(steamField.getText()));
        config.setUserModsFolder(new File(userField.getText()));
        config.save();
        foldersSelected = true;
        dialogStage.close();
    }

    private void validateFields() {
        boolean valid = Stream.of(gameField, steamField, userField)
                .map(tf -> tf.getText().trim())
                .allMatch(text -> !text.isEmpty() && new File(text).exists() && new File(text).isDirectory());
        okButton.setDisable(!valid);
    }
}
