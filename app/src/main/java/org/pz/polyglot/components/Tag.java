package org.pz.polyglot.components;

import java.util.function.Consumer;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * A simple tag component with minimal styling.
 * Displays a text label with a thin border and tooltip support.
 */
/**
 * A simple tag component with minimal styling.
 * Displays a text label with a thin border and tooltip support.
 */
public class Tag extends StackPane {

    /**
     * Theme for the Tag component, determines the color styling.
     */
    public enum Theme {
        /** Blue theme for the tag. */
        BLUE("blue"),
        /** Purple theme for the tag. */
        PURPLE("purple");

        /** CSS class name for the theme. */
        private final String tag;

        /**
         * Constructs a Theme enum value.
         * 
         * @param tag the CSS class name for the theme
         */
        Theme(String tag) {
            this.tag = tag;
        }

        /**
         * Returns the CSS class name for the theme.
         * 
         * @return the CSS class name
         */
        public String getTag() {
            return tag;
        }
    }

    /**
     * Constructs a new Tag with the default theme (BLUE), label, tooltip, and
     * optional click callback.
     *
     * @param labelText       the text to display inside the tag
     * @param tooltipText     the text to display in the tooltip
     * @param onClickCallback optional callback to execute when the tag is clicked
     */
    public Tag(String labelText, String tooltipText, Consumer<Tag> onClickCallback) {
        this(Theme.BLUE, labelText, tooltipText, onClickCallback);
    }

    /**
     * Constructs a new Tag with the specified theme, label, tooltip, and optional
     * click callback.
     *
     * @param theme           the theme to apply to the tag
     * @param labelText       the text to display inside the tag
     * @param tooltipText     the text to display in the tooltip
     * @param onClickCallback optional callback to execute when the tag is clicked
     */
    public Tag(Theme theme, String labelText, String tooltipText, Consumer<Tag> onClickCallback) {
        // Apply CSS stylesheet and theme classes
        this.getStylesheets().add(getClass().getResource("/css/tag.css").toExternalForm());
        this.getStyleClass().add("tag");
        this.getStyleClass().add(theme.getTag());

        // If a click callback is provided, mark as active and set handler
        if (onClickCallback != null) {
            this.getStyleClass().add("active");
            this.setOnMouseClicked(event -> onClickCallback.accept(this));
        }

        // Set preferred width constraints
        this.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        this.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        // Create and add the label to the tag
        Label label = new Label(labelText);
        label.getStyleClass().add("tag-label");
        this.getChildren().add(label);

        // Add tooltip if provided
        if (tooltipText != null && !tooltipText.isEmpty()) {
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.setHideDelay(Duration.millis(0));
            tooltip.setShowDuration(Duration.INDEFINITE);
            tooltip.getStyleClass().add("tag-tooltip");
            Tooltip.install(this, tooltip);
        }
    }
}
