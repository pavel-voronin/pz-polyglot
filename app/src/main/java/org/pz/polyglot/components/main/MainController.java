package org.pz.polyglot.components.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.application.HostServices;

import org.pz.polyglot.State;
import org.pz.polyglot.components.ToolbarComponent;
import org.pz.polyglot.components.TranslationPanel;
import org.pz.polyglot.components.TranslationTable;
import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.models.translations.PZTranslations;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;
import org.pz.polyglot.viewModels.registries.TranslationEntryViewModelRegistry;

import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 * Main controller for the Polyglot application.
 * Handles initialization and configuration of the main TableView.
 */
public class MainController {
    @FXML
    private TextField filterField;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu helpMenu;
    @FXML
    private MenuItem quitMenuItem;
    @FXML
    private MenuItem githubMenuItem;
    @FXML
    private MenuItem discordMenuItem;
    @FXML
    private TranslationPanel translationPanel;
    @FXML
    private ToolbarComponent toolbarComponent;
    @FXML
    private TranslationTable translationTable;
    @FXML
    private SplitPane mainSplitPane;
    private ObservableList<TranslationEntryViewModel> allTableItems = FXCollections.observableArrayList();
    private final State stateManager = State.getInstance();
    private HostServices hostServices;

    /**
     * Initializes the TableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
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
        setupRowSelectionListener();
        setupObservableBindings();
        setupFilterField();
        // Ensure TranslationTable takes all space if TranslationPanel is hidden at startup
        Platform.runLater(() -> {
            if (!translationPanel.isVisible() && mainSplitPane.getItems().size() == 2) {
                mainSplitPane.getItems().remove(1);
            }
        });
    }

    private void setupFilterField() {
        filterField.setPromptText("Filter by key...");
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredItems(newValue);
        });
    }

    private void updateFilteredItems(String filterText) {
        ObservableList<TranslationEntryViewModel> filtered = FXCollections.observableArrayList();
        for (TranslationEntryViewModel item : allTableItems) {
            if (filterText == null || filterText.trim().isEmpty()
                    || item.getKey().toLowerCase().contains(filterText.toLowerCase())) {
                filtered.add(item);
            }
        }
        translationTable.setItems(filtered);
    }

    /**
     * Sets up Observable bindings to replace callbacks.
     */
    private void setupObservableBindings() {
        stateManager.saveAllTriggeredProperty().addListener((obs, oldVal, newVal) -> {
            refreshTableIndicators();
        });

        stateManager.rightPanelVisibleProperty().addListener((obs, oldVal, newVal) -> {
            translationPanel.setVisible(newVal);
            translationPanel.setManaged(newVal);
            if (!newVal) {
                translationTable.getSelectionModel().clearSelection();
                // Hide right panel in SplitPane, so left takes all space
                if(mainSplitPane.getItems().size() == 2) {
                    mainSplitPane.getItems().remove(1);
                }
            } else {
                // Show right panel in SplitPane if not present
                if(!mainSplitPane.getItems().contains(translationPanel)) {
                    mainSplitPane.getItems().add(translationPanel);
                }
            }
        });

        stateManager.refreshKeyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                refreshTableIndicatorsForKey(newVal);
            }
        });

        stateManager.tableRebuildRequiredProperty().addListener((obs, oldVal, newVal) -> {
            populateTranslationsTable();
        });

        stateManager.selectedTranslationKeyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                PZTranslationEntry entry = PZTranslations.getInstance().getOrCreateTranslation(newVal);
                TranslationEntryViewModel entryViewModel = TranslationEntryViewModelRegistry.getViewModel(entry);
                showTranslationPanel(entryViewModel);
            }
        });
    }

    /**
     * Sets up the row selection listener for the TableView.
     */
    private void setupRowSelectionListener() {
        translationTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        stateManager.setSelectedTranslationKey(newSelection.getKey());
                        stateManager.setRightPanelVisible(true);
                    }
                });
    }

    /**
     * Shows the right panel with translation details for the given key.
     */
    private void showTranslationPanel(TranslationEntryViewModel entryViewModel) {
        translationPanel.showTranslation(entryViewModel);
        translationPanel.setVisible(true);
        translationPanel.setManaged(true);
    }

    public void populateTranslationsTable() {
        PZTranslations translations = PZTranslations.getInstance();
        Map<String, PZTranslationEntry> allTranslations = translations.getAllTranslations();
        allTableItems.clear();
        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            PZTranslationEntry translationEntry = entry.getValue();
            TranslationEntryViewModel entryViewModel = TranslationEntryViewModelRegistry.getViewModel(translationEntry);
            allTableItems.add(entryViewModel);
        }
        updateFilteredItems(filterField.getText());
    }

    /**
     * Refreshes the table indicators to show current state of translations and
     * changes.
     */
    public void refreshTableIndicators() {
        translationTable.refreshTableIndicators();
    }

    /**
     * Refreshes the table indicators for a specific translation key.
     * This is more efficient than refreshing the entire table.
     */
    private void refreshTableIndicatorsForKey(String translationKey) {
        translationTable.refreshTableIndicatorsForKey(translationKey);
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
