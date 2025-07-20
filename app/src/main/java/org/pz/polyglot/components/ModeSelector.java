package org.pz.polyglot.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.pz.polyglot.State;
import org.pz.polyglot.models.WorkMode;

/**
 * A UI component that allows users to switch between different work modes.
 * <p>
 * This component displays a toggle switch with labels for each available mode.
 * When a mode is active, its corresponding label is enabled while the other
 * label is disabled to provide visual feedback about the current state.
 * <p>
 * The component automatically synchronizes with the global application state
 * and persists mode changes across the application.
 */
public class ModeSelector extends HBox {
    /** Label for Discovery mode text. */
    @FXML
    private Label discoveryModeLabel;
    /** Label for Focus mode text. */
    @FXML
    private Label focusModeLabel;
    /** Toggle switch component that controls mode selection. */
    @FXML
    private ToggleSwitch modeSwitch;

    /**
     * Creates a new ModeSelector component.
     */
    public ModeSelector() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModeSelector.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ModeSelector.fxml", e);
        }

        initializeMode();
    }

    /**
     * Initializes the mode selector with current state and sets up event listeners.
     */
    private void initializeMode() {
        State state = State.getInstance();

        // Set initial state from State
        boolean isFocus = state.getCurrentWorkMode() == WorkMode.FOCUS;
        modeSwitch.setSelected(isFocus);
        updateLabelStates(state.getCurrentWorkMode());

        // Listen for toggle switch changes
        modeSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            WorkMode newMode = newVal ? WorkMode.FOCUS : WorkMode.DISCOVERY;
            state.setCurrentWorkMode(newMode);
            updateLabelStates(newMode);
        });

        // Listen for state changes
        state.currentWorkModeProperty().addListener((obs, oldVal, newVal) -> {
            boolean isFocusMode = newVal == WorkMode.FOCUS;
            modeSwitch.setSelected(isFocusMode);
            updateLabelStates(newVal);
        });
    }

    /**
     * Updates the visual state of mode labels based on the current work mode.
     * <p>
     * When a mode is active, its corresponding label is enabled while the
     * other label is disabled (grayed out). This provides clear visual
     * feedback about which mode is currently selected.
     * 
     * @param currentMode the currently active work mode
     */
    private void updateLabelStates(WorkMode currentMode) {
        if (currentMode == WorkMode.DISCOVERY) {
            discoveryModeLabel.setDisable(false);
            focusModeLabel.setDisable(true);
        } else {
            discoveryModeLabel.setDisable(true);
            focusModeLabel.setDisable(false);
        }
    }
}
