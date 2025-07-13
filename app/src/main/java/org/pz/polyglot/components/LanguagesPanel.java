package org.pz.polyglot.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.pz.polyglot.State;
import org.pz.polyglot.models.languages.PZLanguages;

import java.util.ArrayList;
import java.util.List;

public class LanguagesPanel extends VBox {
    private final State stateManager = State.getInstance();
    private final DragSelectListView<String> languagesListView = new DragSelectListView<>();
    private final Button allButton = new Button("All");
    private final Button noneButton = new Button("None");
    private final HBox buttonsBox = new HBox(8, allButton, noneButton);

    public LanguagesPanel() {
        setSpacing(0);
        setPadding(Insets.EMPTY);

        // Get all available languages in order (EN first, then alphabetical)
        PZLanguages pzLanguages = PZLanguages.getInstance();
        List<String> allLanguageCodes = new ArrayList<>(pzLanguages.getAllLanguageCodes());

        languagesListView.getItems().setAll(allLanguageCodes);

        languagesListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String languageCode, boolean empty) {
                    super.updateItem(languageCode, empty);
                    if (empty || languageCode == null) {
                        setText(null);
                        setStyle("");
                    } else {
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

        allButton.setOnAction(e -> {
            java.util.Set<Integer> allIndices = new java.util.HashSet<>();
            for (int i = 0; i < languagesListView.getItems().size(); i++) {
                allIndices.add(i);
            }
            languagesListView.selectItems(allIndices);
            stateManager.updateVisibleLanguages(new ArrayList<>(languagesListView.getItems()));
        });

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

        // Initial sync - delay to ensure ListView is fully initialized
        javafx.application.Platform.runLater(() -> {
            javafx.application.Platform.runLater(() -> {
                syncSelectionFromState();
            });
        });
    }

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
