package org.pz.polyglot.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.FlowPane;

import org.pz.polyglot.ui.models.TranslationVariantViewModel;
import org.pz.polyglot.ui.models.TranslationEntryViewModel;
import org.pz.polyglot.ui.state.UIStateManager;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.sources.PZSource;
import org.pz.polyglot.pz.translations.PZTranslationType;
import org.pz.polyglot.pz.translations.PZTranslationVariant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Component for displaying and editing translation details for a selected
 * translation key.
 * Shows the right panel with translation variants and controls.
 */
public class TranslationPanel extends VBox {

    /**
     * Represents a group of translation variants from the same source.
     */
    private record SourceGroup(
            String sourceName,
            Label sourceHeader,
            VBox container,
            List<TranslationVariantField> variantFields) {
    }

    // UI components
    private final Label panelTitleLabel;
    private final Button closePanelButton;
    private final ScrollPane panelScrollPane;
    private final VBox languageFieldsContainer;

    // Internal state
    private final Map<String, SourceGroup> sourceGroups = new LinkedHashMap<>();
    private final List<TranslationVariantField> allVariantFields = new ArrayList<>();
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
        panelScrollPane.getStyleClass().add("translation-scroll-panel");

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
        sourceGroups.clear();
        allVariantFields.clear();

        // Get visible languages from state manager
        List<String> visibleLanguageCodes = new ArrayList<>(stateManager.getVisibleLanguages());

        // Collect all variants for visible languages
        List<TranslationVariantViewModel> allVariants = new ArrayList<>();
        for (String langCode : visibleLanguageCodes) {
            List<TranslationVariantViewModel> languageVariantViewModels = currentEntryViewModel
                    .getVariantViewModelsForLanguage(langCode);
            allVariants.addAll(languageVariantViewModels);
        }

        // Group variants by source
        Map<String, List<TranslationVariantViewModel>> variantsBySource = allVariants.stream()
                .collect(Collectors.groupingBy(
                        TranslationVariantViewModel::getSource,
                        LinkedHashMap::new,
                        Collectors.toList()));

        // Create source groups and add to UI
        for (Map.Entry<String, List<TranslationVariantViewModel>> entry : variantsBySource.entrySet()) {
            String sourceName = entry.getKey();
            List<TranslationVariantViewModel> sourceVariants = entry.getValue();

            // Create source group
            SourceGroup sourceGroup = createSourceGroup(sourceName, sourceVariants);
            sourceGroups.put(sourceName, sourceGroup);

            // Add to container
            languageFieldsContainer.getChildren().add(sourceGroup.container());
        }

        // Create active languages section for adding new variants
        VBox activeLanguagesSection = createActiveLanguagesSection();

        // Add button and active languages section to the container
        languageFieldsContainer.getChildren().addAll(activeLanguagesSection);
    }

    /**
     * Creates a source group with header and variant fields.
     */
    private SourceGroup createSourceGroup(String sourceName, List<TranslationVariantViewModel> sourceVariants) {
        // Check if source is editable (all variants from same source should have same
        // editability)
        boolean isSourceEditable = sourceVariants.isEmpty() || sourceVariants.get(0).isSourceEditable();

        // Create source header container
        HBox headerContainer = new HBox();
        headerContainer.setSpacing(5);

        // Create source name label
        Label sourceHeader = new Label(sourceName);
        sourceHeader.getStyleClass().add("translation-panel-source-header");
        headerContainer.getChildren().add(sourceHeader);

        // Add lock icon if not editable
        if (!isSourceEditable) {
            Label lockIcon = new Label("🔒");
            lockIcon.getStyleClass().add("translation-panel-lock-icon");
            headerContainer.getChildren().add(lockIcon);
        }

        // Create container for this source group
        VBox sourceContainer = new VBox();
        sourceContainer.getStyleClass().add("translation-panel-source-group");

        // Add header to container
        sourceContainer.getChildren().add(headerContainer);

        // Create variant fields for this source
        List<TranslationVariantField> variantFields = new ArrayList<>();
        for (TranslationVariantViewModel variantViewModel : sourceVariants) {
            // Create variant field component
            TranslationVariantField variantField = new TranslationVariantField(variantViewModel);

            // Set up callbacks
            variantField.setOnStateChanged(() -> {
                stateManager.updateHasChangesFromSession();
            });

            variantField.setOnVariantChanged(key -> {
                scheduleTableRefresh(key);
            });

            // Store references
            variantFields.add(variantField);
            allVariantFields.add(variantField);

            // Add to source container
            sourceContainer.getChildren().add(variantField);
        }

        return new SourceGroup(sourceName, sourceHeader, sourceContainer, variantFields);
    }

    /**
     * Creates a section showing active languages (languages that are currently
     * selected
     * in the state manager) for adding new translation variants.
     */
    private VBox createActiveLanguagesSection() {
        VBox sectionContainer = new VBox();
        sectionContainer.getStyleClass().add("translation-panel-active-languages-section");

        // Get all active languages from state manager
        List<String> activeLanguageCodes = new ArrayList<>(stateManager.getVisibleLanguages());

        // Only show section if there are active languages
        if (!activeLanguageCodes.isEmpty()) {
            // Create header
            Label headerLabel = new Label("Add new variant:");
            headerLabel.getStyleClass().add("translation-panel-source-header");

            // Create flow pane for language tags
            FlowPane languageTagsFlow = new FlowPane();
            languageTagsFlow.getStyleClass().add("translation-panel-active-languages-flow");
            languageTagsFlow.setHgap(5);
            languageTagsFlow.setVgap(5);

            // Get PZLanguages instance
            PZLanguages pzLanguages = PZLanguages.getInstance();

            // Add language tags for active languages
            for (String langCode : activeLanguageCodes) {
                pzLanguages.getLanguage(langCode).ifPresent(language -> {
                    LanguageTag languageTag = new LanguageTag(language);

                    // Set click handler separately to avoid scope issues
                    languageTag.setOnMouseClicked(event -> {
                        // Create and show dynamic context menu starting from source selection (level 2)
                        DynamicContextMenu contextMenu = new DynamicContextMenu(language, selection -> {
                            // Handle the complete selection - create new translation variant
                            createNewTranslationVariant(selection);
                        });

                        // Show the context menu at the language tag location
                        contextMenu.show(languageTag, javafx.geometry.Side.BOTTOM, 0, 0);
                    });

                    languageTagsFlow.getChildren().add(languageTag);
                });
            }

            sectionContainer.getChildren().addAll(headerLabel, languageTagsFlow);
        }

        return sectionContainer;
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
        sourceGroups.clear();
        allVariantFields.clear();
        currentTranslationKey = null;
        currentEntryViewModel = null;
    }

    /**
     * Updates the state of individual variant save/reset buttons.
     */
    public void updateVariantButtons() {
        for (TranslationVariantField variantField : allVariantFields) {
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

    /**
     * Creates a new translation variant based on the provided selection.
     * 
     * @param selection the complete selection from the dynamic context menu
     */
    private void createNewTranslationVariant(DynamicContextMenu.TranslationVariantSelection selection) {
        if (currentEntryViewModel == null) {
            System.err.println("Cannot create variant: no current translation entry");
            return;
        }

        try {
            // Get the charset for the selected language and source version
            PZLanguage language = selection.language();
            PZSource source = selection.source();
            PZTranslationType type = selection.translationType();

            // Get charset for this language/source combination
            java.nio.charset.Charset charset = language.getCharset(source.getVersion())
                    .orElse(java.nio.charset.StandardCharsets.UTF_8);

            // Create new translation variant
            PZTranslationVariant newVariant = new PZTranslationVariant(
                    currentEntryViewModel.getEntry(),
                    source,
                    language,
                    type,
                    "", // Empty initial text
                    charset,
                    charset);

            // Add the variant to the entry
            currentEntryViewModel.getEntry().getVariants().add(newVariant);

            // Refresh the view model and UI
            currentEntryViewModel.refresh();
            updateLanguageFields();

            System.out.println("Created new translation variant: Language=" + language.getCode() +
                    ", Source=" + source.getName() +
                    ", Type=" + type.name());

        } catch (Exception e) {
            System.err.println("Failed to create new translation variant: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
