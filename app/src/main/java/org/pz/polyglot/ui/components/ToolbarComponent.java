package org.pz.polyglot.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.pz.polyglot.pz.translations.PZTranslationManager;
import org.pz.polyglot.pz.translations.PZTranslationSession;
import org.pz.polyglot.ui.state.UIStateManager;

import java.io.IOException;

/**
 * Toolbar component with reactive behavior.
 * Manages toolbar buttons and their states through UIStateManager.
 */
public class ToolbarComponent extends ToolBar {

    @FXML
    private Button saveAllToolbarButton;

    private final UIStateManager stateManager = UIStateManager.getInstance();

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
        setupToolbarButtons();
        setupReactiveBindings();
    }

    /**
     * Sets up the toolbar buttons and their actions.
     */
    private void setupToolbarButtons() {
        // Set up Save All toolbar button
        saveAllToolbarButton.setOnAction(e -> {
            PZTranslationManager.saveAll();
            // Update state which will trigger all necessary updates
            stateManager.updateHasChangesFromSession();
            // Trigger save all event for other components to react
            stateManager.triggerSaveAllEvent();
        });

        // Initial state update
        updateToolbarSaveAllButtonState();
    }

    /**
     * Sets up reactive bindings with UIStateManager.
     */
    private void setupReactiveBindings() {
        // Bind toolbar button state to changes
        stateManager.hasChangesProperty().addListener((obs, oldVal, newVal) -> {
            saveAllToolbarButton.setDisable(!newVal);
        });
    }

    /**
     * Updates the state of the toolbar "Save All" button based on updated variants.
     */
    private void updateToolbarSaveAllButtonState() {
        PZTranslationSession updatedVariants = PZTranslationSession.getInstance();
        boolean hasUpdatedVariants = !updatedVariants.getVariants().isEmpty();
        saveAllToolbarButton.setDisable(!hasUpdatedVariants);
    }
}
