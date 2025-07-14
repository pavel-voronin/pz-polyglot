package org.pz.polyglot.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.pz.polyglot.State;
import org.pz.polyglot.models.sources.PZSources;

/**
 * Panel for managing and displaying available sources with selection controls.
 */
public class SourcesPanel extends VBox {
    /**
     * Set of currently enabled sources.
     */
    private final Set<String> enabledSources = new HashSet<>();

    /**
     * State manager singleton for application state.
     */
    private final State stateManager = State.getInstance();

    /**
     * ListView for displaying and selecting sources with drag-select support.
     */
    private final DragSelectListView<String> sourcesListView = new DragSelectListView<>();

    /**
     * Button to select all sources.
     */
    private final Button allButton = new Button("All");

    /**
     * Button to deselect all sources.
     */
    private final Button noneButton = new Button("None");

    /**
     * Container for selection control buttons.
     */
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    public SourcesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);

        updateSourcesList();
        enabledSources.clear();
        enabledSources.addAll(stateManager.getEnabledSources());

        sourcesListView.setCellFactory(lv -> {
            // Custom cell to display source name and lock status
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
                        var sources = PZSources.getInstance().getSources();
                        for (var src : sources) {
                            // Mark source as locked if not editable
                            if (src.getName().equals(sourceName) && !src.isEditable()) {
                                isLocked = true;
                                break;
                            }
                        }
                        String displayText = isLocked ? "🔒 " + sourceName : sourceName;
                        setText(displayText);
                        setGraphic(null);

                        // Visual feedback for drag-select and selection states
                        DragSelectListView<String> dragListView = (DragSelectListView<String>) getListView();
                        boolean isDragged = dragListView.isDraggedIndex(getIndex());
                        boolean isDragSelecting = dragListView.isDragSelecting();
                        boolean isSelected = getListView().getSelectionModel().isSelected(getIndex());

                        String style;
                        if (isDragged) {
                            // Light green for drag-selecting, light gray for drag-deselecting
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

        // Set up selection change callback
        sourcesListView.setOnSelectionChanged(selectedIndices -> {
            // Update enabled sources in state manager when selection changes
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
            // Select all sources
            Set<Integer> allIndices = new HashSet<>();
            for (int i = 0; i < sourcesListView.getItems().size(); i++) {
                allIndices.add(i);
            }
            sourcesListView.selectItems(allIndices);

            // Update enabled sources in state manager
            var allSources = new HashSet<>(sourcesListView.getItems());
            stateManager.setEnabledSources(allSources);
            updateLocalState();
        });

        noneButton.setOnAction(e -> {
            // Deselect all sources
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

        // Listen for external changes in enabled sources and update selection
        // accordingly
        stateManager.getEnabledSources().addListener((javafx.collections.SetChangeListener<String>) change -> {
            updateLocalState();
            syncSelectionFromState();
        });

        // Initial sync: double runLater to ensure ListView is initialized before
        // syncing selection
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                syncSelectionFromState();
            });
        });
    }

    /**
     * Synchronizes the selection in the ListView with the enabled sources from the
     * state manager.
     */
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

    /**
     * Updates the ListView with all available sources, sorted by priority.
     */
    private void updateSourcesList() {
        List<String> allSources = PZSources.getInstance().getSources().stream()
                .map(source -> source.getName())
                .distinct()
                .toList();
        sourcesListView.getItems().setAll(allSources);
    }

    /**
     * Updates the local enabledSources set from the state manager.
     */
    private void updateLocalState() {
        enabledSources.clear();
        enabledSources.addAll(stateManager.getEnabledSources());
    }

    /**
     * Refreshes the sources list and synchronizes selection and local state.
     */
    public void refreshSources() {
        updateSourcesList();
        updateLocalState();
        syncSelectionFromState();
    }
}