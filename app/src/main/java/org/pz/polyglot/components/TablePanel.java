package org.pz.polyglot.components;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.pz.polyglot.State;

/**
 * Panel containing the translation table and filter field for key-based
 * filtering.
 */
public class TablePanel extends VBox {
    /**
     * The table displaying translations.
     */
    private final TranslationTable table = new TranslationTable();

    /**
     * Text field for filtering table rows by key.
     */
    private final TextField filterField = new TextField();

    /**
     * Constructs the TablePanel and sets up UI components and filtering logic.
     */
    public TablePanel() {
        setPadding(Insets.EMPTY);

        filterField.setPromptText("Filter by key...");
        filterField.setStyle("-fx-font-size: 12px;");
        HBox.setHgrow(filterField, Priority.ALWAYS);

        var filterBox = new HBox(filterField);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Bind filter field input to global filter text property
        filterField.textProperty()
                .addListener((obs, oldVal, newVal) -> State.getInstance().filterTextProperty().set(newVal));

        getChildren().setAll(filterBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }
}
