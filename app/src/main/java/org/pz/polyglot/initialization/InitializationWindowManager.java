package org.pz.polyglot.initialization;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

public class InitializationWindowManager {
    private Stage stage;
    private VBox statusContainer;
    private List<InitializationStatus> statusLines;
    private Button exitButton;
    public boolean hasError = false;

    public InitializationWindowManager() {
        this.statusLines = new ArrayList<>();
        createUI();
    }

    private void createUI() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Loading Polyglot...");
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        statusContainer = new VBox(5);
        statusContainer.setAlignment(Pos.TOP_LEFT);
        statusContainer.setPrefWidth(300);
        statusContainer.setMinHeight(150);

        exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());
        exitButton.setVisible(false);
        exitButton.setManaged(false);

        root.getChildren().addAll(statusContainer, exitButton);

        Scene scene = new Scene(root, 350, 220);
        stage.setScene(scene);
        stage.centerOnScreen();

        // If the window is closed (Alt+F4, etc.), exit the app
        stage.setOnCloseRequest((WindowEvent event) -> {
            Platform.exit();
        });
    }

    public void show() {
        Platform.runLater(() -> stage.show());
    }

    public void hide() {
        Platform.runLater(() -> stage.hide());
    }

    public void addStatusLine(String message) {
        InitializationStatus status = new InitializationStatus(message);
        statusLines.add(0, status);
        refreshStatusDisplay();
    }

    public void updateCurrentStatus(String result, boolean isError) {
        if (!statusLines.isEmpty()) {
            statusLines.get(0).setResult(result, isError);
            if (isError) {
                hasError = true;
                showExitButton();
            }
            refreshStatusDisplay();
        }
    }

    private void refreshStatusDisplay() {
        statusContainer.getChildren().clear();
        for (InitializationStatus status : statusLines) {
            statusContainer.getChildren().add(status.getLabel());
        }
    }

    private void showExitButton() {
        exitButton.setVisible(true);
        exitButton.setManaged(true);
    }

    private static class InitializationStatus {
        private String message;
        private String result;
        private boolean isError;
        private Label label;

        public InitializationStatus(String message) {
            this.message = message;
            this.label = new Label();
            updateLabel();
        }

        public void setResult(String result, boolean isError) {
            this.result = result;
            this.isError = isError;
            updateLabel();
        }

        private void updateLabel() {
            String text = message + "... ";
            if (result != null) {
                text += result;
            }
            
            String style = "-fx-font-size: 12px;";
            if (isError) {
                style += " -fx-text-fill: red;";
            } else if (result != null) {
                style += " -fx-text-fill: green;";
            }
            
            label.setText(text);
            label.setStyle(style);
        }

        public Label getLabel() {
            return label;
        }
    }
}