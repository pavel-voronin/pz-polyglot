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
import org.pz.polyglot.models.translations.PZTranslationManager;
import org.pz.polyglot.models.translations.PZTranslationSession;
import org.pz.polyglot.models.translations.PZTranslationVariant;
import org.pz.polyglot.models.translations.PZTranslations;
import org.pz.polyglot.util.FolderUtils;

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

    private final State stateManager = State.getInstance();
    private final BooleanProperty typesPanelVisible = new SimpleBooleanProperty(false);

    public ToolbarComponent() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/toolbar.fxml"));
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

        // Listen for changes in selected types and update button text
        stateManager.selectedTypesChangedProperty().addListener((obs, oldVal, newVal) -> {
            updateTypesButtonText();
        });
        updateTypesButtonText();

        // Listen for changes in panel visibility and update toggle state
        typesPanelVisible.addListener((obs, oldVal, newVal) -> {
            typesButton.setSelected(newVal);
        });

        // Listen to changes in the set of dirty variants
        PZTranslationSession.getInstance().getVariants()
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
        var variants = PZTranslationSession.getInstance().getVariants();
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
}
