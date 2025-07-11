package org.pz.polyglot.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.pz.polyglot.State;
import org.pz.polyglot.models.translations.PZTranslationType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TypesPanel extends VBox {
    private final Set<PZTranslationType> selectedTypes = new HashSet<>();
    private final State stateManager = State.getInstance();
    private final ListView<PZTranslationType> typesListView = new ListView<>();
    private final Button allButton = new Button("All");
    private final Button noneButton = new Button("None");
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    public TypesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);
        typesListView.getItems().setAll(Arrays.asList(PZTranslationType.values()));
        selectedTypes.clear();
        selectedTypes.addAll(stateManager.getSelectedTypes());
        typesListView.refresh();
        typesListView.setSelectionModel(null);
        typesListView.setCellFactory(lv -> {
            ListCell<PZTranslationType> cell = new ListCell<>() {
                @Override
                protected void updateItem(PZTranslationType item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.name());
                        setStyle(selectedTypes.contains(item) ? "-fx-background-color: #cce5ff;"
                                : "-fx-background-color: transparent;");
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                PZTranslationType item = cell.getItem();
                if (item != null) {
                    if (selectedTypes.contains(item)) {
                        selectedTypes.remove(item);
                    } else {
                        selectedTypes.add(item);
                    }
                    typesListView.refresh();
                    stateManager.setSelectedTypes(Set.copyOf(selectedTypes));
                }
            });
            return cell;
        });
        typesListView.setFocusTraversable(false);
        allButton.setOnAction(e -> {
            selectedTypes.clear();
            selectedTypes.addAll(typesListView.getItems());
            typesListView.refresh();
            stateManager.setSelectedTypes(Set.copyOf(selectedTypes));
        });
        noneButton.setOnAction(e -> {
            selectedTypes.clear();
            typesListView.refresh();
            stateManager.setSelectedTypes(Set.copyOf(selectedTypes));
        });
        buttonsBox.setPadding(new Insets(2));
        buttonsBox.setSpacing(8);
        allButton.setPadding(new Insets(2));
        noneButton.setPadding(new Insets(2));
        getChildren().setAll(buttonsBox, typesListView);
        VBox.setVgrow(typesListView, Priority.ALWAYS);
    }
}
