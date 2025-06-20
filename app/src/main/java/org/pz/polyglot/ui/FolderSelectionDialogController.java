package org.pz.polyglot.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.i18n.I18nManager;
import org.pz.polyglot.util.OsUtils;

public class FolderSelectionDialogController {
    private static final I18nManager i18n = I18nManager.getInstance();

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
        if (config.getGamePath() != null)
            gameField.setText(config.getGamePath());
        if (config.getSteamModsPath() != null)
            steamField.setText(config.getSteamModsPath());
        if (config.getUserModsPath() != null)
            userField.setText(config.getUserModsPath());
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
        this.dialogStage.setOnCloseRequest(e -> {
            if (!foldersSelected) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(i18n.getString("language-not-set-alert.exit.title"));
                alert.setHeaderText(i18n.getString("language-not-set-alert.exit.header"));
                alert.setContentText(i18n.getString("language-not-set-alert.exit.message"));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Platform.exit();
                } else {
                    e.consume(); // Prevent closing
                }
            }
        });
    }

    @FXML
    private void initialize() {
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
        chooseDirectory(gameField, "language-dialog.choose.game.title");
    }

    @FXML
    private void onSteamBrowse(ActionEvent e) {
        chooseDirectory(steamField, "language-dialog.choose.steam.title");
    }

    @FXML
    private void onUserBrowse(ActionEvent e) {
        chooseDirectory(userField, "language-dialog.choose.user.title");
    }

    private void chooseDirectory(TextField field, String key) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle(i18n.getString(key));
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
        config.setGamePath(gameField.getText());
        config.setSteamModsPath(steamField.getText());
        config.setUserModsPath(userField.getText());
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
