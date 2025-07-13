package org.pz.polyglot.components.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.application.HostServices;

import org.pz.polyglot.State;
import org.pz.polyglot.components.TranslationPanel;
import org.pz.polyglot.components.TypesPanel;
import org.pz.polyglot.components.SourcesPanel;
import org.pz.polyglot.components.LanguagesPanel;

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
    private TypesPanel typesPanel;
    @FXML
    private SourcesPanel sourcesPanel;
    @FXML
    private LanguagesPanel languagesPanel;
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
                mainSplitPane.getItems().remove(translationPanel);
            } else {
                if (!mainSplitPane.getItems().contains(translationPanel)) {
                    mainSplitPane.getItems().add(translationPanel);
                }
            }
        });
        stateManager.typesPanelVisibleProperty().addListener((obs, oldVal, newVal) -> {
            typesPanel.setVisible(newVal);
            typesPanel.setManaged(newVal);
            if (!newVal) {
                mainSplitPane.getItems().remove(typesPanel);
            } else {
                if (!mainSplitPane.getItems().contains(typesPanel)) {
                    // Types panel should be first
                    mainSplitPane.getItems().add(0, typesPanel);
                    SplitPane.setResizableWithParent(typesPanel, false);
                }
            }
        });
        stateManager.sourcesPanelVisibleProperty().addListener((obs, oldVal, newVal) -> {
            sourcesPanel.setVisible(newVal);
            sourcesPanel.setManaged(newVal);
            if (!newVal) {
                mainSplitPane.getItems().remove(sourcesPanel);
            } else {
                if (!mainSplitPane.getItems().contains(sourcesPanel)) {
                    // Sources panel should be after types panel but before languages panel
                    int insertIndex = 0;
                    if (stateManager.isTypesPanelVisible())
                        insertIndex++;
                    // Don't count languages panel since sources should be before it
                    mainSplitPane.getItems().add(insertIndex, sourcesPanel);
                    SplitPane.setResizableWithParent(sourcesPanel, false);
                }
            }
        });
        stateManager.languagesPanelVisibleProperty().addListener((obs, oldVal, newVal) -> {
            languagesPanel.setVisible(newVal);
            languagesPanel.setManaged(newVal);
            if (!newVal) {
                mainSplitPane.getItems().remove(languagesPanel);
            } else {
                if (!mainSplitPane.getItems().contains(languagesPanel)) {
                    // Languages panel should be after types and sources panels
                    int insertIndex = 0;
                    if (stateManager.isTypesPanelVisible())
                        insertIndex++;
                    if (stateManager.isSourcesPanelVisible())
                        insertIndex++;
                    mainSplitPane.getItems().add(insertIndex, languagesPanel);
                    SplitPane.setResizableWithParent(languagesPanel, false);
                }
            }
        });
        // Ensure only visible panels are present
        Platform.runLater(() -> {
            // Clear all panel visibility
            if (typesPanel != null) {
                typesPanel.setVisible(false);
                typesPanel.setManaged(false);
                mainSplitPane.getItems().remove(typesPanel);
            }
            if (sourcesPanel != null) {
                sourcesPanel.setVisible(false);
                sourcesPanel.setManaged(false);
                mainSplitPane.getItems().remove(sourcesPanel);
            }
            if (languagesPanel != null) {
                languagesPanel.setVisible(false);
                languagesPanel.setManaged(false);
                mainSplitPane.getItems().remove(languagesPanel);
            }
        });
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
