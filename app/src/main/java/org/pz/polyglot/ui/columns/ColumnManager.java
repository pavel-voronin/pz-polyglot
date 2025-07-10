package org.pz.polyglot.ui.columns;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.ui.models.TranslationEntryViewModel;
import org.pz.polyglot.ui.state.UIStateManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages TreeTableView columns for translations.
 * Handles column creation, ordering, visibility, and configuration persistence.
 */
public class ColumnManager {
    private final TreeTableView<TranslationEntryViewModel> treeTableView;
    private final UIStateManager stateManager;
    private TreeTableColumn<TranslationEntryViewModel, String> keyColumn;

    public ColumnManager(TreeTableView<TranslationEntryViewModel> treeTableView) {
        this.treeTableView = treeTableView;
        this.stateManager = UIStateManager.getInstance();
    }

    /**
     * Creates and configures all columns for the table.
     */
    public void createColumns() {
        treeTableView.getColumns().clear();

        // All available languages sorted with EN first
        List<String> allLanguages = getAllLanguagesInOrder();

        // Create key column
        createKeyColumn();

        // Create language columns - ONLY based on UIStateManager
        createLanguageColumns(allLanguages);

        // Enable column visibility control button
        treeTableView.setTableMenuButtonVisible(true);

        // Create header context menu
        createHeaderContextMenu(allLanguages);

        // Setup column reordering protection and listeners
        setupColumnOrderingProtection();
    }

    /**
     * Creates the key column.
     */
    private void createKeyColumn() {
        keyColumn = new TreeTableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> {
            TranslationEntryViewModel viewModel = param.getValue().getValue();
            return new SimpleStringProperty(viewModel != null ? viewModel.getKey() : "");
        });
        keyColumn.setPrefWidth(150);
        keyColumn.setReorderable(false);
        treeTableView.getColumns().add(keyColumn);
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
        TreeTableColumn<TranslationEntryViewModel, String> col = new TreeTableColumn<>(lang);
        col.setCellValueFactory(param -> {
            TranslationEntryViewModel entryViewModel = param.getValue().getValue();
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
        treeTableView.getColumns().add(col);
    }

    /**
     * Creates header context menu for toggling column visibility.
     */
    private void createHeaderContextMenu(List<String> allLanguages) {
        ContextMenu headerMenu = new ContextMenu();

        for (String lang : allLanguages) {
            CheckMenuItem item = new CheckMenuItem(lang);
            // Find corresponding column for this language
            TreeTableColumn<TranslationEntryViewModel, ?> langColumn = findLanguageColumn(lang);

            if (langColumn != null) {
                item.setSelected(langColumn.isVisible());
                // Sync column visibility with menu item
                final TreeTableColumn<TranslationEntryViewModel, ?> finalLangColumn = langColumn;
                langColumn.visibleProperty().addListener((obs, oldV, newV) -> item.setSelected(newV));
                item.selectedProperty().addListener((obs, oldV, newV) -> finalLangColumn.setVisible(newV));
            }
            headerMenu.getItems().add(item);
        }

        // Attach context menu only to language column headers
        treeTableView.getColumns().stream()
                .filter(col -> col != keyColumn)
                .forEach(col -> col.setContextMenu(headerMenu));
    }

    /**
     * Sets up column ordering protection to keep Key column first.
     */
    private void setupColumnOrderingProtection() {
        treeTableView.getColumns()
                .addListener((ListChangeListener<TreeTableColumn<TranslationEntryViewModel, ?>>) change -> {
                    while (change.next()) {
                        // Check for any type of change that might affect position 0
                        if (change.wasAdded() || change.wasRemoved() || change.wasPermutated()) {
                            // Check if Key column is still at position 0
                            if (!treeTableView.getColumns().isEmpty() &&
                                    treeTableView.getColumns().get(0) != keyColumn) {

                                // Find where the Key column is now
                                int keyColumnIndex = treeTableView.getColumns().indexOf(keyColumn);

                                if (keyColumnIndex > 0) {
                                    // Remove Key column from its current position and put it at position 0
                                    treeTableView.getColumns().remove(keyColumn);
                                    treeTableView.getColumns().add(0, keyColumn);
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
    private TreeTableColumn<TranslationEntryViewModel, ?> findLanguageColumn(String lang) {
        for (TreeTableColumn<TranslationEntryViewModel, ?> col : treeTableView.getColumns()) {
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

        for (TreeTableColumn<TranslationEntryViewModel, ?> column : treeTableView.getColumns()) {
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
        List<String> visibleLanguageCodes = treeTableView.getColumns().stream()
                .filter(col -> col != keyColumn && col.isVisible())
                .map(TreeTableColumn::getText)
                .collect(Collectors.toList());

        stateManager.updateVisibleLanguages(visibleLanguageCodes);
    }

    /**
     * Gets the key column reference.
     */
    public TreeTableColumn<TranslationEntryViewModel, String> getKeyColumn() {
        return keyColumn;
    }
}
