package org.pz.polyglot.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import org.pz.polyglot.pz.translations.PZTranslations;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.pz.translations.PZTranslationVariant;
import org.pz.polyglot.pz.translations.PZTranslationManager;
import org.pz.polyglot.pz.translations.PZTranslationUpdatedVariants;
import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.ui.components.TranslationVariantField;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextField;

/**
 * Main controller for the Polyglot application.
 * Handles initialization and configuration of the main TreeTableView.
 */
public class MainController {
    /**
     * Model class representing a row in the tree table for translations.
     */
    public static class TranslationRow {
        private final String key;
        private final Map<String, Boolean> languagePresence;
        private final Map<String, Boolean> languageChanges;

        public TranslationRow(String key, Map<String, Boolean> languagePresence, Map<String, Boolean> languageChanges) {
            this.key = key;
            this.languagePresence = languagePresence;
            this.languageChanges = languageChanges;
        }

        public String getKey() {
            return key;
        }

        public boolean hasTranslation(String langCode) {
            return languagePresence.getOrDefault(langCode, false);
        }

        public boolean hasChanges(String langCode) {
            return languageChanges.getOrDefault(langCode, false);
        }
    }

    @FXML
    private TreeTableView<TranslationRow> treeTableView;
    @FXML
    private TextField filterField;
    @FXML
    private javafx.scene.control.MenuBar menuBar;
    @FXML
    private javafx.scene.control.Menu fileMenu;
    @FXML
    private javafx.scene.control.Menu helpMenu;
    @FXML
    private TreeTableColumn<TranslationRow, String> keyColumn; // Reference to the key column
    @FXML
    private javafx.scene.control.MenuItem quitMenuItem;
    @FXML
    private javafx.scene.control.MenuItem aboutMenuItem;
    @FXML
    private javafx.scene.control.MenuItem documentationMenuItem;
    @FXML
    private javafx.scene.control.MenuItem discordMenuItem;

    // Toolbar components
    @FXML
    private Button saveAllToolbarButton;

    // Right panel components
    @FXML
    private VBox rightPanel;
    @FXML
    private Label panelTitleLabel;
    @FXML
    private Button closePanelButton;
    @FXML
    private ScrollPane panelScrollPane;
    @FXML
    private VBox panelContent;
    @FXML
    private VBox languageFieldsContainer;

    // Current translation data
    private Map<String, TextArea> languageTextFields = new HashMap<>();
    private Map<TextArea, PZTranslationVariant> textAreaToVariant = new HashMap<>(); // Track which variant each text
                                                                                     // area represents
    // Track which text areas have been manually resized
    private Set<TextArea> manuallyResizedTextAreas = new HashSet<>();
    // Track variant field components
    private List<TranslationVariantField> variantFields = new ArrayList<>();
    // Save All button
    private Button saveAllButton;
    // Store root item for filtering
    private TreeItem<TranslationRow> rootItem;
    private List<TreeItem<TranslationRow>> allTableItems = new ArrayList<>();

    // Timer for periodic table indicator updates
    private javafx.animation.Timeline tableRefreshTimer;
    private final Set<String> keysNeedingRefresh = new HashSet<>();

    /**
     * Initializes the TreeTableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        // Handle Quit menu action
        quitMenuItem.setOnAction(event -> Platform.exit());
        setupRowSelectionListener();
        setupToolbarButtons();
        setupFilterField();
        loadCssStyles();
    }

    /**
     * Sets up the toolbar buttons and their actions.
     */
    private void setupToolbarButtons() {
        // Set up Save All toolbar button
        saveAllToolbarButton.setOnAction(e -> {
            PZTranslationManager.saveAll();
            // Update button state
            updateToolbarSaveAllButtonState();
            // Update individual variant buttons
            updateVariantButtons();
            // Refresh table indicators
            refreshTableIndicators();
        });

        // Initial state update
        updateToolbarSaveAllButtonState();
    }

    /**
     * Updates the state of the toolbar "Save All" button based on updated variants.
     */
    private void updateToolbarSaveAllButtonState() {
        PZTranslationUpdatedVariants updatedVariants = PZTranslationUpdatedVariants.getInstance();
        boolean hasUpdatedVariants = !updatedVariants.getVariants().isEmpty();
        saveAllToolbarButton.setDisable(!hasUpdatedVariants);
    }

    /**
     * Loads CSS styles for the application.
     */
    private void loadCssStyles() {
        // Load CSS when the scene is available
        Platform.runLater(() -> {
            if (treeTableView.getScene() != null) {
                String cssPath = getClass().getResource("/css/custom-textarea.css").toExternalForm();
                treeTableView.getScene().getStylesheets().addAll(cssPath);
            }
        });
    }

    /**
     * Sets up the row selection listener for the TreeTableView.
     */
    private void setupRowSelectionListener() {
        treeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getValue() != null) {
                showTranslationPanel(newSelection.getValue().getKey());
            }
        });
    }

    /**
     * Shows the right panel with translation details for the given key.
     */
    private void showTranslationPanel(String translationKey) {
        // Update panel title with just the key
        panelTitleLabel.setText(translationKey);

        // Clear previous fields
        languageFieldsContainer.getChildren().clear();
        languageTextFields.clear();
        textAreaToVariant.clear();
        variantFields.clear();

        // Get translation entry
        PZTranslations translations = PZTranslations.getInstance();
        PZTranslationEntry entry = translations.getAllTranslations().get(translationKey);

        // Determine languages in the order of current visible columns
        List<String> sortedLangCodes = treeTableView.getColumns().stream()
                .filter(col -> col != keyColumn && col.isVisible())
                .map(TreeTableColumn::getText)
                .collect(Collectors.toList());

        // Create fields for each language
        for (String langCode : sortedLangCodes) {
            PZLanguage lang = PZLanguages.getInstance().getLanguage(langCode).orElse(null);
            // Find all translation variants for this language
            List<PZTranslationVariant> languageVariants = new ArrayList<>();
            if (entry != null) {
                for (PZTranslationVariant variant : entry.getVariants()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            langCode.equals(variant.getFile().getLanguage().getCode())) {
                        languageVariants.add(variant);
                    }
                }
            }

            // Only create fields if variants exist
            if (!languageVariants.isEmpty()) {
                // Create fields for each variant of this language
                for (int i = 0; i < languageVariants.size(); i++) {
                    PZTranslationVariant variant = languageVariants.get(i);

                    // Create field key
                    String fieldKey = languageVariants.size() == 1 ? langCode : langCode + "_" + i;

                    // Create variant field component
                    TranslationVariantField variantField = new TranslationVariantField(
                            variant, entry, lang, translationKey, fieldKey);

                    // Set up callbacks
                    variantField.setOnStateChanged(() -> {
                        updateSaveAllButtonState(entry);
                        updateToolbarSaveAllButtonState();
                    });

                    variantField.setOnVariantChanged(key -> {
                        scheduleTableRefresh(key);
                    });

                    // Store references
                    variantFields.add(variantField);
                    languageTextFields.put(fieldKey, variantField.getTextArea());
                    textAreaToVariant.put(variantField.getTextArea(), variant);

                    // Add to container
                    languageFieldsContainer.getChildren().add(variantField);
                }
            }
        }

        // Create "Save All" button
        saveAllButton = new Button("Save All");
        saveAllButton.setStyle("-fx-padding: 10 20; -fx-font-size: 14px; -fx-pref-width: 150px;");
        saveAllButton.setOnAction(e -> {
            // Save all changes for this entry
            PZTranslationManager.saveEntry(entry);
            // Update button states after saving
            updateSaveAllButtonState(entry);
            updateToolbarSaveAllButtonState();
            // Update individual save/reset buttons
            updateVariantButtons();
            // Refresh table indicators for this specific key only
            refreshTableIndicatorsForKey(translationKey);
        });

        // Add some spacing before the button
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(20);

        // Add button to the container
        languageFieldsContainer.getChildren().addAll(spacer, saveAllButton);

        // Initial state update
        updateSaveAllButtonState(entry);

        // Show the panel
        rightPanel.setVisible(true);
        rightPanel.setManaged(true);
    }

    /**
     * Closes the right panel.
     */
    @FXML
    private void closePanelAction() {
        // Stop the table refresh timer
        if (tableRefreshTimer != null) {
            tableRefreshTimer.stop();
        }
        keysNeedingRefresh.clear();

        rightPanel.setVisible(false);
        rightPanel.setManaged(false);
        languageTextFields.clear();
        textAreaToVariant.clear();
        manuallyResizedTextAreas.clear();
        variantFields.clear();
        saveAllButton = null;
        treeTableView.getSelectionModel().clearSelection();
    }

    public void populateTranslationsTable() {
        // Refresh columns based on config and all available languages
        treeTableView.getColumns().clear();
        PZTranslations translations = PZTranslations.getInstance();
        Map<String, PZTranslationEntry> allTranslations = translations.getAllTranslations();
        // Load config languages order
        AppConfig config = AppConfig.getInstance();
        String[] cfgLangs = config.getPzLanguages();
        List<String> cfgList = (cfgLangs != null && cfgLangs.length > 0)
                ? new ArrayList<>(Arrays.asList(cfgLangs))
                : null;
        // All available languages sorted with EN first
        PZLanguages pzLang = PZLanguages.getInstance();
        Set<String> avail = pzLang.getAllLanguageCodes();
        List<String> allList = new ArrayList<>(avail);
        Collections.sort(allList);
        if (allList.remove("EN"))
            allList.add(0, "EN");
        // Key column
        keyColumn = new TreeTableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getKey()));
        keyColumn.setPrefWidth(150);
        keyColumn.setReorderable(false);
        treeTableView.getColumns().add(keyColumn);
        // Language columns with visibility per config - create ALL available languages
        // in alphabetical order
        for (String lang : allList) {
            TreeTableColumn<TranslationRow, String> col = new TreeTableColumn<>(lang);
            col.setCellValueFactory(param -> {
                boolean present = param.getValue().getValue().hasTranslation(lang);
                boolean hasChanges = param.getValue().getValue().hasChanges(lang);

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
            // Set visibility based on config
            col.setVisible(cfgList == null || cfgList.contains(lang));
            // Listen for visibility changes to save config
            col.visibleProperty().addListener((obs, oldV, newV) -> saveLanguageOrderToConfig());
            treeTableView.getColumns().add(col);
        }
        // Enable column visibility control button
        treeTableView.setTableMenuButtonVisible(true);

        // Create header context menu for toggling column visibility
        ContextMenu headerMenu = new ContextMenu();
        // Include ALL available languages in fixed alphabetical order (EN first)
        for (String lang : allList) {
            CheckMenuItem item = new CheckMenuItem(lang);
            // Find corresponding column for this language
            TreeTableColumn<TranslationRow, ?> langColumn = null;
            for (TreeTableColumn<TranslationRow, ?> col : treeTableView.getColumns()) {
                if (col != keyColumn && col.getText().equals(lang)) {
                    langColumn = col;
                    break;
                }
            }

            if (langColumn != null) {
                item.setSelected(langColumn.isVisible());
                // Sync column visibility with menu item
                final TreeTableColumn<TranslationRow, ?> finalLangColumn = langColumn;
                langColumn.visibleProperty().addListener((obs, oldV, newV) -> item.setSelected(newV));
                item.selectedProperty().addListener((obs, oldV, newV) -> finalLangColumn.setVisible(newV));
            }
            headerMenu.getItems().add(item);
        }
        // Attach context menu only to language column headers
        treeTableView.getColumns().stream()
                .filter(col -> col != keyColumn)
                .forEach(col -> col.setContextMenu(headerMenu));

        // Add listener to prevent any column from moving to position 0 except Key
        // column and save language order changes to config
        treeTableView.getColumns().addListener((ListChangeListener<TreeTableColumn<TranslationRow, ?>>) change -> {
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

                    // Save the new language column order to config
                    saveLanguageOrderToConfig();
                }
            }
        });

        // Build rows
        TreeItem<TranslationRow> root = new TreeItem<>(
                new TranslationRow("Root", Collections.emptyMap(), Collections.emptyMap()));
        root.setExpanded(true);
        rootItem = root; // Store reference for filtering
        allTableItems.clear(); // Clear previous items

        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            String key = entry.getKey();
            PZTranslationEntry translationEntry = entry.getValue();
            Map<String, Boolean> langPresence = new HashMap<>();
            Map<String, Boolean> langChanges = new HashMap<>();

            for (String lang : allList) {
                boolean found = false;
                boolean hasChanges = false;

                for (PZTranslationVariant variant : translationEntry.getVariants()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            lang.equals(variant.getFile().getLanguage().getCode())) {
                        if (variant.getOriginalText() != null && !variant.getOriginalText().isEmpty()) {
                            found = true;
                        }
                        if (variant.isChanged()) {
                            hasChanges = true;
                        }
                    }
                }
                langPresence.put(lang, found);
                langChanges.put(lang, hasChanges);
            }
            TreeItem<TranslationRow> item = new TreeItem<>(new TranslationRow(key, langPresence, langChanges));
            allTableItems.add(item); // Store all items for filtering
            root.getChildren().add(item);
        }

        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);

        // Apply current filter if any
        if (filterField != null && filterField.getText() != null && !filterField.getText().trim().isEmpty()) {
            filterTable(filterField.getText());
        }
    }

    /**
     * Saves the current language column order to config.
     */
    private void saveLanguageOrderToConfig() {
        // Get all available languages in their fixed alphabetical order (EN first)
        PZLanguages pzLang = PZLanguages.getInstance();
        Set<String> avail = pzLang.getAllLanguageCodes();
        List<String> allList = new ArrayList<>(avail);
        Collections.sort(allList);
        if (allList.remove("EN"))
            allList.add(0, "EN");

        // Save only visible languages in their fixed alphabetical order
        List<String> visibleLanguages = new ArrayList<>();
        for (String lang : allList) {
            for (TreeTableColumn<TranslationRow, ?> column : treeTableView.getColumns()) {
                if (column != keyColumn && column.getText().equals(lang) && column.isVisible()) {
                    visibleLanguages.add(lang);
                    break;
                }
            }
        }

        AppConfig cfg = AppConfig.getInstance();
        cfg.setPzLanguages(visibleLanguages.toArray(new String[0]));
        cfg.save();
    }

    /**
     * Updates the state of the "Save All" button based on whether there are changes
     * to save.
     */
    private void updateSaveAllButtonState(PZTranslationEntry entry) {
        if (saveAllButton != null && entry != null) {
            boolean hasChanges = entry.getChangedVariants().size() > 0;
            saveAllButton.setDisable(!hasChanges);
        }
    }

    /**
     * Updates the state of individual variant save/reset buttons.
     */
    private void updateVariantButtons() {
        for (TranslationVariantField variantField : variantFields) {
            variantField.updateVariantButtons();
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

        // Store current sort order
        TreeTableColumn<TranslationRow, ?> sortColumn = null;
        TreeTableColumn.SortType sortType = null;
        if (!treeTableView.getSortOrder().isEmpty()) {
            sortColumn = treeTableView.getSortOrder().get(0);
            sortType = sortColumn.getSortType();
        }

        // Clear current selection when filtering
        treeTableView.getSelectionModel().clearSelection();

        // Clear all children from root
        rootItem.getChildren().clear();

        // Add back only matching items
        for (TreeItem<TranslationRow> item : allTableItems) {
            if (filterText == null || filterText.trim().isEmpty()) {
                // Show all items when filter is empty
                rootItem.getChildren().add(item);
            } else {
                // Check if key contains the filter text (case-insensitive)
                String key = item.getValue().getKey();
                if (key.toLowerCase().contains(filterText.toLowerCase())) {
                    rootItem.getChildren().add(item);
                }
            }
        }

        // Restore sort order if it was set
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

        // Update the data for all table items
        for (TreeItem<TranslationRow> item : allTableItems) {
            String key = item.getValue().getKey();
            PZTranslations translations = PZTranslations.getInstance();
            PZTranslationEntry entry = translations.getAllTranslations().get(key);

            if (entry != null) {
                // Get all available languages
                PZLanguages pzLang = PZLanguages.getInstance();
                Set<String> avail = pzLang.getAllLanguageCodes();
                List<String> allList = new ArrayList<>(avail);
                Collections.sort(allList);
                if (allList.remove("EN")) {
                    allList.add(0, "EN");
                }

                Map<String, Boolean> langPresence = new HashMap<>();
                Map<String, Boolean> langChanges = new HashMap<>();

                for (String lang : allList) {
                    boolean found = false;
                    boolean hasChanges = false;

                    for (PZTranslationVariant variant : entry.getVariants()) {
                        if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                                lang.equals(variant.getFile().getLanguage().getCode())) {
                            if (variant.getOriginalText() != null && !variant.getOriginalText().isEmpty()) {
                                found = true;
                            }
                            if (variant.isChanged()) {
                                hasChanges = true;
                            }
                        }
                    }
                    langPresence.put(lang, found);
                    langChanges.put(lang, hasChanges);
                }

                // Create new TranslationRow with updated data
                TranslationRow updatedRow = new TranslationRow(key, langPresence, langChanges);
                item.setValue(updatedRow);
            }
        }

        // Force table refresh
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

        // Find the specific item to update
        TreeItem<TranslationRow> targetItem = null;
        for (TreeItem<TranslationRow> item : allTableItems) {
            if (item.getValue().getKey().equals(translationKey)) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            return;
        }

        // Update only this specific item
        PZTranslations translations = PZTranslations.getInstance();
        PZTranslationEntry entry = translations.getAllTranslations().get(translationKey);

        if (entry != null) {
            // Get all available languages
            PZLanguages pzLang = PZLanguages.getInstance();
            Set<String> avail = pzLang.getAllLanguageCodes();
            List<String> allList = new ArrayList<>(avail);
            Collections.sort(allList);
            if (allList.remove("EN")) {
                allList.add(0, "EN");
            }

            Map<String, Boolean> langPresence = new HashMap<>();
            Map<String, Boolean> langChanges = new HashMap<>();

            for (String lang : allList) {
                boolean found = false;
                boolean hasChanges = false;

                for (PZTranslationVariant variant : entry.getVariants()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            lang.equals(variant.getFile().getLanguage().getCode())) {
                        if (variant.getOriginalText() != null && !variant.getOriginalText().isEmpty()) {
                            found = true;
                        }
                        if (variant.isChanged()) {
                            hasChanges = true;
                        }
                    }
                }
                langPresence.put(lang, found);
                langChanges.put(lang, hasChanges);
            }

            // Create new TranslationRow with updated data
            TranslationRow updatedRow = new TranslationRow(translationKey, langPresence, langChanges);
            targetItem.setValue(updatedRow);

            // Refresh only the specific item
            treeTableView.refresh();
        }
    }

    /**
     * Schedules a key for table indicator refresh.
     * The actual refresh will happen after a short delay to avoid frequent updates.
     */
    private void scheduleTableRefresh(String translationKey) {
        keysNeedingRefresh.add(translationKey);

        // Stop existing timer if running
        if (tableRefreshTimer != null) {
            tableRefreshTimer.stop();
        }

        // Create new timer with 100 ms delay
        tableRefreshTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), event -> {
                    // Refresh indicators for all keys that need it
                    for (String key : keysNeedingRefresh) {
                        refreshTableIndicatorsForKey(key);
                    }
                    keysNeedingRefresh.clear();
                }));
        tableRefreshTimer.play();
    }
}
