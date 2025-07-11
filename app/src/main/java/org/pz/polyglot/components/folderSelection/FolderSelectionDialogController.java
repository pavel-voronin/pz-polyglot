package org.pz.polyglot.components.folderSelection;

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

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.pz.polyglot.AppConfig;
import org.pz.polyglot.i18n.I18nManager;
import org.pz.polyglot.util.OsUtils;

public class FolderSelectionDialogController {
    private static final I18nManager i18n = I18nManager.getInstance();

    @FXML
    private TextField gameField;
    @FXML
    private TextField steamField;
    @FXML
    private TextField cacheField;
    @FXML
    private CheckBox gameEditableCheckBox;
    @FXML
    private CheckBox steamEditableCheckBox;
    @FXML
    private CheckBox cacheEditableCheckBox;
    @FXML
    private Button gameBrowse;
    @FXML
    private Button steamBrowse;
    @FXML
    private Button cacheBrowse;
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
        if (config.getCachePath() != null)
            cacheField.setText(config.getCachePath());

        // Set checkbox values
        gameEditableCheckBox.setSelected(config.isGamePathEditable());
        steamEditableCheckBox.setSelected(config.isSteamModsPathEditable());
        cacheEditableCheckBox.setSelected(config.isCachePathEditable());
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
        // Set tooltips for better user experience
        gameField.setTooltip(new Tooltip(i18n.getString("language-dialog.choose.game.title")));
        steamField.setTooltip(new Tooltip(i18n.getString("language-dialog.choose.steam.title")));
        cacheField.setTooltip(new Tooltip(i18n.getString("language-dialog.choose.cache.title")));

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

    @FXML
    private void onGameBrowse(ActionEvent e) {
        chooseDirectory(gameField, "language-dialog.choose.game.title");
    }

    @FXML
    private void onSteamBrowse(ActionEvent e) {
        chooseDirectory(steamField, "language-dialog.choose.steam.title");
    }

    @FXML
    private void onCacheBrowse(ActionEvent e) {
        chooseDirectory(cacheField, "language-dialog.choose.cache.title");
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
        config.setCachePath(cacheField.getText());

        // Save checkbox values
        config.setGamePathEditable(gameEditableCheckBox.isSelected());
        config.setSteamModsPathEditable(steamEditableCheckBox.isSelected());
        config.setCachePathEditable(cacheEditableCheckBox.isSelected());

        config.save();
        foldersSelected = true;
        dialogStage.close();
    }

    private void validateFields() {
        boolean valid = Stream.of(gameField, steamField, cacheField)
                .map(tf -> tf.getText().trim())
                .allMatch(text -> !text.isEmpty() && new File(text).exists() && new File(text).isDirectory());
        okButton.setDisable(!valid);
    }
}
