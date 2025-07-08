package org.pz.polyglot.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.pz.polyglot.ui.models.TranslationVariantViewModel;
import org.pz.polyglot.ui.models.TranslationEntryViewModel;
import org.pz.polyglot.ui.state.UIStateManager;

import java.util.*;

/**
 * Component for displaying and editing translation details for a selected
 * translation key.
 * Shows the right panel with translation variants and controls.
 */
public class TranslationPanel extends VBox {

    // UI components
    private final Label panelTitleLabel;
    private final Button closePanelButton;
    private final ScrollPane panelScrollPane;
    private final VBox languageFieldsContainer;

    // Internal state
    private final List<TranslationVariantField> variantFields = new ArrayList<>();
    private Button saveAllButton;
    private Timeline tableRefreshTimer;
    private final Set<String> keysNeedingRefresh = new HashSet<>();

    // State manager
    private final UIStateManager stateManager = UIStateManager.getInstance();

    // Current data
    private String currentTranslationKey;
    private TranslationEntryViewModel currentEntryViewModel;

    public TranslationPanel() {
        // Initialize UI components
        this.panelTitleLabel = new Label();
        this.closePanelButton = new Button("✕");
        this.panelScrollPane = new ScrollPane();
        this.languageFieldsContainer = new VBox();

        // Load CSS stylesheet
        getStylesheets().add(getClass().getResource("/css/translation-panel.css").toExternalForm());

        setupComponent();
        setupStateBindings();
    }

    /**
     * Sets up bindings to state manager for automatic updates.
     */
    private void setupStateBindings() {
        stateManager.saveAllTriggeredProperty().addListener((obs, oldVal, newVal) -> {
            updateVariantButtons();
        });

        stateManager.getVisibleLanguages().addListener((ListChangeListener<String>) change -> {
            if (currentEntryViewModel != null) {
                updateLanguageFields();
            }
        });
    }

    private void setupComponent() {
        // Set main container style class
        getStyleClass().add("translation-panel");

        // Create header with title and close button
        BorderPane header = new BorderPane();
        header.getStyleClass().add("translation-panel-header");

        // Setup title label styling
        panelTitleLabel.getStyleClass().add("translation-panel-title");
        header.setLeft(panelTitleLabel);

        // Setup close button
        closePanelButton.getStyleClass().add("translation-panel-close-button");
        closePanelButton.setOnAction(e -> {
            stateManager.closeRightPanel();
        });
        header.setRight(closePanelButton);

        // Setup scroll pane - key fix here
        panelScrollPane.setFitToWidth(true);
        panelScrollPane.setFitToHeight(true);
        panelScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        panelScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panelScrollPane.setContent(languageFieldsContainer);

        // Make scroll pane expand to fill available space
        VBox.setVgrow(panelScrollPane, Priority.ALWAYS);

        // Setup language fields container
        languageFieldsContainer.getStyleClass().add("translation-panel-content");

        // Add header and scroll pane to main container
        getChildren().addAll(header, panelScrollPane);

        // Initially hidden
        setVisible(false);
        setManaged(false);
    }

    /**
     * Shows the translation panel with details for the given translation entry.
     */
    public void showTranslation(TranslationEntryViewModel entryViewModel) {
        // Stop any existing timer
        if (tableRefreshTimer != null) {
            tableRefreshTimer.stop();
        }
        keysNeedingRefresh.clear();

        // Store the current entry view model
        currentEntryViewModel = entryViewModel;
        currentTranslationKey = entryViewModel.getKey();

        // Update panel title with the key
        panelTitleLabel.setText(currentTranslationKey);

        // Update language fields
        updateLanguageFields();

        // Initial state update
        updateSaveAllButtonState();

        // Show the panel
        setVisible(true);
        setManaged(true);
    }

    /**
     * Updates the language fields based on current visible languages.
     */
    private void updateLanguageFields() {
        if (currentEntryViewModel == null) {
            return;
        }

        // Clear previous fields
        languageFieldsContainer.getChildren().clear();
        variantFields.clear();

        // Get visible languages from state manager
        List<String> visibleLanguageCodes = new ArrayList<>(stateManager.getVisibleLanguages());

        // Create fields for each language
        for (String langCode : visibleLanguageCodes) {
            // Get variant ViewModels for this language
            List<TranslationVariantViewModel> languageVariantViewModels = currentEntryViewModel
                    .getVariantViewModelsForLanguage(langCode);

            // Only create fields if variants exist
            if (!languageVariantViewModels.isEmpty()) {
                // Create fields for each variant of this language
                for (TranslationVariantViewModel variantViewModel : languageVariantViewModels) {
                    // Create variant field component
                    TranslationVariantField variantField = new TranslationVariantField(variantViewModel);

                    // Set up callbacks
                    variantField.setOnStateChanged(() -> {
                        updateSaveAllButtonState();
                        stateManager.updateHasChangesFromSession();
                    });

                    variantField.setOnVariantChanged(key -> {
                        scheduleTableRefresh(key);
                    });

                    // Store references
                    variantFields.add(variantField);

                    // Add to container
                    languageFieldsContainer.getChildren().add(variantField);
                }
            }
        }

        // Create "Save All" button
        saveAllButton = new Button("Save All");
        saveAllButton.getStyleClass().add("translation-panel-save-button");
        saveAllButton.setOnAction(e -> {
            // Save all changes for this entry
            if (currentEntryViewModel != null) {
                currentEntryViewModel.saveAll();
                // Update button states after saving
                updateSaveAllButtonState();
                stateManager.updateHasChangesFromSession();
                // Update individual save/reset buttons
                updateVariantButtons();
                // Refresh table indicators for this specific key only
                stateManager.triggerRefreshForKey(currentTranslationKey);
            }
        });

        // Add some spacing before the button
        Region spacer = new Region();
        spacer.getStyleClass().add("translation-panel-spacer");

        // Add button to the container
        languageFieldsContainer.getChildren().addAll(spacer, saveAllButton);
    }

    /**
     * Hides the translation panel and cleans up resources.
     */
    public void hidePanel() {
        // Stop the table refresh timer
        if (tableRefreshTimer != null) {
            tableRefreshTimer.stop();
        }
        keysNeedingRefresh.clear();

        setVisible(false);
        setManaged(false);
        variantFields.clear();
        saveAllButton = null;
        currentTranslationKey = null;
        currentEntryViewModel = null;
    }

    /**
     * Updates the state of the "Save All" button based on whether there are changes
     * to save.
     */
    private void updateSaveAllButtonState() {
        if (saveAllButton != null && currentEntryViewModel != null) {
            boolean hasChanges = currentEntryViewModel.getHasChanges();
            saveAllButton.setDisable(!hasChanges);
        }
    }

    /**
     * Updates the state of individual variant save/reset buttons.
     */
    public void updateVariantButtons() {
        for (TranslationVariantField variantField : variantFields) {
            variantField.updateVariantButtons();
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
        tableRefreshTimer = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    // Refresh indicators for all keys that need it
                    for (String key : keysNeedingRefresh) {
                        stateManager.triggerRefreshForKey(key);
                    }
                    keysNeedingRefresh.clear();
                }));
        tableRefreshTimer.play();
    }

    /**
     * Returns the currently displayed translation key.
     */
    public String getCurrentTranslationKey() {
        return currentTranslationKey;
    }

    /**
     * Returns whether the panel is currently showing a translation.
     */
    public boolean isShowingTranslation() {
        return currentTranslationKey != null && isVisible();
    }
}
