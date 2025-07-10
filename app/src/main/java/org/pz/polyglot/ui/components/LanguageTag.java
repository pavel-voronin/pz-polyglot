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
        this(language, null);
    }

    /**
     * Creates a new LanguageTag with the specified language and optional click
     * callback.
     * 
     * @param language        the language to display
     * @param onClickCallback optional callback to execute when the tag is clicked,
     *                        if null the tag is not clickable
     */
    public LanguageTag(PZLanguage language, Runnable onClickCallback) {
        this.getStylesheets().add(getClass().getResource("/css/language-tag.css").toExternalForm());

        this.getStyleClass().add("language-tag");

        // Add clickable style and behavior if callback is provided
        if (onClickCallback != null) {
            this.getStyleClass().add("active");
            this.setOnMouseClicked(event -> onClickCallback.run());
        }

        // Set size constraints to prevent compression
        this.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        this.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

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
