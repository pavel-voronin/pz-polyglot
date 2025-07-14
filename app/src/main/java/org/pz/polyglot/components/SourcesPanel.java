package org.pz.polyglot.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.pz.polyglot.State;
import org.pz.polyglot.models.sources.PZSources;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SourcesPanel extends VBox {
    private final Set<String> enabledSources = new HashSet<>();
    private final State stateManager = State.getInstance();
    private final DragSelectListView<String> sourcesListView = new DragSelectListView<>();
    private final Button allButton = new Button("All");
    private final Button noneButton = new Button("None");
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    public SourcesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);

        updateSourcesList();
        enabledSources.clear();
        enabledSources.addAll(stateManager.getEnabledSources());

        sourcesListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String sourceName, boolean empty) {
                    super.updateItem(sourceName, empty);
                    if (empty || sourceName == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        boolean isLocked = false;
                        var sources = org.pz.polyglot.models.sources.PZSources.getInstance().getSources();
                        for (var src : sources) {
                            if (src.getName().equals(sourceName) && !src.isEditable()) {
                                isLocked = true;
                                break;
                            }
                        }
                        // Just prepend lock emoji to text if locked
                        String displayText = isLocked ? "🔒 " + sourceName : sourceName;
                        setText(displayText);
                        setGraphic(null);

                        // Check if this cell is being dragged
                        DragSelectListView<String> dragListView = (DragSelectListView<String>) getListView();
                        boolean isDragged = dragListView.isDraggedIndex(getIndex());
                        boolean isDragSelecting = dragListView.isDragSelecting();

                        // Regular selection state
                        boolean isSelected = getListView().getSelectionModel().isSelected(getIndex());

                        String style;
                        if (isDragged) {
                            // Show drag preview with softer colors
                            if (isDragSelecting) {
                                style = "-fx-background-color: #c8e6c9;"; // Light green for drag-selecting
                            } else {
                                style = "-fx-background-color: #e0e0e0;"; // Light gray for drag-deselecting
                            }
                        } else if (isSelected) {
                            style = "-fx-background-color: #cce5ff;"; // Blue for selected
                        } else {
                            style = "-fx-background-color: transparent;";
                        }

                        setStyle(style);
                    }
                }
            };
            return cell;
        });

        // Set up selection change callback
        sourcesListView.setOnSelectionChanged(selectedIndices -> {
            Set<String> selectedSources = new HashSet<>();
            for (Integer index : selectedIndices) {
                if (index < sourcesListView.getItems().size()) {
                    selectedSources.add(sourcesListView.getItems().get(index));
                }
            }
            stateManager.setEnabledSources(selectedSources);
            updateLocalState();
        });

        sourcesListView.setFocusTraversable(false);

        allButton.setOnAction(e -> {
            // Select all indices
            Set<Integer> allIndices = new HashSet<>();
            for (int i = 0; i < sourcesListView.getItems().size(); i++) {
                allIndices.add(i);
            }
            sourcesListView.selectItems(allIndices);

            // Update state
            var allSources = new HashSet<>(sourcesListView.getItems());
            stateManager.setEnabledSources(allSources);
            updateLocalState();
        });

        noneButton.setOnAction(e -> {
            sourcesListView.clearSelection();
            stateManager.setEnabledSources(Set.of());
            updateLocalState();
        });

        buttonsBox.setPadding(new Insets(2));
        buttonsBox.setSpacing(8);
        allButton.setPadding(new Insets(2));
        noneButton.setPadding(new Insets(2));

        getChildren().setAll(buttonsBox, sourcesListView);
        VBox.setVgrow(sourcesListView, Priority.ALWAYS);

        // Listen for external changes in enabled sources
        stateManager.getEnabledSources().addListener((javafx.collections.SetChangeListener<String>) change -> {
            updateLocalState();
            syncSelectionFromState();
        });

        // Initial sync - delay to ensure ListView is fully initialized
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                syncSelectionFromState();
            });
        });
    }

    private void syncSelectionFromState() {
        Set<Integer> indicesToSelect = new HashSet<>();
        Set<String> enabledSources = stateManager.getEnabledSources();

        for (int i = 0; i < sourcesListView.getItems().size(); i++) {
            if (enabledSources.contains(sourcesListView.getItems().get(i))) {
                indicesToSelect.add(i);
            }
        }

        sourcesListView.selectItems(indicesToSelect);
    }

    private void updateSourcesList() {
        // Sources are already sorted by priority in PZSources.getSources()
        List<String> allSources = PZSources.getInstance().getSources().stream()
                .map(source -> source.getName())
                .distinct()
                .toList();
        sourcesListView.getItems().setAll(allSources);
    }

    private void updateLocalState() {
        enabledSources.clear();
        enabledSources.addAll(stateManager.getEnabledSources());
    }

    public void refreshSources() {
        updateSourcesList();
        updateLocalState();
        syncSelectionFromState();
    }
}