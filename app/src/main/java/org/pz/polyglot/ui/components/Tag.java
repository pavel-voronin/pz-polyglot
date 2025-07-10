package org.pz.polyglot.ui.components;

import java.util.function.Consumer;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * A simple tag component with minimal styling.
 * Displays a text label with a thin border and tooltip support.
 */
public class Tag extends StackPane {
    public enum Theme {
        BLUE("blue"),
        PURPLE("purple");

        private final String tag;

        Theme(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    /**
     * Creates a new Tag with the specified label, tooltip, and optional click
     * callback.
     *
     * @param labelText       the text to display inside the tag
     * @param tooltipText     the text to display in the tooltip
     * @param onClickCallback optional callback to execute when the tag is clicked
     */
    public Tag(String labelText, String tooltipText, Consumer<Tag> onClickCallback) {
        this(Theme.BLUE, labelText, tooltipText, onClickCallback);
    }

    /**
     * Creates a new Tag with the specified label, tooltip, and optional click
     * callback.
     *
     * @param theme           the theme to apply to the tag
     * @param labelText       the text to display inside the tag
     * @param tooltipText     the text to display in the tooltip
     * @param onClickCallback optional callback to execute when the tag is clicked
     */
    public Tag(Theme theme, String labelText, String tooltipText, Consumer<Tag> onClickCallback) {
        this.getStylesheets().add(getClass().getResource("/css/tag.css").toExternalForm());
        this.getStyleClass().add("tag");
        this.getStyleClass().add(theme.getTag());

        if (onClickCallback != null) {
            this.getStyleClass().add("active");
            this.setOnMouseClicked(event -> onClickCallback.accept(this));
        }

        this.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        this.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        Label label = new Label(labelText);
        label.getStyleClass().add("tag-label");
        this.getChildren().add(label);

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
