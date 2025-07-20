package org.pz.polyglot.components;

import javafx.scene.control.CheckBox;

/**
 * Custom toggle switch component that extends CheckBox with specialized
 * styling.
 */
public class ToggleSwitch extends CheckBox {

    /**
     * Creates a new toggle switch with custom CSS styling.
     */
    public ToggleSwitch() {
        super();
        getStylesheets().add(getClass().getResource("/css/toggle-switch.css").toExternalForm());
        getStyleClass().add("toggle-switch");
    }
}
