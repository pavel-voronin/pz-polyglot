package org.pz.polyglot.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import org.pz.polyglot.pz.translations.PZTranslations;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.ui.components.TranslationPanel;
import org.pz.polyglot.ui.components.ToolbarComponent;
import org.pz.polyglot.ui.models.TranslationEntryViewModel;
import org.pz.polyglot.ui.models.registries.TranslationEntryViewModelRegistry;
import org.pz.polyglot.ui.state.UIStateManager;
import org.pz.polyglot.ui.columns.ColumnManager;

import java.util.*;
import javafx.scene.control.*;

/**
 * Main controller for the Polyglot application.
 * Handles initialization and configuration of the main TreeTableView.
 */
public class MainController {
    @FXML
    private TreeTableView<TranslationEntryViewModel> treeTableView;
    @FXML
    private TextField filterField;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu helpMenu;
    @FXML
    private TreeTableColumn<TranslationEntryViewModel, String> keyColumn;
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
    private TreeItem<TranslationEntryViewModel> rootItem;
    private List<TreeItem<TranslationEntryViewModel>> allTableItems = new ArrayList<>();
    private ColumnManager columnManager;

    private final UIStateManager stateManager = UIStateManager.getInstance();

    /**
     * Initializes the TreeTableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        quitMenuItem.setOnAction(event -> Platform.exit());

        columnManager = new ColumnManager(treeTableView);

        setupRowSelectionListener();
        setupFilterField();
        setupObservableBindings();

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
                treeTableView.getSelectionModel().clearSelection();
            }
        });

        stateManager.refreshKeyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                refreshTableIndicatorsForKey(newVal);
            }
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
     * Sets up the row selection listener for the TreeTableView.
     */
    private void setupRowSelectionListener() {
        treeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getValue() != null) {
                stateManager.setSelectedTranslationKey(newSelection.getValue().getKey());
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
        columnManager.createColumns();

        keyColumn = columnManager.getKeyColumn();

        PZTranslations translations = PZTranslations.getInstance();
        Map<String, PZTranslationEntry> allTranslations = translations.getAllTranslations();

        TreeItem<TranslationEntryViewModel> root = new TreeItem<>(null);
        root.setExpanded(true);
        rootItem = root;
        allTableItems.clear();

        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            PZTranslationEntry translationEntry = entry.getValue();

            TranslationEntryViewModel entryViewModel = TranslationEntryViewModelRegistry.getViewModel(translationEntry);

            TreeItem<TranslationEntryViewModel> item = new TreeItem<>(entryViewModel);
            allTableItems.add(item);
            root.getChildren().add(item);
        }

        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);

        if (filterField != null && filterField.getText() != null && !filterField.getText().trim().isEmpty()) {
            filterTable(filterField.getText());
        }
    }

    /**
     * Sets up the filter field for filtering table rows by key name.
     */
    private void setupFilterField() {
        if (filterField != null) {
            filterField.setPromptText("Filter by key...");
            filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTable(newValue);
            });
        }
    }

    /**
     * Filters the table rows based on the filter text.
     */
    private void filterTable(String filterText) {
        if (rootItem == null || allTableItems.isEmpty())
            return;

        TreeTableColumn<TranslationEntryViewModel, ?> sortColumn = null;
        TreeTableColumn.SortType sortType = null;
        if (!treeTableView.getSortOrder().isEmpty()) {
            sortColumn = treeTableView.getSortOrder().get(0);
            sortType = sortColumn.getSortType();
        }

        treeTableView.getSelectionModel().clearSelection();

        rootItem.getChildren().clear();

        for (TreeItem<TranslationEntryViewModel> item : allTableItems) {
            if (filterText == null || filterText.trim().isEmpty()) {
                rootItem.getChildren().add(item);
            } else {
                String key = item.getValue().getKey();
                if (key.toLowerCase().contains(filterText.toLowerCase())) {
                    rootItem.getChildren().add(item);
                }
            }
        }

        if (sortColumn != null && sortType != null) {
            sortColumn.setSortType(sortType);
            treeTableView.getSortOrder().clear();
            treeTableView.getSortOrder().add(sortColumn);
            treeTableView.sort();
        }
    }

    /**
     * Refreshes the table indicators to show current state of translations and
     * changes.
     */
    public void refreshTableIndicators() {
        if (rootItem == null || allTableItems.isEmpty()) {
            return;
        }

        for (TreeItem<TranslationEntryViewModel> item : allTableItems) {
            TranslationEntryViewModel entryViewModel = item.getValue();
            if (entryViewModel != null) {
                entryViewModel.refresh();
            }
        }

        treeTableView.refresh();
    }

    /**
     * Refreshes the table indicators for a specific translation key.
     * This is more efficient than refreshing the entire table.
     */
    private void refreshTableIndicatorsForKey(String translationKey) {
        if (rootItem == null || allTableItems.isEmpty()) {
            return;
        }

        TreeItem<TranslationEntryViewModel> targetItem = null;
        for (TreeItem<TranslationEntryViewModel> item : allTableItems) {
            if (item.getValue().getKey().equals(translationKey)) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            return;
        }

        TranslationEntryViewModel entryViewModel = targetItem.getValue();
        if (entryViewModel != null) {
            entryViewModel.refresh();
            treeTableView.refresh();
        }
    }
}
