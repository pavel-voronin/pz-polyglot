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
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import org.pz.polyglot.pz.translations.PZTranslations;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.pz.translations.PZTranslationVariant;
import org.pz.polyglot.pz.translations.PZTranslationManager;
import org.pz.polyglot.pz.translations.PZTranslationUpdatedVariants;
import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.ui.components.LanguageTag;

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
    @FXML
    private javafx.scene.control.Label memoryLabel;

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
    // Save All button
    private Button saveAllButton;
    // Store root item for filtering
    private TreeItem<TranslationRow> rootItem;
    private List<TreeItem<TranslationRow>> allTableItems = new ArrayList<>();

    /**
     * Initializes the TreeTableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        startMemoryMonitor();
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
                    String sourceName = variant.getFile().getSource().getName();

                    // Create horizontal container for language tag and reset button
                    HBox labelContainer = new HBox(10);
                    labelContainer.setPadding(new Insets(10, 0, 0, 0));
                    labelContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    // Create language tag
                    LanguageTag langTag = new LanguageTag(lang);

                    // Get charset info for source label
                    String detectedCharsetName = variant.getUsedCharset() != null
                            ? variant.getUsedCharset().name()
                            : "Unknown";
                    String supposedCharsetName = variant.getSupposedCharset() != null
                            ? variant.getSupposedCharset().name()
                            : "Unknown";

                    // Add source name and charset info as text (like before)
                    Label sourceLabel = new Label("(" + sourceName + ", " + detectedCharsetName
                            + (detectedCharsetName.equals(supposedCharsetName) ? "" : " *") + ")");
                    sourceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

                    // Make source label truncate text with ellipsis when too long
                    sourceLabel.setMaxWidth(240); // Set maximum width
                    sourceLabel.setMinWidth(0); // Allow shrinking
                    sourceLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

                    // Create reset link (styled as hyperlink)
                    Hyperlink resetLink = new Hyperlink("reset");
                    resetLink.setStyle("-fx-font-size: 12px; -fx-padding: 0; -fx-text-fill: #007acc;");
                    resetLink.setVisible(variant.isChanged()); // Initially visible only if already edited
                    resetLink.setMinWidth(Region.USE_PREF_SIZE); // Prevent shrinking

                    // Create save link (styled as hyperlink)
                    Hyperlink saveLink = new Hyperlink("save");
                    saveLink.setStyle("-fx-font-size: 12px; -fx-padding: 0; -fx-text-fill: #007acc;");
                    saveLink.setVisible(variant.isChanged()); // Initially visible only if already edited
                    saveLink.setMinWidth(Region.USE_PREF_SIZE); // Prevent shrinking

                    // Add language tag, source info and spacer, then buttons to push them to the
                    // right
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    labelContainer.getChildren().addAll(langTag, sourceLabel, spacer, saveLink, resetLink);

                    StackPane textAreaContainer = createResizableTextArea(langCode + "_" + i);
                    TextArea textArea = (TextArea) textAreaContainer.getChildren().get(0);
                    textArea.setText(variant.getEditedText());

                    // Track the variant for this text area
                    textAreaToVariant.put(textArea, variant);

                    // Set up reset functionality
                    resetLink.setOnAction(e -> {
                        variant.reset();
                        textArea.setText(variant.getEditedText());
                        resetLink.setVisible(false);
                        saveLink.setVisible(false);
                        // Update "Save All" button state
                        updateSaveAllButtonState(entry);
                        updateToolbarSaveAllButtonState();
                    });

                    // Set up save functionality
                    saveLink.setOnAction(e -> {
                        // Get the current text from the text area
                        String currentText = textArea.getText();
                        // Update the variant with the current text
                        variant.setEditedText(currentText);
                        // Save the variant to file
                        PZTranslationManager.saveVariant(variant);
                        // Hide the save link after successful save
                        saveLink.setVisible(false);
                        // Reset button should still be visible if text differs from original
                        resetLink.setVisible(false);
                        // Update "Save All" button state
                        updateSaveAllButtonState(entry);
                        updateToolbarSaveAllButtonState();
                    });

                    // Track text changes and show/hide reset and save buttons
                    textArea.textProperty().addListener((obs, oldText, newText) -> {
                        if (newText != null && !newText.equals(variant.getOriginalText())) {
                            variant.setEditedText(newText);
                            resetLink.setVisible(true);
                            saveLink.setVisible(true);
                        } else if (newText != null && newText.equals(variant.getOriginalText())) {
                            variant.reset();
                            resetLink.setVisible(false);
                            saveLink.setVisible(false);
                        }
                        // Update "Save All" button state
                        updateSaveAllButtonState(entry);
                        updateToolbarSaveAllButtonState();
                    });

                    // Store with unique key for multiple variants
                    String fieldKey = languageVariants.size() == 1 ? langCode : langCode + "_" + i;
                    languageTextFields.put(fieldKey, textArea);
                    languageFieldsContainer.getChildren().addAll(labelContainer, textAreaContainer);
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
        rightPanel.setVisible(false);
        rightPanel.setManaged(false);
        languageTextFields.clear();
        textAreaToVariant.clear();
        manuallyResizedTextAreas.clear();
        saveAllButton = null;
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

    /**
     * Creates a resizable TextArea with manual resize handle.
     */
    private StackPane createResizableTextArea(String langCode) {
        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter translation for " + langCode);
        textArea.setPrefWidth(420);
        textArea.setWrapText(true);

        // Set precise initial height - exactly one line
        textArea.setPrefHeight(28); // Fixed pixel height for single line
        textArea.setMinHeight(28);
        textArea.setMaxHeight(Region.USE_PREF_SIZE);
        textArea.setMinWidth(200);

        // Apply CSS class instead of inline styles
        textArea.getStyleClass().add("custom-textarea");

        // Smart auto-resize using pixel-based calculation
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            // Only auto-resize if not manually resized
            if (!manuallyResizedTextAreas.contains(textArea)) {
                Platform.runLater(() -> {
                    if (newText == null || newText.isEmpty()) {
                        textArea.setPrefHeight(28);
                        textArea.setMaxHeight(28);
                    } else {
                        // Calculate height based on text content
                        int lineBreaks = newText.split("\n", -1).length;

                        // Estimate wrapped lines based on character count and width
                        double charWidth = 7.5; // Average character width in pixels
                        double availableWidth = 410; // Text area width minus padding
                        int charsPerLine = (int) (availableWidth / charWidth);

                        int wrappedLines = 0;
                        String[] textLines = newText.split("\n", -1);
                        for (String line : textLines) {
                            if (line.length() > charsPerLine) {
                                wrappedLines += (line.length() / charsPerLine);
                            }
                        }

                        int totalLines = Math.max(1, lineBreaks + wrappedLines);
                        int newHeight = Math.max(24, totalLines * 17 + 10);
                        textArea.setPrefHeight(newHeight);
                        textArea.setMaxHeight(newHeight);
                    }
                });
            }
        });

        // Create resize handle (small triangle in bottom-right corner)
        Polygon resizeHandle = new Polygon();
        resizeHandle.getPoints().addAll(
                0.0, 10.0, // top-left
                10.0, 0.0, // top-right
                10.0, 10.0 // bottom-right
        );
        resizeHandle.setFill(Color.LIGHTGRAY);
        resizeHandle.setCursor(Cursor.SE_RESIZE);

        // Create larger invisible hit area for better usability
        javafx.scene.shape.Rectangle hitArea = new javafx.scene.shape.Rectangle(15, 15);
        hitArea.setFill(Color.TRANSPARENT);
        hitArea.setCursor(Cursor.SE_RESIZE);

        // Container to hold both TextArea and resize elements
        StackPane container = new StackPane();
        container.getChildren().addAll(textArea, resizeHandle, hitArea);

        // Position resize handle and hit area in bottom-right corner
        StackPane.setAlignment(resizeHandle, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(hitArea, javafx.geometry.Pos.BOTTOM_RIGHT);
        resizeHandle.setTranslateX(-2);
        resizeHandle.setTranslateY(-2);
        hitArea.setTranslateX(-2);
        hitArea.setTranslateY(-2);

        // Add resize functionality
        setupResizeHandlers(textArea, hitArea);

        return container;
    }

    /**
     * Sets up mouse handlers for resizing the TextArea.
     */
    private void setupResizeHandlers(TextArea textArea, javafx.scene.Node resizeElement) {
        final double[] dragAnchor = new double[2];

        resizeElement.setOnMousePressed(event -> {
            dragAnchor[0] = event.getSceneX();
            dragAnchor[1] = event.getSceneY();
            event.consume();
        });

        resizeElement.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - dragAnchor[0];
            double deltaY = event.getSceneY() - dragAnchor[1];

            double newWidth = Math.max(textArea.getMinWidth(), textArea.getPrefWidth() + deltaX);
            double newHeight = Math.max(textArea.getMinHeight(), textArea.getPrefHeight() + deltaY);

            // Only resize the TextArea, let container adjust automatically
            textArea.setPrefWidth(newWidth);
            textArea.setPrefHeight(newHeight);
            textArea.setMaxHeight(newHeight); // Allow manual resize to override auto-resize max height

            // Mark this text area as manually resized
            manuallyResizedTextAreas.add(textArea);

            dragAnchor[0] = event.getSceneX();
            dragAnchor[1] = event.getSceneY();
            event.consume();
        });
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
        TreeItem<TranslationRow> root = new TreeItem<>(new TranslationRow("Root", Collections.emptyMap()));
        root.setExpanded(true);
        rootItem = root; // Store reference for filtering
        allTableItems.clear(); // Clear previous items

        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            String key = entry.getKey();
            PZTranslationEntry translationEntry = entry.getValue();
            Map<String, Boolean> langPresence = new HashMap<>();
            for (String lang : allList) {
                boolean found = false;
                for (PZTranslationVariant variant : translationEntry.getVariants()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            lang.equals(variant.getFile().getLanguage().getCode()) &&
                            variant.getOriginalText() != null && !variant.getOriginalText().isEmpty()) {
                        found = true;
                        break;
                    }
                }
                langPresence.put(lang, found);
            }
            TreeItem<TranslationRow> item = new TreeItem<>(new TranslationRow(key, langPresence));
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
        for (Map.Entry<TextArea, PZTranslationVariant> entry : textAreaToVariant.entrySet()) {
            TextArea textArea = entry.getKey();

            // Find the corresponding reset and save links for this text area
            // This is a bit tricky since we need to find the parent container
            // For now, we'll trigger a refresh by simulating text property change
            Platform.runLater(() -> {
                String currentText = textArea.getText();
                textArea.setText(currentText + " ");
                textArea.setText(currentText);
            });
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
}
