package org.pz.polyglot.components.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.application.HostServices;

import org.pz.polyglot.State;
import org.pz.polyglot.components.TranslationPanel;
import org.pz.polyglot.components.TypesPanel;
import org.pz.polyglot.components.SourcesPanel;
import org.pz.polyglot.components.LanguagesPanel;

/**
 * Main controller for the Polyglot application.
 */
public class MainController {
    /**
     * Menu item for quitting the application.
     */
    @FXML
    private MenuItem quitMenuItem;

    /**
     * Menu item for opening the GitHub repository.
     */
    @FXML
    private MenuItem githubMenuItem;

    /**
     * Menu item for joining the Discord community.
     */
    @FXML
    private MenuItem discordMenuItem;

    /**
     * Panel displaying translation data.
     */
    @FXML
    private TranslationPanel translationPanel;

    /**
     * Panel displaying types information.
     */
    @FXML
    private TypesPanel typesPanel;

    /**
     * Panel displaying sources information.
     */
    @FXML
    private SourcesPanel sourcesPanel;

    /**
     * Panel displaying languages information.
     */
    @FXML
    private LanguagesPanel languagesPanel;

    /**
     * Main split pane containing all panels.
     */
    @FXML
    private SplitPane mainSplitPane;

    /**
     * Application state manager singleton.
     */
    private final State stateManager = State.getInstance();

    /**
     * Host services for opening external links.
     */
    private HostServices hostServices;

    /**
     * Initializes the controller and sets up menu actions and panel bindings.
     */
    @FXML
    private void initialize() {
        initializeMenuActions();
        setupObservableBindings();
    }

    /**
     * Sets up actions for menu items: quit, GitHub, and Discord.
     */
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

    /**
     * Sets up observable bindings for panel visibility and layout management.
     * Ensures panels are added/removed from the split pane according to state
     * changes.
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
                    // Types panel should always be the first in the split pane
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
                    if (stateManager.isTypesPanelVisible()) {
                        insertIndex++;
                    }
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
                    if (stateManager.isTypesPanelVisible()) {
                        insertIndex++;
                    }
                    if (stateManager.isSourcesPanelVisible()) {
                        insertIndex++;
                    }
                    mainSplitPane.getItems().add(insertIndex, languagesPanel);
                    SplitPane.setResizableWithParent(languagesPanel, false);
                }
            }
        });
        // Ensure only visible panels are present in the split pane after initialization
        Platform.runLater(() -> {
            if (translationPanel != null) {
                translationPanel.setVisible(false);
                translationPanel.setManaged(false);
                mainSplitPane.getItems().remove(translationPanel);
            }
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

    /**
     * Sets the HostServices instance for opening external links.
     *
     * @param hostServices the HostServices instance to use
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
