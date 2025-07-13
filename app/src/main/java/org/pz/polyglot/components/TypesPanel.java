package org.pz.polyglot.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
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
    private final DragSelectListView<PZTranslationType> typesListView = new DragSelectListView<>();
    private final Button allButton = new Button("All");
    private final Button noneButton = new Button("None");
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    public TypesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);

        typesListView.getItems().setAll(Arrays.asList(PZTranslationType.values()));
        selectedTypes.clear();
        selectedTypes.addAll(stateManager.getSelectedTypes());

        typesListView.setCellFactory(lv -> {
            ListCell<PZTranslationType> cell = new ListCell<>() {
                @Override
                protected void updateItem(PZTranslationType type, boolean empty) {
                    super.updateItem(type, empty);
                    if (empty || type == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(type.name());
                        DragSelectListView<PZTranslationType> dragListView = (DragSelectListView<PZTranslationType>) getListView();
                        boolean isDragged = dragListView.isDraggedIndex(getIndex());
                        boolean isDragSelecting = dragListView.isDragSelecting();
                        boolean isSelected = getListView().getSelectionModel().isSelected(getIndex());
                        String style;
                        if (isDragged) {
                            style = isDragSelecting ? "-fx-background-color: #c8e6c9;"
                                    : "-fx-background-color: #e0e0e0;";
                        } else if (isSelected) {
                            style = "-fx-background-color: #cce5ff;";
                        } else {
                            style = "-fx-background-color: transparent;";
                        }
                        setStyle(style);
                    }
                }
            };
            return cell;
        });

        typesListView.setOnSelectionChanged(selectedIndices -> {
            Set<PZTranslationType> selected = new HashSet<>();
            for (Integer index : selectedIndices) {
                if (index < typesListView.getItems().size()) {
                    selected.add(typesListView.getItems().get(index));
                }
            }
            stateManager.setSelectedTypes(selected);
            updateLocalState();
        });

        typesListView.setFocusTraversable(false);

        allButton.setOnAction(e -> {
            Set<Integer> allIndices = new HashSet<>();
            for (int i = 0; i < typesListView.getItems().size(); i++) {
                allIndices.add(i);
            }
            typesListView.selectItems(allIndices);
            var allTypes = new HashSet<>(typesListView.getItems());
            stateManager.setSelectedTypes(allTypes);
            updateLocalState();
        });

        noneButton.setOnAction(e -> {
            typesListView.clearSelection();
            stateManager.setSelectedTypes(Set.of());
            updateLocalState();
        });

        buttonsBox.setPadding(new Insets(2));
        buttonsBox.setSpacing(8);
        allButton.setPadding(new Insets(2));
        noneButton.setPadding(new Insets(2));

        getChildren().setAll(buttonsBox, typesListView);
        VBox.setVgrow(typesListView, Priority.ALWAYS);

        Platform.runLater(() -> {
            syncSelectionFromState();
        });
    }

    private void syncSelectionFromState() {
        Set<Integer> indicesToSelect = new HashSet<>();
        Set<PZTranslationType> selected = stateManager.getSelectedTypes();
        for (int i = 0; i < typesListView.getItems().size(); i++) {
            if (selected.contains(typesListView.getItems().get(i))) {
                indicesToSelect.add(i);
            }
        }
        typesListView.selectItems(indicesToSelect);
    }

    private void updateLocalState() {
        selectedTypes.clear();
        selectedTypes.addAll(stateManager.getSelectedTypes());
    }
}
