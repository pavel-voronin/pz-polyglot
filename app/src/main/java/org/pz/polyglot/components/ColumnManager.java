package org.pz.polyglot.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;

import org.pz.polyglot.AppConfig;
import org.pz.polyglot.State;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages TableView columns for translations.
 * Handles column creation, ordering, visibility, and configuration persistence.
 */
public class ColumnManager {
    private final TableView<TranslationEntryViewModel> tableView;
    private final State stateManager;
    private TableColumn<TranslationEntryViewModel, String> keyColumn;

    public ColumnManager(TableView<TranslationEntryViewModel> tableView) {
        this.tableView = tableView;
        this.stateManager = State.getInstance();
    }

    /**
     * Creates and configures all columns for the table.
     */
    public void createColumns() {
        tableView.getColumns().clear();

        // All available languages sorted with EN first
        List<String> allLanguages = getAllLanguagesInOrder();

        // Create key column
        createKeyColumn();

        // Create language columns - ONLY based on UIStateManager
        createLanguageColumns(allLanguages);

        // Disable standard column visibility control button
        tableView.setTableMenuButtonVisible(false);

        // Create header context menu
        createHeaderContextMenu(allLanguages);

        // Setup column reordering protection and listeners
        setupColumnOrderingProtection();
    }

    /**
     * Creates the key column.
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
     * Creates language columns with proper configuration.
     */
    private void createLanguageColumns(List<String> allLanguages) {
        // Get current visible languages from state manager - this is the ONLY source of
        // truth
        List<String> visibleLanguagesInOrder = new ArrayList<>(stateManager.getVisibleLanguages());

        // Fallback: if no visible languages configured, default to EN only
        // TODO: must be fixed in #18
        if (visibleLanguagesInOrder.isEmpty()) {
            visibleLanguagesInOrder.add("EN");
        }

        // First, create columns for visible languages in the saved order
        for (String lang : visibleLanguagesInOrder) {
            if (allLanguages.contains(lang)) {
                createLanguageColumn(lang, true);
            }
        }

        // Then, create columns for remaining languages (invisible)
        for (String lang : allLanguages) {
            if (!visibleLanguagesInOrder.contains(lang)) {
                createLanguageColumn(lang, false);
            }
        }
    }

    /**
     * Creates a single language column.
     */
    private void createLanguageColumn(String lang, boolean visible) {
        TableColumn<TranslationEntryViewModel, String> col = new TableColumn<>(lang);
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
        col.setReorderable(true);

        // Set visibility based on parameter
        col.setVisible(visible);

        // Listen for visibility changes to update state and save config
        col.visibleProperty().addListener((obs, oldV, newV) -> {
            updateVisibleLanguagesState();
            saveLanguageOrderToConfig();
        });
        tableView.getColumns().add(col);
    }

    /**
     * Creates header context menu for toggling column visibility.
     */
    private void createHeaderContextMenu(List<String> allLanguages) {
        ContextMenu headerMenu = new ContextMenu();

        for (String lang : allLanguages) {
            CheckMenuItem item = new CheckMenuItem(lang);
            // Find corresponding column for this language
            TableColumn<TranslationEntryViewModel, ?> langColumn = findLanguageColumn(lang);

            if (langColumn != null) {
                item.setSelected(langColumn.isVisible());
                // Sync column visibility with menu item
                final TableColumn<TranslationEntryViewModel, ?> finalLangColumn = langColumn;
                langColumn.visibleProperty().addListener((obs, oldV, newV) -> item.setSelected(newV));
                item.selectedProperty().addListener((obs, oldV, newV) -> finalLangColumn.setVisible(newV));
            }
            headerMenu.getItems().add(item);
        }

        // Attach context menu only to language column headers
        tableView.getColumns().stream()
                .filter(col -> col != keyColumn)
                .forEach(col -> col.setContextMenu(headerMenu));
    }

    /**
     * Sets up column ordering protection to keep Key column first.
     */
    private void setupColumnOrderingProtection() {
        tableView.getColumns()
                .addListener((ListChangeListener<TableColumn<TranslationEntryViewModel, ?>>) change -> {
                    while (change.next()) {
                        // Check for any type of change that might affect position 0
                        if (change.wasAdded() || change.wasRemoved() || change.wasPermutated()) {
                            // Check if Key column is still at position 0
                            if (!tableView.getColumns().isEmpty() &&
                                    tableView.getColumns().get(0) != keyColumn) {

                                // Find where the Key column is now
                                int keyColumnIndex = tableView.getColumns().indexOf(keyColumn);

                                if (keyColumnIndex > 0) {
                                    // Remove Key column from its current position and put it at position 0
                                    tableView.getColumns().remove(keyColumn);
                                    tableView.getColumns().add(0, keyColumn);
                                }
                            }

                            // Save the new language column order to config and update state
                            updateVisibleLanguagesState();
                            saveLanguageOrderToConfig();
                        }
                    }
                });
    }

    /**
     * Gets all available languages in fixed alphabetical order with EN first.
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
     * Finds a language column by language code.
     */
    private TableColumn<TranslationEntryViewModel, ?> findLanguageColumn(String lang) {
        for (TableColumn<TranslationEntryViewModel, ?> col : tableView.getColumns()) {
            if (col != keyColumn && col.getText().equals(lang)) {
                return col;
            }
        }
        return null;
    }

    /**
     * Saves the current language column order to config.
     */
    private void saveLanguageOrderToConfig() {
        // Save visible languages in the ACTUAL order they appear in the table
        List<String> visibleLanguagesInOrder = new ArrayList<>();

        for (TableColumn<TranslationEntryViewModel, ?> column : tableView.getColumns()) {
            // Skip the key column, only process language columns
            if (column != keyColumn && column.isVisible()) {
                visibleLanguagesInOrder.add(column.getText());
            }
        }

        AppConfig cfg = AppConfig.getInstance();
        cfg.setPzLanguages(visibleLanguagesInOrder.toArray(new String[0]));
        cfg.save();
    }

    /**
     * Updates the visible languages state in UIStateManager.
     */
    private void updateVisibleLanguagesState() {
        List<String> visibleLanguageCodes = tableView.getColumns().stream()
                .filter(col -> col != keyColumn && col.isVisible())
                .map(TableColumn::getText)
                .collect(Collectors.toList());

        stateManager.updateVisibleLanguages(visibleLanguageCodes);
    }

    /**
     * Gets the key column reference.
     */
    public TableColumn<TranslationEntryViewModel, String> getKeyColumn() {
        return keyColumn;
    }
}
