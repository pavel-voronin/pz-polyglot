package org.pz.polyglot.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import org.pz.polyglot.Logger;
import org.pz.polyglot.State;
import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;
import org.pz.polyglot.viewModels.registries.TranslationEntryViewModelRegistry;

import java.io.IOException;

/**
 * Component encapsulating the translation entries TableView and its logic.
 * Now subscribes directly to global State for all table-related events.
 */
public class TranslationTable extends TableView<TranslationEntryViewModel> {
    private ObservableList<TranslationEntryViewModel> backingList = FXCollections.observableArrayList();
    private FilteredList<TranslationEntryViewModel> filteredTableItems;
    private SortedList<TranslationEntryViewModel> sortedTableItems;
    private ObservableList<PZTranslationEntry> allEntries = FXCollections.observableArrayList();
    private ColumnManager columnManager;
    private String filterText = "";
    private final State stateManager = State.getInstance();

    public TranslationTable() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/TranslationTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        subscribeToState();
        setupTableVirtualization();

        SystemMonitor.addHook(() -> "backing: " + backingList.size());
        SystemMonitor.addHook(() -> "filtered: " + filteredTableItems.size());
        SystemMonitor.addHook(() -> "sorted: " + sortedTableItems.size());

        populateTranslationsTable();
    }

    private void setupTableVirtualization() {
        filteredTableItems = new FilteredList<>(backingList, p -> true);
        sortedTableItems = new SortedList<>(filteredTableItems);
        sortedTableItems.comparatorProperty().bind(comparatorProperty());
        setItems(sortedTableItems);
        setRowFactory(tableView -> new TableRow<>() {
            @Override
            protected void updateItem(TranslationEntryViewModel item, boolean empty) {
                super.updateItem(item, empty);
                // ...custom row logic if needed...
            }
        });
    }

    @FXML
    private void initialize() {
        columnManager = new ColumnManager(this);
        columnManager.createColumns();
        getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                stateManager.setSelectedTranslationKey(newSelection.getKey());
                stateManager.setRightPanelVisible(true);
            }
        });
    }

    private void subscribeToState() {
        stateManager.saveAllTriggeredProperty().addListener((obs, oldVal, newVal) -> refreshTableIndicators());
        stateManager.refreshKeyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                refreshTableIndicatorsForKey(newVal);
            } else {
                refreshTableIndicators();
            }
        });
        stateManager.tableRebuildRequiredProperty().addListener((obs, oldVal, newVal) -> populateTranslationsTable());
        stateManager.selectedTranslationKeyProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                getSelectionModel().clearSelection();
            } else {
                for (var item : filteredTableItems) {
                    if (item.getKey().equals(newVal)) {
                        getSelectionModel().select(item);
                        break;
                    }
                }
            }
        });
        stateManager.filterTextProperty().addListener((obs, oldVal, newVal) -> {
            filterText = newVal == null ? "" : newVal;
            applyFilter();
        });
        stateManager.selectedTypesChangedProperty()
                .addListener((obs, oldVal, newVal) -> applyFilter());
        stateManager.enabledSourcesChangedProperty()
                .addListener((obs, oldVal, newVal) -> applyFilter());
    }

    /**
     * Set the items to display in the table. This is the only way to set data.
     */
    public void setTableEntries(ObservableList<PZTranslationEntry> entries) {
        allEntries.setAll(entries);
        rebuildFilteredList();
    }

    private void rebuildFilteredList() {
        backingList.clear();
        for (var entry : allEntries) {
            backingList.add((TranslationEntryViewModel) TranslationEntryViewModelRegistry.getViewModel(entry));
        }
        applyFilter();
    }

    /**
     * Deprecated: use State.filterTextProperty instead.
     */
    @Deprecated
    public void setFilterText(String filterText) {
        this.filterText = filterText == null ? "" : filterText;
        applyFilter();
    }

    private void applyFilter() {
        Logger.info("Applying filter");
        var selectedTypes = stateManager.getSelectedTypes();
        var enabledSources = stateManager.getEnabledSources();

        filteredTableItems.setPredicate(
                item -> (filterText.isBlank() || item.getKey().toLowerCase().contains(filterText.toLowerCase()))
                        && (item.getTypes().isEmpty() || selectedTypes.contains(item.getType()))
                        && (item.getSources().isEmpty() ||
                                (!enabledSources.isEmpty()
                                        && item.getSources().stream().anyMatch(enabledSources::contains))));
    }

    public void populateTranslationsTable() {
        var translations = org.pz.polyglot.models.translations.PZTranslations.getInstance();
        allEntries.setAll(translations.getAllTranslations().values());
        rebuildFilteredList();
    }

    public void refreshTableIndicators() {
        for (TranslationEntryViewModel entryViewModel : filteredTableItems) {
            if (entryViewModel != null) {
                entryViewModel.refresh();
            }
        }
        refresh();
    }

    public void refreshTableIndicatorsForKey(String translationKey) {
        for (TranslationEntryViewModel item : filteredTableItems) {
            if (item.getKey().equals(translationKey)) {
                item.refresh();
                refresh();
                break;
            }
        }
    }

    public ColumnManager getColumnManager() {
        return columnManager;
    }
}
