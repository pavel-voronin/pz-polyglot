package org.pz.polyglot.components;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ListCell;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class DragSelectListView<T> extends ListView<T> {
    private boolean isDragging = false;
    private boolean initialSelectionState = false;
    public int startIndex = -1;
    private boolean setupDone = false;
    private Set<Integer> draggedIndices = new HashSet<>();
    private Consumer<Set<Integer>> onSelectionChanged = null;
    private int minDraggedIndex = Integer.MAX_VALUE;
    private int maxDraggedIndex = Integer.MIN_VALUE;

    public DragSelectListView() {
        super();

        Platform.runLater(() -> {
            if (getSelectionModel() != null) {
                getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                forceSetup();
            }
        });

        itemsProperty().addListener((obs, oldItems, newItems) -> {
            if (!setupDone && getSelectionModel() != null) {
                forceSetup();
            }
        });

        parentProperty().addListener((obs, oldParent, newParent) -> {
            if (!setupDone && newParent != null && getSelectionModel() != null) {
                forceSetup();
            }
        });
    }

    public void setOnSelectionChanged(Consumer<Set<Integer>> callback) {
        this.onSelectionChanged = callback;
    }

    // Method to programmatically select items (for All/None buttons)
    public void selectItems(Set<Integer> indices) {
        if (getSelectionModel() == null)
            return;

        getSelectionModel().clearSelection();
        for (Integer index : indices) {
            if (index >= 0 && index < getItems().size()) {
                getSelectionModel().select(index);
            }
        }
        refresh();
    }

    public void clearSelection() {
        if (getSelectionModel() == null)
            return;
        getSelectionModel().clearSelection();
        refresh();
    }

    private void forceSetup() {
        if (setupDone)
            return;

        setOnMousePressed(null);
        setOnMouseDragged(null);
        setOnMouseReleased(null);

        addEventFilter(MouseEvent.MOUSE_PRESSED, this::onPressed);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::onReleased);

        setupDone = true;
    }

    private void onPressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || getSelectionModel() == null) {
            return;
        }

        // Check if clicking on scrollbar
        if (event.getX() > getWidth() - 20) {
            return; // Let scrollbar handle the event
        }

        // Force layout update before calculating index
        this.layout();

        int index = getIndexAt(event);

        if (index >= 0 && index < getItems().size()) {
            isDragging = true;
            startIndex = index;
            draggedIndices.clear();
            minDraggedIndex = index;
            maxDraggedIndex = index;

            initialSelectionState = !getSelectionModel().isSelected(index);

            // Add to dragged set for visual preview
            draggedIndices.add(index);
            updateDragPreview();
        }

        event.consume();
    }

    private void onDragged(MouseEvent event) {
        if (!isDragging || getSelectionModel() == null)
            return;

        // Check if dragging on scrollbar
        if (event.getX() > getWidth() - 20) {
            return;
        }

        // Force layout update before calculating index
        this.layout();

        int currentIndex = getIndexAt(event);
        if (currentIndex >= 0 && currentIndex < getItems().size()) {
            // Update min/max range
            minDraggedIndex = Math.min(minDraggedIndex, currentIndex);
            maxDraggedIndex = Math.max(maxDraggedIndex, currentIndex);

            // Fill entire range from min to max
            draggedIndices.clear();
            for (int i = minDraggedIndex; i <= maxDraggedIndex; i++) {
                draggedIndices.add(i);
            }

            updateDragPreview();
        }

        event.consume();
    }

    private void onReleased(MouseEvent event) {
        if (!isDragging)
            return;

        // Apply actual selection changes
        for (Integer index : draggedIndices) {
            if (initialSelectionState) {
                getSelectionModel().select(index);
            } else {
                getSelectionModel().clearSelection(index);
            }
        }

        // Notify callback once at the end
        notifySelectionChanged();

        isDragging = false;
        startIndex = -1;
        draggedIndices.clear();
        minDraggedIndex = Integer.MAX_VALUE;
        maxDraggedIndex = Integer.MIN_VALUE;

        // Refresh to remove drag preview styling
        refresh();
    }

    private void updateDragPreview() {
        // Refresh cells to show drag preview
        refresh();
    }

    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            Set<Integer> selectedIndices = new HashSet<>();
            for (int i = 0; i < getItems().size(); i++) {
                if (getSelectionModel().isSelected(i)) {
                    selectedIndices.add(i);
                }
            }
            onSelectionChanged.accept(selectedIndices);
        }
    }

    private int getIndexAt(MouseEvent event) {
        // Robust index calculation: find the cell under the mouse Y coordinate
        try {
            var virtualFlow = lookup(".virtual-flow");
            if (virtualFlow != null) {
                var visibleCells = virtualFlow.lookupAll(".list-cell");
                for (var cell : visibleCells) {
                    if (cell instanceof ListCell) {
                        ListCell<?> listCell = (ListCell<?>) cell;
                        if (listCell.getIndex() >= 0 && listCell.isVisible()) {
                            var bounds = listCell.getBoundsInParent();
                            if (event.getY() >= bounds.getMinY() && event.getY() <= bounds.getMaxY()) {
                                return listCell.getIndex();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore and fallback
        }

        // Fallback: use previous logic
        double y = event.getY();
        double cellHeight = getCellHeight();
        double scrollOffset = 0;
        try {
            var virtualFlow = lookup(".virtual-flow");
            if (virtualFlow != null) {
                var visibleCells = virtualFlow.lookupAll(".list-cell");
                if (!visibleCells.isEmpty()) {
                    for (var cell : visibleCells) {
                        if (cell instanceof ListCell) {
                            ListCell<?> listCell = (ListCell<?>) cell;
                            if (listCell.getIndex() >= 0) {
                                double cellY = listCell.getBoundsInParent().getMinY();
                                scrollOffset = -cellY + (listCell.getIndex() * cellHeight);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        int index = (int) ((y + scrollOffset) / cellHeight);
        int boundedIndex = Math.max(0, Math.min(index, getItems().size() - 1));
        return boundedIndex;
    }

    private double getCellHeight() {
        if (getFixedCellSize() > 0) {
            return getFixedCellSize();
        }

        try {
            var virtualFlow = lookup(".virtual-flow");
            if (virtualFlow != null) {
                var cells = virtualFlow.lookupAll(".list-cell");
                if (!cells.isEmpty()) {
                    var firstCell = cells.iterator().next();
                    double height = firstCell.getBoundsInLocal().getHeight();
                    if (height > 0) {
                        return height;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        return 24.0;
    }

    // Helper method to check if index is being dragged (for cell styling)
    public boolean isDraggedIndex(int index) {
        return isDragging && draggedIndices.contains(index);
    }

    // Helper method to get drag state for styling
    public boolean isDragSelecting() {
        return isDragging && initialSelectionState;
    }
}