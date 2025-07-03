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
import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.ui.components.LanguageTag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
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
    private Map<TextArea, PZTranslationVariant> textAreaToVariant = new HashMap<>(); // Track which variant each text
                                                                                     // area represents
    // Track which text areas have been manually resized
    private Set<TextArea> manuallyResizedTextAreas = new HashSet<>();

    /**
     * Initializes the TreeTableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        startMemoryMonitor();
        // Handle Quit menu action
        quitMenuItem.setOnAction(event -> Platform.exit());
        setupRowSelectionListener();
        loadCssStyles();
    }

    /**
     * Loads CSS styles for the application.
     */
    private void loadCssStyles() {
        // Load CSS when the scene is available
        Platform.runLater(() -> {
            if (treeTableView.getScene() != null) {
                String cssPath = getClass().getResource("/css/custom-textarea.css").toExternalForm();
                String languageTagCssPath = getClass().getResource("/css/language-tag.css").toExternalForm();
                treeTableView.getScene().getStylesheets().addAll(cssPath, languageTagCssPath);
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
                for (PZTranslationVariant variant : entry.getTranslations()) {
                    if (variant.getFile() != null && variant.getFile().getLanguage() != null &&
                            langCode.equals(variant.getFile().getLanguage().getCode()) &&
                            variant.getText() != null && !variant.getText().isEmpty()) {
                        languageVariants.add(variant);
                    }
                }
            }

            // If no variants found, create empty field
            if (languageVariants.isEmpty()) {
                // Create horizontal container for language tag and other elements
                HBox labelContainer = new HBox(10);
                labelContainer.setPadding(new Insets(10, 0, 0, 0));
                labelContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                LanguageTag langTag = new LanguageTag(lang);

                labelContainer.getChildren().add(langTag);

                StackPane textAreaContainer = createResizableTextArea(langCode);
                TextArea textArea = (TextArea) textAreaContainer.getChildren().get(0);

                languageTextFields.put(langCode, textArea);
                languageFieldsContainer.getChildren().addAll(labelContainer, textAreaContainer);
            } else {
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
                    String detectedCharsetName = variant.getDetectedCharset() != null
                            ? variant.getDetectedCharset().name()
                            : "Unknown";
                    String supposedCharsetName = variant.getSupposedCharset() != null
                            ? variant.getSupposedCharset().name()
                            : "Unknown";

                    // Add source name and charset info as text (like before)
                    Label sourceLabel = new Label("(" + sourceName + ", " + detectedCharsetName
                            + (detectedCharsetName.equals(supposedCharsetName) ? "" : " *") + ")");
                    sourceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

                    // Create reset link (styled as hyperlink)
                    Hyperlink resetLink = new Hyperlink("reset");
                    resetLink.setStyle("-fx-font-size: 12px; -fx-padding: 0; -fx-text-fill: #007acc;");
                    resetLink.setVisible(variant.isEdited()); // Initially visible only if already edited

                    // Create save link (styled as hyperlink)
                    Hyperlink saveLink = new Hyperlink("save");
                    saveLink.setStyle("-fx-font-size: 12px; -fx-padding: 0; -fx-text-fill: #007acc;");
                    saveLink.setVisible(variant.isEdited()); // Initially visible only if already edited

                    // Add language tag, source info and spacer, then buttons to push them to the
                    // right
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    labelContainer.getChildren().addAll(langTag, sourceLabel, spacer, saveLink, resetLink);

                    StackPane textAreaContainer = createResizableTextArea(langCode + "_" + i);
                    TextArea textArea = (TextArea) textAreaContainer.getChildren().get(0);
                    textArea.setText(variant.getCurrentText()); // Use current text (edited or original)

                    // Track the variant for this text area
                    textAreaToVariant.put(textArea, variant);

                    // Set up reset functionality
                    resetLink.setOnAction(e -> {
                        variant.resetToOriginal();
                        textArea.setText(variant.getCurrentText());
                        resetLink.setVisible(false);
                        saveLink.setVisible(false);
                    });

                    // Set up save functionality (placeholder for now)
                    saveLink.setOnAction(e -> {
                        // TODO: Implement save functionality
                    });

                    // Track text changes and show/hide reset and save buttons
                    textArea.textProperty().addListener((obs, oldText, newText) -> {
                        if (newText != null && !newText.equals(variant.getOriginalText())) {
                            variant.setEditedText(newText);
                            resetLink.setVisible(true);
                            saveLink.setVisible(true);
                        } else if (newText != null && newText.equals(variant.getOriginalText())) {
                            variant.resetToOriginal();
                            resetLink.setVisible(false);
                            saveLink.setVisible(false);
                        }
                    });

                    // Store with unique key for multiple variants
                    String fieldKey = languageVariants.size() == 1 ? langCode : langCode + "_" + i;
                    languageTextFields.put(fieldKey, textArea);
                    languageFieldsContainer.getChildren().addAll(labelContainer, textAreaContainer);
                }
            }
        }

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
        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            String key = entry.getKey();
            PZTranslationEntry translationEntry = entry.getValue();
            Map<String, Boolean> langPresence = new HashMap<>();
            for (String lang : allList) {
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
}
