package org.pz.polyglot.ui.components;

import org.pz.polyglot.pz.languages.PZLanguage;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

/**
 * A simple language tag component with minimal styling.
 * Just a text label with a thin border and tooltip support.
 */
public class LanguageTag extends StackPane {
    /**
     * Creates a new LanguageTag with the specified language.
     * 
     * @param language the language to display
     */
    public LanguageTag(PZLanguage language) {
        this.getStyleClass().add("language-tag");

        // label

        Label label = new Label(language.getCode());
        label.getStyleClass().add("language-tag-label");
        this.getChildren().add(label);

        // tooltip

        Tooltip tooltip = new Tooltip(language.getName());
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        tooltip.setHideDelay(javafx.util.Duration.millis(0));
        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);
        tooltip.getStyleClass().add("language-tag-tooltip");
        Tooltip.install(this, tooltip);
    }
}
