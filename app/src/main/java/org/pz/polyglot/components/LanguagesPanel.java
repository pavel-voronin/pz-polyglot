package org.pz.polyglot.components;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.pz.polyglot.State;
import org.pz.polyglot.models.languages.PZLanguages;

/**
 * Panel for displaying and managing the selection of available languages.
 * Provides controls to select all or none, and synchronizes selection with
 * application state.
 */
public class LanguagesPanel extends VBox {
    /**
     * Reference to the application state manager for synchronizing visible
     * languages.
     */
    private final State stateManager = State.getInstance();

    /**
     * ListView for displaying available language codes with drag-select capability.
     */
    private final DragSelectListView<String> languagesListView = new DragSelectListView<>();

    /**
     * Button to select all languages.
     */
    private final Button allButton = new Button("All");

    /**
     * Button to clear all language selections.
     */
    private final Button noneButton = new Button("None");

    /**
     * Container for selection control buttons.
     */
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    /**
     * Constructs the LanguagesPanel and initializes UI components and bindings.
     */
    public LanguagesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);

        // Retrieve all available languages (EN first, then alphabetical)
        PZLanguages pzLanguages = PZLanguages.getInstance();
        List<String> allLanguageCodes = new ArrayList<>(pzLanguages.getAllLanguageCodes());

        // Populate the ListView with language codes
        languagesListView.getItems().setAll(allLanguageCodes);

        // Custom cell factory to display language code and name, and apply
        // selection/drag styles
        languagesListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String languageCode, boolean empty) {
                    super.updateItem(languageCode, empty);
                    if (empty || languageCode == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Show code and name if available, otherwise just code
                        pzLanguages.getLanguage(languageCode).ifPresentOrElse(
                                language -> setText(language.getCode() + " - " + language.getName()),
                                () -> setText(languageCode));
                        DragSelectListView<String> dragListView = (DragSelectListView<String>) getListView();
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

        // Update state when selection changes in the ListView
        languagesListView.setOnSelectionChanged(selectedIndices -> {
            List<String> selectedLanguages = new ArrayList<>();
            for (Integer index : selectedIndices) {
                if (index < languagesListView.getItems().size()) {
                    selectedLanguages.add(languagesListView.getItems().get(index));
                }
            }
            stateManager.updateVisibleLanguages(selectedLanguages);
        });

        languagesListView.setFocusTraversable(false);

        // Select all languages when 'All' button is pressed
        allButton.setOnAction(e -> {
            java.util.Set<Integer> allIndices = new java.util.HashSet<>();
            for (int i = 0; i < languagesListView.getItems().size(); i++) {
                allIndices.add(i);
            }
            languagesListView.selectItems(allIndices);
            stateManager.updateVisibleLanguages(new ArrayList<>(languagesListView.getItems()));
        });

        // Clear selection when 'None' button is pressed
        noneButton.setOnAction(e -> {
            languagesListView.clearSelection();
            stateManager.updateVisibleLanguages(new ArrayList<>());
        });

        buttonsBox.setPadding(new Insets(2));
        buttonsBox.setSpacing(8);
        allButton.setPadding(new Insets(2));
        noneButton.setPadding(new Insets(2));

        getChildren().setAll(buttonsBox, languagesListView);
        VBox.setVgrow(languagesListView, Priority.ALWAYS);

        // Two-way binding: update ListView selection when State changes
        stateManager.getVisibleLanguages().addListener((javafx.collections.ListChangeListener<String>) change -> {
            syncSelectionFromState();
        });

        // Initial sync: ensure ListView selection matches state after initialization
        javafx.application.Platform.runLater(() -> {
            javafx.application.Platform.runLater(() -> {
                syncSelectionFromState();
            });
        });
    }

    /**
     * Synchronizes the ListView selection with the current visible languages in the
     * state.
     * Called when the state changes or on initialization.
     */
    private void syncSelectionFromState() {
        List<String> visibleLanguages = stateManager.getVisibleLanguages();
        java.util.Set<Integer> indicesToSelect = new java.util.HashSet<>();
        for (int i = 0; i < languagesListView.getItems().size(); i++) {
            if (visibleLanguages.contains(languagesListView.getItems().get(i))) {
                indicesToSelect.add(i);
            }
        }
        languagesListView.selectItems(indicesToSelect);
    }
}
