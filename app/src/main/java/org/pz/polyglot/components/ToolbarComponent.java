package org.pz.polyglot.components;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;

import org.pz.polyglot.State;
import org.pz.polyglot.components.addKeyDialog.AddKeyDialogManager;
import org.pz.polyglot.components.addModDialog.AddModDialogManager;
import org.pz.polyglot.models.TranslationSession;
import org.pz.polyglot.models.translations.PZTranslationManager;
import org.pz.polyglot.models.translations.PZTranslationVariant;
import org.pz.polyglot.models.translations.PZTranslations;
import org.pz.polyglot.utils.FolderUtils;

/**
 * Toolbar component with reactive behavior.
 * Manages toolbar buttons and their states through UIStateManager.
 */
/**
 * Toolbar component with reactive behavior.
 * Manages toolbar buttons and their states through UIStateManager.
 */
public class ToolbarComponent extends ToolBar {

    /** Button for adding a new translation key. */
    @FXML
    private Button addKeyButton;
    /** Button for adding a new mod. Disabled if workshop path is unavailable. */
    @FXML
    private Button addModButton;
    /** Button for saving all translation variants. Disabled if no changes. */
    @FXML
    private Button saveAllToolbarButton;
    /** Toggle button for showing/hiding the Types panel. */
    @FXML
    private ToggleButton typesButton;
    /** Toggle button for showing/hiding the Sources panel. */
    @FXML
    private ToggleButton sourcesButton;
    /** Toggle button for showing/hiding the Languages panel. */
    @FXML
    private ToggleButton languagesButton;

    /** Application state manager singleton. */
    private final State stateManager = State.getInstance();
    /** Indicates whether the Types panel is visible. */
    private final BooleanProperty typesPanelVisible = new SimpleBooleanProperty(false);
    /** Indicates whether the Sources panel is visible. */
    private final BooleanProperty sourcesPanelVisible = new SimpleBooleanProperty(false);
    /** Indicates whether the Languages panel is visible. */
    private final BooleanProperty languagesPanelVisible = new SimpleBooleanProperty(false);

    /**
     * Constructs the ToolbarComponent and loads its FXML definition.
     * Throws RuntimeException if FXML loading fails.
     */
    public ToolbarComponent() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Toolbar.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load toolbar.fxml", e);
        }
    }

    /**
     * Initializes the toolbar component and sets up all button actions and
     * listeners.
     * This method is called automatically by the FXML loader.
     */
    @FXML
    private void initialize() {
        // Add Key button: opens dialog and adds new key if provided
        addKeyButton.setOnAction(e -> {
            Stage stage = (Stage) addKeyButton.getScene().getWindow();
            String newKey = AddKeyDialogManager.showAddKeyDialog(stage);
            if (newKey != null && !newKey.trim().isEmpty()) {
                PZTranslations.getInstance().getOrCreateTranslation(newKey.trim());
                stateManager.requestTableRebuild();
            }
        });

        // Add Mod button: opens dialog for adding a mod
        addModButton.setOnAction(e -> {
            Stage stage = (Stage) addModButton.getScene().getWindow();
            AddModDialogManager.showAddModDialog(stage);
        });

        // Save All button: saves all translation variants and updates state
        saveAllToolbarButton.setOnAction(e -> {
            PZTranslationManager.saveAll();
            stateManager.updateHasChangesFromSession();
            stateManager.triggerSaveAllEvent();
        });

        // Types panel toggle: updates visibility state
        typesButton.setOnAction(e -> {
            boolean show = typesButton.isSelected();
            typesPanelVisible.set(show);
            stateManager.setTypesPanelVisible(show);
        });

        // Sources panel toggle: updates visibility state
        sourcesButton.setOnAction(e -> {
            boolean show = sourcesButton.isSelected();
            sourcesPanelVisible.set(show);
            stateManager.setSourcesPanelVisible(show);
        });

        // Languages panel toggle: updates visibility state
        languagesButton.setOnAction(e -> {
            boolean show = languagesButton.isSelected();
            languagesPanelVisible.set(show);
            stateManager.setLanguagesPanelVisible(show);
        });

        // Update button text when selected types change
        stateManager.selectedTypesChangedProperty().addListener((obs, oldVal, newVal) -> updateTypesButtonText());
        updateTypesButtonText();

        // Update button text when enabled sources change
        stateManager.enabledSourcesChangedProperty().addListener((obs, oldVal, newVal) -> updateSourcesButtonText());
        updateSourcesButtonText();

        // Update button text when visible languages change
        stateManager.getVisibleLanguages()
                .addListener((javafx.collections.ListChangeListener<String>) change -> updateLanguagesButtonText());
        updateLanguagesButtonText();

        // Sync toggle button state with panel visibility properties
        typesPanelVisible.addListener((obs, oldVal, newVal) -> typesButton.setSelected(newVal));
        sourcesPanelVisible.addListener((obs, oldVal, newVal) -> sourcesButton.setSelected(newVal));
        languagesPanelVisible.addListener((obs, oldVal, newVal) -> languagesButton.setSelected(newVal));

        // Listen for changes in the set of dirty translation variants to update Save
        // All button
        TranslationSession.getInstance().getVariants()
                .addListener((SetChangeListener<PZTranslationVariant>) change -> updateSaveAllButtonState());

        // Set initial button states
        updateSaveAllButtonState();
        updateAddModButtonState();
    }

    /**
     * Updates the Save All button's text and enabled state based on the number of
     * dirty translation variants.
     */
    private void updateSaveAllButtonState() {
        var variants = TranslationSession.getInstance().getVariants();
        int count = variants.size();
        saveAllToolbarButton.setText("Save All" + (count > 0 ? " (" + count + ")" : ""));
        saveAllToolbarButton.setDisable(count == 0);
    }

    /**
     * Updates the Add Mod button's enabled state based on workshop path
     * availability.
     * The button is disabled if the workshop path is not present.
     */
    private void updateAddModButtonState() {
        boolean hasValidWorkshopPath = FolderUtils.getWorkshopPath().isPresent();
        addModButton.setDisable(!hasValidWorkshopPath);
    }

    /**
     * Updates the Types button text to reflect the number of selected types.
     */
    private void updateTypesButtonText() {
        int count = stateManager.getSelectedTypes().size();
        typesButton.setText("Types (" + count + ")");
    }

    /**
     * Updates the Sources button text to reflect the number of enabled sources.
     */
    private void updateSourcesButtonText() {
        int count = stateManager.getEnabledSources().size();
        sourcesButton.setText("Sources (" + count + ")");
    }

    /**
     * Updates the Languages button text to reflect the number of visible languages.
     */
    private void updateLanguagesButtonText() {
        int count = stateManager.getVisibleLanguages().size();
        languagesButton.setText("Languages (" + count + ")");
    }
}
