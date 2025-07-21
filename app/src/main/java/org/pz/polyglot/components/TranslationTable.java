package org.pz.polyglot.components;

import java.util.Objects;
import java.util.Set;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import org.pz.polyglot.Logger;
import org.pz.polyglot.State;
import org.pz.polyglot.models.TranslationSession;
import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.models.translations.PZTranslations;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;
import org.pz.polyglot.viewModels.registries.TranslationEntryViewModelRegistry;

/**
 * TableView component for displaying and managing translation entries.
 * Subscribes to global State for all table-related events and updates.
 */
public class TranslationTable extends TableView<TranslationEntryViewModel> {
    /**
     * Backing list for all translation entry view models.
     */
    private final ObservableList<TranslationEntryViewModel> backingList = FXCollections.observableArrayList();

    /**
     * Filtered list for table virtualization and filtering.
     */
    private FilteredList<TranslationEntryViewModel> filteredTableItems;

    /**
     * Sorted list for table virtualization and sorting.
     */
    private SortedList<TranslationEntryViewModel> sortedTableItems;

    /**
     * All translation entries loaded from the model.
     */
    private final ObservableList<PZTranslationEntry> allEntries = FXCollections.observableArrayList();

    /**
     * Manages table columns and their configuration.
     */
    private ColumnManager columnManager;

    /**
     * Current filter text applied to the table.
     */
    private String filterText = "";

    /**
     * Reference to the global state manager singleton.
     */
    private final State stateManager = State.getInstance();

    /**
     * Constructs the TranslationTable and initializes FXML, state subscriptions,
     * and table data.
     */
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
        populateTranslationsTable();
    }

    /**
     * Sets up table virtualization, sorting, filtering, and row context menu.
     * Also listens for selection changes to update global state.
     */
    private void setupTableVirtualization() {
        filteredTableItems = new FilteredList<>(backingList, p -> true);
        sortedTableItems = new SortedList<>(filteredTableItems);
        sortedTableItems.comparatorProperty().bind(comparatorProperty());
        setItems(sortedTableItems);
        setRowFactory(tableView -> {
            TableRow<TranslationEntryViewModel> row = new TableRow<>() {
                @Override
                protected void updateItem(TranslationEntryViewModel item, boolean empty) {
                    super.updateItem(item, empty);
                }
            };

            // Context menu for copying the translation key to clipboard
            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyKeyItem = new MenuItem("Copy key");
            copyKeyItem.setOnAction(event -> {
                TranslationEntryViewModel item = row.getItem();
                if (item != null) {
                    var clipboard = Clipboard.getSystemClipboard();
                    var content = new ClipboardContent();
                    content.putString(item.getKey());
                    clipboard.setContent(content);
                }
            });
            contextMenu.getItems().add(copyKeyItem);

            row.setOnMouseClicked(event -> {
                // Show context menu on right mouse button click
                if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                    row.setContextMenu(contextMenu);
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    event.consume();
                } else if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() != MouseButton.SECONDARY) {
                    // Add key to sessionKeys on double click (any button except right)
                    var item = row.getItem();
                    if (item != null) {
                        TranslationSession.getInstance().addSessionKey(item.getKey());
                    }
                }
            });

            return row;
        });

        // Listen for selection changes to support keyboard navigation and update state
        getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                String newKey = newItem.getKey();
                stateManager.setSelectedTranslationKey(newKey);
                stateManager.setRightPanelVisible(true);
            }
        });
    }

    /**
     * Initializes the table columns using ColumnManager. Called by FXML.
     */
    @FXML
    private void initialize() {
        columnManager = new ColumnManager(this);
        columnManager.createColumns();
    }

    /**
     * Subscribes to global state properties to update the table when relevant
     * events occur.
     * Handles table refresh, selection, filtering, and rebuild events.
     */
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
                var selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem == null || !Objects.equals(selectedItem.getKey(), newVal)) {
                    for (var item : filteredTableItems) {
                        if (item.getKey().equals(newVal)) {
                            getSelectionModel().select(item);
                            break;
                        }
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
        stateManager.getFilteredLanguages()
                .addListener((ListChangeListener<? super String>) change -> applyFilter());
    }

    /**
     * Sets the items to display in the table. This is the only way to set data.
     * 
     * @param entries Observable list of translation entries to display
     */
    public void setTableEntries(ObservableList<PZTranslationEntry> entries) {
        allEntries.setAll(entries);
        rebuildFilteredList();
    }

    /**
     * Rebuilds the backing list from all entries and applies the current filter.
     */
    private void rebuildFilteredList() {
        backingList.clear();
        for (var entry : allEntries) {
            backingList.add((TranslationEntryViewModel) TranslationEntryViewModelRegistry.getViewModel(entry));
        }
        applyFilter();
    }

    /**
     * Applies the current filter to the table items based on text, type, source,
     * and language.
     * <p>
     * Filtering logic:
     * <ul>
     * <li>- If filter text is blank, all items match.</li>
     * <li>- If filtered languages are set, only items containing all filtered
     * languages
     * are shown, unless the key is new.</li>
     * <li>- Items are also filtered by type and source.</li>
     * </ul>
     */
    private void applyFilter() {
        Logger.info("Applying filter");
        var selectedTypes = stateManager.getSelectedTypes();
        var enabledSources = stateManager.getEnabledSources();

        // Filter by filteredLanguages (subset of visibleLanguages)
        var filteredLanguages = stateManager.getFilteredLanguages();
        filteredTableItems.setPredicate(
                item -> {
                    boolean matchesText = filterText.isBlank()
                            || item.getKey().toLowerCase().contains(filterText.toLowerCase())
                            || item.getVariantViewModels().stream()
                                    .anyMatch(variant -> {
                                        String text = variant.getVariant().getEditedText();
                                        return text != null && text.toLowerCase().contains(filterText.toLowerCase());
                                    });
                    boolean matchesType = item.getTypes().isEmpty() || selectedTypes.contains(item.getType());
                    boolean matchesSource = item.getSources().isEmpty() ||
                            (!enabledSources.isEmpty()
                                    && item.getSources().stream().anyMatch(enabledSources::contains));

                    // New key: no variants at all
                    boolean isNewKey = item.getVariantViewModels().isEmpty();

                    if (filteredLanguages.isEmpty()) {
                        return matchesText && matchesType && matchesSource;
                    }

                    // If any filtered language is missing, exclude unless new key
                    Set<String> presentLanguages = item.getLanguages();
                    boolean hasAllFiltered = filteredLanguages.stream().allMatch(presentLanguages::contains);
                    return (matchesText && matchesType && matchesSource && (hasAllFiltered || isNewKey));
                });
    }

    /**
     * Populates the table with all translation entries from the model.
     */
    public void populateTranslationsTable() {
        var translations = PZTranslations.getInstance();
        allEntries.setAll(translations.getAllTranslations().values());
        rebuildFilteredList();
    }

    /**
     * Refreshes all table indicators for visible items.
     */
    public void refreshTableIndicators() {
        for (TranslationEntryViewModel entryViewModel : filteredTableItems) {
            if (entryViewModel != null) {
                entryViewModel.refresh();
            }
        }
        refresh();
    }

    /**
     * Refreshes table indicators for a specific translation key.
     * 
     * @param translationKey the key to refresh
     */
    public void refreshTableIndicatorsForKey(String translationKey) {
        for (TranslationEntryViewModel item : filteredTableItems) {
            if (item.getKey().equals(translationKey)) {
                item.refresh();
                refresh();
                break;
            }
        }
    }

    /**
     * Returns the column manager for this table.
     * 
     * @return the column manager
     */
    public ColumnManager getColumnManager() {
        return columnManager;
    }
}
