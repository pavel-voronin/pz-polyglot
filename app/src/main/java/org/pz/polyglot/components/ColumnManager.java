package org.pz.polyglot.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.pz.polyglot.Config;
import org.pz.polyglot.State;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;

/**
 * Manages TableView columns for translations.
 * Handles column creation, ordering, visibility, and configuration persistence.
 */
/**
 * Manages TableView columns for translations.
 * Handles column creation, ordering, visibility, and configuration persistence.
 */
public class ColumnManager {
    /**
     * The TableView instance managed by this class.
     */
    private final TableView<TranslationEntryViewModel> tableView;

    /**
     * The state manager for UI and configuration state.
     */
    private final State stateManager;

    /**
     * Reference to the key column (always first).
     */
    private TableColumn<TranslationEntryViewModel, String> keyColumn;

    /**
     * Flag to prevent circular updates when changing column visibility
     * programmatically.
     */
    private boolean updatingColumnVisibility = false;

    /**
     * Constructs a ColumnManager for the given TableView.
     * 
     * @param tableView the TableView to manage
     */
    public ColumnManager(TableView<TranslationEntryViewModel> tableView) {
        this.tableView = tableView;
        this.stateManager = State.getInstance();
    }

    /**
     * Creates and configures all columns for the table, including key and language
     * columns,
     * header context menus, and listeners for column ordering and visibility.
     */
    public void createColumns() {
        tableView.getColumns().clear();

        // All available languages sorted with EN first
        List<String> allLanguages = getAllLanguagesInOrder();

        createKeyColumn();
        createLanguageColumns(allLanguages);
        tableView.setTableMenuButtonVisible(false);
        createHeaderContextMenu(allLanguages);
        setupColumnOrderingProtection();
        setupVisibleLanguagesListener();
    }

    /**
     * Sets up a listener for changes in the visible languages list to update column
     * visibility accordingly.
     */
    private void setupVisibleLanguagesListener() {
        stateManager.getVisibleLanguages().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                updateColumnVisibility();
            }
        });
    }

    /**
     * Updates column visibility based on the current visible languages state.
     * Ensures columns are shown/hidden and added as needed, and prevents circular
     * updates.
     */
    private void updateColumnVisibility() {
        List<String> visibleLanguages = new ArrayList<>(stateManager.getVisibleLanguages());
        updatingColumnVisibility = true;
        try {
            // Track existing language columns by their id
            Set<String> existingLanguageColumns = new HashSet<>();
            for (TableColumn<TranslationEntryViewModel, ?> column : tableView.getColumns()) {
                if (column != keyColumn && column.getId() != null) {
                    existingLanguageColumns.add(column.getId());
                }
            }

            // Update visibility for existing language columns
            List<TableColumn<TranslationEntryViewModel, ?>> columnsCopy = new ArrayList<>(tableView.getColumns());
            for (TableColumn<TranslationEntryViewModel, ?> column : columnsCopy) {
                if (column != keyColumn && column.getId() != null) {
                    String languageCode = column.getId();
                    boolean shouldBeVisible = visibleLanguages.contains(languageCode);
                    if (column.isVisible() != shouldBeVisible) {
                        column.setVisible(shouldBeVisible);
                        // Move newly visible column to the end
                        if (shouldBeVisible) {
                            tableView.getColumns().remove(column);
                            tableView.getColumns().add(column);
                        }
                    }
                }
            }

            // Add columns for languages that do not yet have a column
            for (String languageCode : visibleLanguages) {
                if (!existingLanguageColumns.contains(languageCode)) {
                    createLanguageColumnAtEnd(languageCode, true);
                }
            }
        } finally {
            updatingColumnVisibility = false;
        }
    }

    /**
     * Creates the key column and adds it to the table as the first column.
     */
    private void createKeyColumn() {
        keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> {
            TranslationEntryViewModel viewModel = param.getValue();
            return new SimpleStringProperty(viewModel != null ? viewModel.getKey() : "");
        });
        keyColumn.setPrefWidth(150);
        keyColumn.setReorderable(false);
        tableView.getColumns().add(keyColumn);
    }

    /**
     * Creates columns for all languages, first for visible languages in saved
     * order,
     * then for remaining (invisible) languages.
     * 
     * @param allLanguages all available language codes
     */
    private void createLanguageColumns(List<String> allLanguages) {
        List<String> visibleLanguagesInOrder = new ArrayList<>(stateManager.getVisibleLanguages());
        for (String lang : visibleLanguagesInOrder) {
            if (allLanguages.contains(lang)) {
                createLanguageColumn(lang, true);
            }
        }
        for (String lang : allLanguages) {
            if (!visibleLanguagesInOrder.contains(lang)) {
                createLanguageColumn(lang, false);
            }
        }
    }

    /**
     * Creates a single language column with custom header and visibility.
     * 
     * @param lang    language code
     * @param visible whether the column should be visible
     */
    private void createLanguageColumn(String lang, boolean visible) {
        TableColumn<TranslationEntryViewModel, String> col = new TableColumn<>(lang);
        col.setId(lang);
        col.setCellValueFactory(param -> {
            TranslationEntryViewModel entryViewModel = param.getValue();
            if (entryViewModel == null) {
                return new SimpleStringProperty("");
            }

            boolean present = entryViewModel.hasTranslationForLanguage(lang);
            boolean hasChanges = entryViewModel.hasChangesForLanguage(lang);

            String content = "";
            if (present) {
                content += "✔";
            }
            if (hasChanges) {
                content += " ●"; // Bullet point to indicate changes
            }
            return new SimpleStringProperty(content);
        });
        col.setPrefWidth(60);
        col.setMinWidth(48);
        col.setReorderable(true);

        col.setVisible(visible);
        col.setGraphic(createLanguageHeaderBox(lang));
        col.setText("");
        // Listen for visibility changes to update state and config only if not
        // programmatic
        col.visibleProperty().addListener((obs, oldV, newV) -> {
            if (!updatingColumnVisibility) {
                updateVisibleLanguagesState();
                saveLanguageOrderToConfig();
            }
        });
        tableView.getColumns().add(col);
    }

    /**
     * Creates a single language column and adds it at the end, used for dynamic
     * addition.
     * 
     * @param lang    language code
     * @param visible whether the column should be visible
     */
    private void createLanguageColumnAtEnd(String lang, boolean visible) {
        TableColumn<TranslationEntryViewModel, String> col = new TableColumn<>(lang);
        col.setId(lang);
        col.setCellValueFactory(param -> {
            TranslationEntryViewModel entryViewModel = param.getValue();
            if (entryViewModel == null) {
                return new SimpleStringProperty("");
            }

            boolean present = entryViewModel.hasTranslationForLanguage(lang);
            boolean hasChanges = entryViewModel.hasChangesForLanguage(lang);

            String content = "";
            if (present) {
                content += "✔";
            }
            if (hasChanges) {
                content += " ●"; // Bullet point to indicate changes
            }
            return new SimpleStringProperty(content);
        });
        col.setPrefWidth(60);
        col.setMinWidth(48);
        col.setReorderable(true);

        col.setVisible(visible);
        col.setGraphic(createLanguageHeaderBox(lang));
        col.setText("");
        col.visibleProperty().addListener((obs, oldV, newV) -> {
            if (!updatingColumnVisibility) {
                updateVisibleLanguagesState();
                saveLanguageOrderToConfig();
            }
        });
        // Add after all currently visible columns (after Key column)
        int targetPosition = 1;
        for (TableColumn<TranslationEntryViewModel, ?> column : tableView.getColumns()) {
            if (column != keyColumn && column.isVisible()) {
                targetPosition++;
            }
        }
        tableView.getColumns().add(targetPosition, col);
    }

    /**
     * Creates header context menus for each language column to allow toggling
     * visibility.
     * 
     * @param allLanguages all available language codes
     */
    private void createHeaderContextMenu(List<String> allLanguages) {
        for (TableColumn<TranslationEntryViewModel, ?> column : tableView.getColumns()) {
            if (column != keyColumn) {
                ContextMenu headerMenu = new ContextMenu();
                for (String lang : allLanguages) {
                    CheckMenuItem item = new CheckMenuItem(lang);
                    TableColumn<TranslationEntryViewModel, ?> langColumn = findLanguageColumn(lang);
                    if (langColumn != null) {
                        item.setSelected(langColumn.isVisible());
                        // Sync menu item with column visibility
                        final TableColumn<TranslationEntryViewModel, ?> finalLangColumn = langColumn;
                        langColumn.visibleProperty().addListener((obs, oldV, newV) -> item.setSelected(newV));
                        item.selectedProperty().addListener((obs, oldV, newV) -> {
                            if (newV && !finalLangColumn.isVisible()) {
                                finalLangColumn.setVisible(true);
                                int targetIndex = tableView.getColumns().indexOf(column);
                                tableView.getColumns().remove(finalLangColumn);
                                tableView.getColumns().add(targetIndex, finalLangColumn);
                            } else {
                                finalLangColumn.setVisible(newV);
                            }
                        });
                    }
                    headerMenu.getItems().add(item);
                }
                column.setContextMenu(headerMenu);
            }
        }
    }

    /**
     * Ensures the key column always remains at position 0 and saves language order
     * when columns are reordered.
     */
    private void setupColumnOrderingProtection() {
        tableView.getColumns()
                .addListener((ListChangeListener<TableColumn<TranslationEntryViewModel, ?>>) change -> {
                    while (change.next()) {
                        if (change.wasAdded() || change.wasRemoved() || change.wasPermutated()) {
                            // Ensure key column is always first
                            if (!tableView.getColumns().isEmpty() &&
                                    tableView.getColumns().get(0) != keyColumn) {
                                int keyColumnIndex = tableView.getColumns().indexOf(keyColumn);
                                if (keyColumnIndex > 0) {
                                    tableView.getColumns().remove(keyColumn);
                                    tableView.getColumns().add(0, keyColumn);
                                }
                            }
                            // Save state and config if not a programmatic update
                            if (!updatingColumnVisibility) {
                                updateVisibleLanguagesState();
                                saveLanguageOrderToConfig();
                            }
                        }
                    }
                });
    }

    /**
     * Returns all available language codes in alphabetical order, with EN first.
     * 
     * @return ordered list of language codes
     */
    private List<String> getAllLanguagesInOrder() {
        PZLanguages pzLang = PZLanguages.getInstance();
        Set<String> avail = pzLang.getAllLanguageCodes();
        List<String> allList = new ArrayList<>(avail);
        Collections.sort(allList);
        if (allList.remove("EN")) {
            allList.add(0, "EN");
        }
        return allList;
    }

    /**
     * Finds a language column by its language code.
     * 
     * @param lang language code
     * @return TableColumn for the language, or null if not found
     */
    private TableColumn<TranslationEntryViewModel, ?> findLanguageColumn(String lang) {
        for (TableColumn<TranslationEntryViewModel, ?> col : tableView.getColumns()) {
            if (col != keyColumn && lang.equals(col.getId())) {
                return col;
            }
        }
        return null;
    }

    /**
     * Saves the current order of visible language columns to configuration.
     */
    private void saveLanguageOrderToConfig() {
        List<String> visibleLanguagesInOrder = new ArrayList<>();
        for (TableColumn<TranslationEntryViewModel, ?> column : tableView.getColumns()) {
            if (column != keyColumn && column.isVisible() && column.getId() != null) {
                visibleLanguagesInOrder.add(column.getId());
            }
        }
        Config cfg = Config.getInstance();
        cfg.setPzLanguages(visibleLanguagesInOrder.toArray(new String[0]));
    }

    /**
     * Updates the visible languages state in the state manager based on current
     * table columns.
     */
    private void updateVisibleLanguagesState() {
        List<String> visibleLanguageCodes = tableView.getColumns().stream()
                .filter(col -> col != keyColumn && col.isVisible() && col.getId() != null)
                .map(TableColumn::getId)
                .collect(Collectors.toList());
        stateManager.updateVisibleLanguages(visibleLanguageCodes);
    }

    /**
     * Returns the key column reference.
     * 
     * @return key TableColumn
     */
    public TableColumn<TranslationEntryViewModel, String> getKeyColumn() {
        return keyColumn;
    }

    /**
     * Creates a header box for a language column, containing a label and a filter
     * toggle button.
     * The filter button allows filtering rows by the presence of values in this
     * column.
     * 
     * @param lang language code
     * @return HBox containing the header UI
     */
    private javafx.scene.layout.HBox createLanguageHeaderBox(String lang) {
        javafx.scene.control.Label headerLabel = new javafx.scene.control.Label(lang);
        headerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        headerLabel.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.ToggleButton filterButton = new javafx.scene.control.ToggleButton("F");
        filterButton.setMaxSize(20, 20);
        filterButton.setMinSize(20, 20);
        filterButton.setFocusTraversable(false);
        filterButton.setPadding(javafx.geometry.Insets.EMPTY);
        filterButton.setTooltip(new javafx.scene.control.Tooltip("Filter: show only rows with values in this column"));

        // Set initial state from filtered languages
        filterButton.setSelected(stateManager.getFilteredLanguages().contains(lang));

        // Update filtered languages when toggled
        filterButton.selectedProperty().addListener((obs, oldV, newV) -> {
            var filtered = new java.util.ArrayList<>(stateManager.getFilteredLanguages());
            if (newV) {
                if (!filtered.contains(lang) && stateManager.getVisibleLanguages().contains(lang)) {
                    filtered.add(lang);
                }
            } else {
                filtered.remove(lang);
            }
            stateManager.updateFilteredLanguages(filtered);
        });

        // Sync button state with filtered languages
        stateManager.getFilteredLanguages().addListener((javafx.collections.ListChangeListener<String>) change -> {
            filterButton.setSelected(stateManager.getFilteredLanguages().contains(lang));
        });

        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox(5);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.HBox.setHgrow(headerLabel, javafx.scene.layout.Priority.ALWAYS);
        headerBox.getChildren().addAll(headerLabel, filterButton);
        return headerBox;
    }
}
