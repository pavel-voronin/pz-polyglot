package org.pz.polyglot.initialization;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Manages the initialization window UI for Polyglot application startup.
 */
public class InitializationWindowManager {
    /**
     * The main stage for the initialization window.
     */
    private Stage stage;
    /**
     * Container for status lines displayed during initialization.
     */
    private VBox statusContainer;
    /**
     * List of status lines representing initialization steps and their results.
     */
    private List<InitializationStatus> statusLines;
    /**
     * Button to exit the application in case of error.
     */
    private Button exitButton;
    /**
     * Indicates if an error has occurred during initialization.
     */
    public boolean hasError = false;

    /**
     * Constructs the initialization window manager and creates the UI.
     */
    public InitializationWindowManager() {
        this.statusLines = new ArrayList<>();
        createUI();
    }

    /**
     * Creates and configures the initialization window UI.
     */
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

        // Ensure application exits if the window is closed (e.g., Alt+F4)
        stage.setOnCloseRequest((WindowEvent event) -> {
            Platform.exit();
        });
    }

    /**
     * Shows the initialization window.
     */
    public void show() {
        Platform.runLater(() -> stage.show());
    }

    /**
     * Hides the initialization window.
     */
    public void hide() {
        Platform.runLater(() -> stage.hide());
    }

    /**
     * Adds a new status line to the initialization window.
     * 
     * @param message the status message to display
     */
    public void addStatusLine(String message) {
        InitializationStatus status = new InitializationStatus(message);
        statusLines.add(0, status);
        refreshStatusDisplay();
    }

    /**
     * Updates the current (latest) status line with a result and error state.
     * If an error occurs, shows the exit button.
     * 
     * @param result  the result message to display
     * @param isError true if the result is an error
     */
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

    /**
     * Refreshes the status display in the window to show all current status lines.
     */
    private void refreshStatusDisplay() {
        statusContainer.getChildren().clear();
        for (InitializationStatus status : statusLines) {
            statusContainer.getChildren().add(status.getLabel());
        }
    }

    /**
     * Makes the exit button visible and managed, allowing the user to exit on
     * error.
     */
    private void showExitButton() {
        exitButton.setVisible(true);
        exitButton.setManaged(true);
    }

    /**
     * Represents a single status line in the initialization window.
     */
    private static class InitializationStatus {
        /**
         * The status message.
         */
        private String message;
        /**
         * The result message, if any.
         */
        private String result;
        /**
         * Indicates if this status line represents an error.
         */
        private boolean isError;
        /**
         * The label UI element for displaying the status.
         */
        private Label label;

        /**
         * Constructs a new status line with the given message.
         * 
         * @param message the status message
         */
        public InitializationStatus(String message) {
            this.message = message;
            this.label = new Label();
            updateLabel();
        }

        /**
         * Sets the result and error state for this status line.
         * 
         * @param result  the result message
         * @param isError true if this is an error
         */
        public void setResult(String result, boolean isError) {
            this.result = result;
            this.isError = isError;
            updateLabel();
        }

        /**
         * Updates the label text and style based on the current state.
         * Styles errors in red, successful results in green.
         */
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

        /**
         * Returns the label UI element for this status line.
         * 
         * @return the label
         */
        public Label getLabel() {
            return label;
        }
    }
}