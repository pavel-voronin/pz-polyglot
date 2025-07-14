package org.pz.polyglot.components;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.pz.polyglot.State;
import org.pz.polyglot.models.translations.PZTranslationType;

/**
 * Panel for selecting translation types in the UI.
 * Displays a list of {@link PZTranslationType} and allows selection via drag or
 * buttons.
 */
public class TypesPanel extends VBox {
    /**
     * The set of currently selected translation types.
     */
    private final Set<PZTranslationType> selectedTypes = new HashSet<>();

    /**
     * Reference to the application state manager.
     */
    private final State stateManager = State.getInstance();

    /**
     * List view for displaying and selecting translation types.
     */
    private final DragSelectListView<PZTranslationType> typesListView = new DragSelectListView<>();

    /**
     * Button to select all translation types.
     */
    private final Button allButton = new Button("All");

    /**
     * Button to deselect all translation types.
     */
    private final Button noneButton = new Button("None");

    /**
     * Container for selection buttons.
     */
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    /**
     * Constructs the TypesPanel and initializes UI components and event handlers.
     */
    public TypesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);

        // Populate the list view with all translation types
        typesListView.getItems().setAll(Arrays.asList(PZTranslationType.values()));
        selectedTypes.clear();
        selectedTypes.addAll(stateManager.getSelectedTypes());

        // Custom cell factory for coloring and displaying type names
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
                        // Apply background color based on drag/select state
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

        // Update state when selection changes in the list view
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

        // Select all types when 'All' button is pressed
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

        // Deselect all types when 'None' button is pressed
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

        // Ensure UI selection matches state after initialization
        Platform.runLater(this::syncSelectionFromState);
    }

    /**
     * Synchronizes the selection in the UI list view with the current state.
     */
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

    /**
     * Updates the local selectedTypes set from the state manager.
     */
    private void updateLocalState() {
        selectedTypes.clear();
        selectedTypes.addAll(stateManager.getSelectedTypes());
    }
}
