package org.pz.polyglot.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import org.pz.polyglot.pz.translations.PZTranslations;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.ui.components.TranslationPanel;
import org.pz.polyglot.ui.components.ToolbarComponent;
import org.pz.polyglot.ui.components.TranslationTable;
import org.pz.polyglot.ui.models.TranslationEntryViewModel;
import org.pz.polyglot.ui.models.registries.TranslationEntryViewModelRegistry;
import org.pz.polyglot.ui.state.UIStateManager;

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
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem documentationMenuItem;
    @FXML
    private MenuItem discordMenuItem;
    @FXML
    private TranslationPanel translationPanel;
    @FXML
    private ToolbarComponent toolbarComponent;
    @FXML
    private TranslationTable translationTable;
    private ObservableList<TranslationEntryViewModel> allTableItems = FXCollections.observableArrayList();
    private final UIStateManager stateManager = UIStateManager.getInstance();

    /**
     * Initializes the TableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        quitMenuItem.setOnAction(event -> Platform.exit());
        setupRowSelectionListener();
        setupObservableBindings();
        setupFilterField();
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
}
