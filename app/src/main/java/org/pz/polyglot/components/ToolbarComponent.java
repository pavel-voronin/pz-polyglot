package org.pz.polyglot.components;

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

import java.io.IOException;

/**
 * Toolbar component with reactive behavior.
 * Manages toolbar buttons and their states through UIStateManager.
 */
public class ToolbarComponent extends ToolBar {

    @FXML
    private Button addKeyButton;
    @FXML
    private Button addModButton;
    @FXML
    private Button saveAllToolbarButton;
    @FXML
    private ToggleButton typesButton;
    @FXML
    private ToggleButton sourcesButton;
    @FXML
    private ToggleButton languagesButton;

    private final State stateManager = State.getInstance();
    private final BooleanProperty typesPanelVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty sourcesPanelVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty languagesPanelVisible = new SimpleBooleanProperty(false);

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

    @FXML
    private void initialize() {
        // Set up Add Key button action
        addKeyButton.setOnAction(e -> {
            // Get the current stage
            Stage stage = (Stage) addKeyButton.getScene().getWindow();
            String newKey = AddKeyDialogManager.showAddKeyDialog(stage);

            if (newKey != null && !newKey.trim().isEmpty()) {
                // Add the new key to translations
                PZTranslations.getInstance().getOrCreateTranslation(newKey.trim());

                // Rebuild the table to show the new entry
                stateManager.requestTableRebuild();
            }
        });

        // Set up Add Mod button action
        addModButton.setOnAction(e -> {
            // Get the current stage
            Stage stage = (Stage) addModButton.getScene().getWindow();
            AddModDialogManager.showAddModDialog(stage);
        });

        // Set up Save All toolbar button action
        saveAllToolbarButton.setOnAction(e -> {
            PZTranslationManager.saveAll();
            stateManager.updateHasChangesFromSession();
            stateManager.triggerSaveAllEvent();
        });

        // Types panel toggle logic
        typesButton.setOnAction(e -> {
            boolean show = typesButton.isSelected();
            typesPanelVisible.set(show);
            // Show/hide TypesPanel in main UI via state or controller
            stateManager.setTypesPanelVisible(show);
        });

        // Sources panel toggle logic
        sourcesButton.setOnAction(e -> {
            boolean show = sourcesButton.isSelected();
            sourcesPanelVisible.set(show);
            stateManager.setSourcesPanelVisible(show);
        });

        // Languages panel toggle logic
        languagesButton.setOnAction(e -> {
            boolean show = languagesButton.isSelected();
            languagesPanelVisible.set(show);
            // Show/hide LanguagesPanel in main UI via state or controller
            stateManager.setLanguagesPanelVisible(show);
        });

        // Listen for changes in selected types and update button text
        stateManager.selectedTypesChangedProperty().addListener((obs, oldVal, newVal) -> {
            updateTypesButtonText();
        });
        updateTypesButtonText();

        // Listen for changes in enabled sources and update button text
        stateManager.enabledSourcesChangedProperty().addListener((obs, oldVal, newVal) -> {
            updateSourcesButtonText();
        });
        updateSourcesButtonText();

        // Listen for changes in visible languages and update button text
        stateManager.getVisibleLanguages().addListener((javafx.collections.ListChangeListener<String>) change -> {
            updateLanguagesButtonText();
        });
        updateLanguagesButtonText();

        // Listen for changes in panel visibility and update toggle state
        typesPanelVisible.addListener((obs, oldVal, newVal) -> {
            typesButton.setSelected(newVal);
        });
        sourcesPanelVisible.addListener((obs, oldVal, newVal) -> {
            sourcesButton.setSelected(newVal);
        });
        languagesPanelVisible.addListener((obs, oldVal, newVal) -> {
            languagesButton.setSelected(newVal);
        });

        // Listen to changes in the set of dirty variants
        TranslationSession.getInstance().getVariants()
                .addListener((SetChangeListener<PZTranslationVariant>) change -> updateSaveAllButtonState());

        // Initial state
        updateSaveAllButtonState();
        updateAddModButtonState();
    }

    /**
     * Updates the Save All button's text and enabled state based on the current
     * variants.
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
     */
    private void updateAddModButtonState() {
        boolean hasValidWorkshopPath = FolderUtils.getWorkshopPath().isPresent();
        addModButton.setDisable(!hasValidWorkshopPath);
    }

    private void updateTypesButtonText() {
        int count = stateManager.getSelectedTypes().size();
        typesButton.setText("Types (" + count + ")");
    }

    private void updateSourcesButtonText() {
        int count = stateManager.getEnabledSources().size();
        sourcesButton.setText("Sources (" + count + ")");
    }

    private void updateLanguagesButtonText() {
        int count = stateManager.getVisibleLanguages().size();
        languagesButton.setText("Languages (" + count + ")");
    }
}
