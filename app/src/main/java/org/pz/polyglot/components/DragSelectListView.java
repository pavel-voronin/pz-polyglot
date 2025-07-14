package org.pz.polyglot.components;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ListCell;

/**
 * A ListView supporting drag selection of multiple items, similar to desktop
 * file managers.
 * 
 * @param <T> the type of the items contained within the ListView
 */
public class DragSelectListView<T> extends ListView<T> {
    /** Indicates if a drag selection is in progress. */
    private boolean isDragging = false;

    /** Stores the initial selection state of the dragged item. */
    private boolean initialSelectionState = false;

    /** The index where the drag selection started. */
    public int startIndex = -1;

    /** True if mouse event filters have been set up. */
    private boolean setupDone = false;

    /** Indices currently being dragged over for selection preview. */
    private final Set<Integer> draggedIndices = new HashSet<>();

    /** Callback invoked when selection changes. */
    private Consumer<Set<Integer>> onSelectionChanged = null;

    /** Minimum index in the current drag selection. */
    private int minDraggedIndex = Integer.MAX_VALUE;

    /** Maximum index in the current drag selection. */
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

    /**
     * Sets a callback to be invoked when the selection changes.
     * 
     * @param callback a Consumer receiving the set of selected indices
     */
    public void setOnSelectionChanged(Consumer<Set<Integer>> callback) {
        this.onSelectionChanged = callback;
    }

    /**
     * Programmatically selects items by their indices (used for All/None buttons).
     * 
     * @param indices set of indices to select
     */
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

    /**
     * Clears all selections in the list view.
     */
    public void clearSelection() {
        if (getSelectionModel() == null)
            return;
        getSelectionModel().clearSelection();
        refresh();
    }

    /**
     * Sets up mouse event filters for drag selection. Called once per instance.
     */
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

    /**
     * Handles mouse press events to initiate drag selection.
     */
    private void onPressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || getSelectionModel() == null) {
            return;
        }

        // Ignore clicks on the scrollbar
        if (event.getX() > getWidth() - 20) {
            return;
        }

        // Ensure layout is up-to-date before index calculation
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

    /**
     * Handles mouse drag events to update drag selection preview.
     */
    private void onDragged(MouseEvent event) {
        if (!isDragging || getSelectionModel() == null)
            return;

        // Ignore drags on the scrollbar
        if (event.getX() > getWidth() - 20) {
            return;
        }

        // Ensure layout is up-to-date before index calculation
        this.layout();

        int currentIndex = getIndexAt(event);
        if (currentIndex >= 0 && currentIndex < getItems().size()) {
            // Update min/max range for drag selection
            minDraggedIndex = Math.min(minDraggedIndex, currentIndex);
            maxDraggedIndex = Math.max(maxDraggedIndex, currentIndex);

            // Fill entire range from min to max for preview
            draggedIndices.clear();
            for (int i = minDraggedIndex; i <= maxDraggedIndex; i++) {
                draggedIndices.add(i);
            }

            updateDragPreview();
        }

        event.consume();
    }

    /**
     * Handles mouse release events to finalize drag selection.
     */
    private void onReleased(MouseEvent event) {
        if (!isDragging)
            return;

        // Apply selection changes for all dragged indices
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

    /**
     * Refreshes the ListView to update drag preview styling.
     */
    private void updateDragPreview() {
        refresh();
    }

    /**
     * Notifies the selection change callback with the current selected indices.
     */
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

    /**
     * Calculates the index of the item under the mouse event.
     * Uses robust cell lookup, falling back to coordinate math if needed.
     * 
     * @param event the mouse event
     * @return the index under the mouse, or a bounded fallback
     */
    private int getIndexAt(MouseEvent event) {
        // Try to find the cell under the mouse Y coordinate
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

        // Fallback: estimate index by Y coordinate and cell height
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
            // Ignore and fallback
        }
        int index = (int) ((y + scrollOffset) / cellHeight);
        int boundedIndex = Math.max(0, Math.min(index, getItems().size() - 1));
        return boundedIndex;
    }

    /**
     * Returns the height of a cell in the list view.
     * 
     * @return cell height in pixels
     */
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
            // Ignore and fallback
        }

        return 24.0;
    }

    /**
     * Checks if the given index is currently being dragged over (for cell styling).
     * 
     * @param index the index to check
     * @return true if index is part of the current drag selection
     */
    public boolean isDraggedIndex(int index) {
        return isDragging && draggedIndices.contains(index);
    }

    /**
     * Returns true if a drag selection is in progress and the initial state is
     * selecting.
     * Used for cell styling.
     * 
     * @return true if drag selecting
     */
    public boolean isDragSelecting() {
        return isDragging && initialSelectionState;
    }
}