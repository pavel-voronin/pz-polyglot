package org.pz.polyglot.components;

import org.pz.polyglot.State;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class TablePanel extends VBox {
    private final TranslationTable table = new TranslationTable();
    private final TextField filterField = new TextField();

    public TablePanel() {
        setPadding(Insets.EMPTY);

        filterField.setPromptText("Filter by key...");
        filterField.setStyle("-fx-font-size: 12px;");
        HBox.setHgrow(filterField, Priority.ALWAYS);

        var filterBox = new HBox(filterField);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        filterField.textProperty()
                .addListener((obs, oldVal, newVal) -> State.getInstance().filterTextProperty().set(newVal));

        getChildren().setAll(filterBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }
}
