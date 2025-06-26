package org.pz.polyglot.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import org.pz.polyglot.pz.translations.PZTranslations;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.pz.translations.PZTranslationVariant;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.pz.core.PZBuild;

import java.util.*;

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

    /**
     * Initializes the TreeTableView and its columns with translation data.
     */
    @FXML
    private void initialize() {
        populateTranslationsTable();
        startMemoryMonitor();
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
        // Remove all columns
        treeTableView.getColumns().clear();

        // Get all translations
        PZTranslations translations = PZTranslations.getInstance();
        Map<String, PZTranslationEntry> allTranslations = translations.getAllTranslations();

        // Get all language codes from PZLanguages (from PZBuild 42)
        PZLanguages pzLanguages = PZBuild.BUILD_42.getLanguages();
        List<String> sortedLangCodes = new ArrayList<>(pzLanguages.getAllLanguageCodes());
        Collections.sort(sortedLangCodes);
        // Move EN to the first position if present
        if (sortedLangCodes.remove("EN")) {
            sortedLangCodes.add(0, "EN");
        }

        // Key column
        keyColumn = new TreeTableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getKey()));
        keyColumn.setPrefWidth(150);
        keyColumn.setReorderable(false); // Prevent moving the key column
        treeTableView.getColumns().add(keyColumn);

        // Language columns
        for (String lang : sortedLangCodes) {
            TreeTableColumn<TranslationRow, String> langCol = new TreeTableColumn<>(lang);
            langCol.setCellValueFactory(param -> {
                boolean present = param.getValue().getValue().hasTranslation(lang);
                return new SimpleStringProperty(present ? "✔" : "");
            });
            langCol.setPrefWidth(60);
            // Allow reordering for language columns
            langCol.setReorderable(true);
            treeTableView.getColumns().add(langCol);
        }

        // Add listener to prevent any column from moving to position 0 except Key
        // column
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
            for (String lang : sortedLangCodes) {
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
}
