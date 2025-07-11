package org.pz.polyglot.components.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.application.HostServices;

import org.pz.polyglot.State;
import org.pz.polyglot.components.TranslationPanel;

import javafx.scene.control.*;

/**
 * Main controller for the Polyglot application.
 * Handles initialization and configuration of the main TableView.
 */
public class MainController {
    @FXML
    private TextField filterField;
    @FXML
    private MenuItem quitMenuItem;
    @FXML
    private MenuItem githubMenuItem;
    @FXML
    private MenuItem discordMenuItem;
    @FXML
    private TranslationPanel translationPanel;
    @FXML
    private SplitPane mainSplitPane;
    private final State stateManager = State.getInstance();
    private HostServices hostServices;

    /**
     * Initializes the TableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        initializeMenuActions();
        setupObservableBindings();
        setupFilterField();

        Platform.runLater(() -> {
            if (!translationPanel.isVisible() && mainSplitPane.getItems().size() == 2) {
                mainSplitPane.getItems().remove(1);
            }
        });
    }

    private void initializeMenuActions() {
        quitMenuItem.setOnAction(event -> Platform.exit());
        githubMenuItem.setOnAction(event -> {
            if (hostServices != null) {
                hostServices.showDocument("https://github.com/pavel-voronin/pz-polyglot");
            }
        });
        discordMenuItem.setOnAction(event -> {
            if (hostServices != null) {
                hostServices.showDocument("https://discord.gg/byCBHwpa");
            }
        });
    }

    private void setupFilterField() {
        stateManager.filterTextProperty().bind(filterField.textProperty());
    }

    /**
     * Sets up Observable bindings to replace callbacks.
     */
    private void setupObservableBindings() {
        stateManager.rightPanelVisibleProperty().addListener((obs, oldVal, newVal) -> {
            translationPanel.setVisible(newVal);
            translationPanel.setManaged(newVal);
            if (!newVal) {
                // Hide right panel in SplitPane, so left takes all space
                if (mainSplitPane.getItems().size() == 2) {
                    mainSplitPane.getItems().remove(1);
                }
            } else {
                // Show right panel in SplitPane if not present
                if (!mainSplitPane.getItems().contains(translationPanel)) {
                    mainSplitPane.getItems().add(translationPanel);
                }
            }
        });
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
