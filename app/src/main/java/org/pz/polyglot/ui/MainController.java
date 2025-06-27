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
import javafx.geometry.Insets;
import org.pz.polyglot.pz.translations.PZTranslations;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.pz.translations.PZTranslationVariant;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.pz.core.PZBuild;
import org.pz.polyglot.config.AppConfig;

import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CheckMenuItem;

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

        public TranslationRow(String key, Map<String, Boolean> languagePresence) {
            this.key = key;
            this.languagePresence = languagePresence;
        }

        public String getKey() {
            return key;
        }

        public boolean hasTranslation(String langCode) {
            return languagePresence.getOrDefault(langCode, false);
        }
    }

    @FXML
    private TreeTableView<TranslationRow> treeTableView;
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
    @FXML
    private javafx.scene.control.Label memoryLabel;

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

    /**
     * Initializes the TreeTableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        startMemoryMonitor();
        // Enable column visibility menu button
        treeTableView.setTableMenuButtonVisible(true);
        // Handle Quit menu action
        quitMenuItem.setOnAction(event -> Platform.exit());
        setupRowSelectionListener();
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
            VBox langContainer = new VBox(5);
            langContainer.setPadding(new Insets(5));
            langContainer.setStyle(
                    "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-color: #fafafa;");

            Label langLabel = new Label(langCode);
            langLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

            TextArea textArea = new TextArea();
            textArea.setPromptText("Enter translation for " + langCode);
            textArea.setPrefWidth(350);
            textArea.setPrefRowCount(2); // Start with 2 rows
            textArea.setWrapText(true); // Enable text wrapping

            // Make TextArea expand with content
            textArea.textProperty().addListener((obs, oldText, newText) -> {
                // Calculate approximate number of lines needed
                if (newText != null) {
                    int lines = Math.max(2, newText.split("\n").length);
                    // Add extra line if text wraps
                    if (newText.length() > 50 * lines) {
                        lines++;
                    }
                    textArea.setPrefRowCount(Math.min(lines, 10)); // Limit to 10 rows max
                }
            });

            // Find existing translation for this language
            if (entry != null) {
                for (PZTranslationVariant variant : entry.getTranslations()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            langCode.equals(variant.getFile().getLanguage().getCode()) &&
                            variant.getText() != null && !variant.getText().isEmpty()) {
                        textArea.setText(variant.getText());
                        break;
                    }
                }
            }

            languageTextFields.put(langCode, textArea);
            langContainer.getChildren().addAll(langLabel, textArea);
            languageFieldsContainer.getChildren().add(langContainer);
        }

        // Show the panel
        rightPanel.setVisible(true);
        rightPanel.setManaged(true);
        // Set default size for translation input fields
        languageTextFields.values().forEach(textArea -> {
            textArea.setPrefRowCount(1);
            textArea.setWrapText(true);
        });
    }

    /**
     * Closes the right panel.
     */
    @FXML
    private void closePanelAction() {
        rightPanel.setVisible(false);
        rightPanel.setManaged(false);
        languageTextFields.clear();
        treeTableView.getSelectionModel().clearSelection();
    }

    /**
     * Starts a timer to update the memory usage label in the status bar.
     */
    private void startMemoryMonitor() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> updateMemoryLabel()));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Updates the memory usage label with the current memory usage.
     */
    private void updateMemoryLabel() {
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long usedMB = used / (1024 * 1024);
        memoryLabel.setText("Memory: " + usedMB + " MB");
    }

    private void populateTranslationsTable() {
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
        PZLanguages pzLang = PZBuild.BUILD_42.getLanguages();
        Set<String> avail = pzLang.getAllLanguageCodes();
        List<String> allList = new ArrayList<>(avail);
        Collections.sort(allList);
        if (allList.remove("EN"))
            allList.add(0, "EN");
        // Determine creation order: config first, then missing
        List<String> order = new ArrayList<>();
        if (cfgList != null) {
            for (String code : cfgList)
                if (avail.contains(code))
                    order.add(code);
        } else {
            order.addAll(allList);
        }
        for (String code : allList) {
            if (cfgList == null || !cfgList.contains(code))
                order.add(code);
        }
        // Key column
        keyColumn = new TreeTableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getKey()));
        keyColumn.setPrefWidth(150);
        keyColumn.setReorderable(false);
        treeTableView.getColumns().add(keyColumn);
        // Language columns with visibility per config
        for (String lang : order) {
            TreeTableColumn<TranslationRow, String> col = new TreeTableColumn<>(lang);
            col.setCellValueFactory(param -> {
                boolean present = param.getValue().getValue().hasTranslation(lang);
                return new SimpleStringProperty(present ? "✔" : "");
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
        // Only include language columns in the menu
        for (TreeTableColumn<TranslationRow, ?> col : treeTableView.getColumns()) {
            if (col == keyColumn)
                continue;
            CheckMenuItem item = new CheckMenuItem(col.getText());
            item.setSelected(col.isVisible());
            // Sync column visibility with menu item
            col.visibleProperty().addListener((obs, oldV, newV) -> item.setSelected(newV));
            item.selectedProperty().addListener((obs, oldV, newV) -> col.setVisible(newV));
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
        TreeItem<TranslationRow> root = new TreeItem<>(new TranslationRow("Root", Collections.emptyMap()));
        root.setExpanded(true);
        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            String key = entry.getKey();
            PZTranslationEntry translationEntry = entry.getValue();
            Map<String, Boolean> langPresence = new HashMap<>();
            for (String lang : order) {
                boolean found = false;
                for (PZTranslationVariant variant : translationEntry.getTranslations()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            lang.equals(variant.getFile().getLanguage().getCode()) &&
                            variant.getText() != null && !variant.getText().isEmpty()) {
                        found = true;
                        break;
                    }
                }
                langPresence.put(lang, found);
            }
            root.getChildren().add(new TreeItem<>(new TranslationRow(key, langPresence)));
        }
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
    }

    public void refreshTranslationsTable() {
        populateTranslationsTable();
    }

    /**
     * Saves the current language column order to config.
     */
    private void saveLanguageOrderToConfig() {
        // Save only visible language columns in current order
        List<String> current = new ArrayList<>();
        for (TreeTableColumn<TranslationRow, ?> column : treeTableView.getColumns()) {
            if (column != keyColumn && column.isVisible()) {
                current.add(column.getText());
            }
        }
        AppConfig cfg = AppConfig.getInstance();
        cfg.setPzLanguages(current.toArray(new String[0]));
        cfg.save();
    }
}
