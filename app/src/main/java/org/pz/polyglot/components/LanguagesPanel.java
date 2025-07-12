package org.pz.polyglot.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.pz.polyglot.State;
import org.pz.polyglot.models.languages.PZLanguages;

import java.util.ArrayList;
import java.util.List;

public class LanguagesPanel extends VBox {
    private final State stateManager = State.getInstance();
    private final ListView<String> languagesListView = new ListView<>();
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
        languagesListView.refresh();
        languagesListView.setSelectionModel(null);

        languagesListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String languageCode, boolean empty) {
                    super.updateItem(languageCode, empty);
                    if (empty || languageCode == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Format: "CODE - Language Name"
                        pzLanguages.getLanguage(languageCode).ifPresentOrElse(
                                language -> setText(language.getCode() + " - " + language.getName()),
                                () -> setText(languageCode));
                        // Check if this language is visible (selected)
                        boolean isVisible = stateManager.getVisibleLanguages().contains(languageCode);
                        setStyle(isVisible ? "-fx-background-color: #cce5ff;"
                                : "-fx-background-color: transparent;");
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                String languageCode = cell.getItem();
                if (languageCode != null) {
                    List<String> currentVisible = new ArrayList<>(stateManager.getVisibleLanguages());
                    if (currentVisible.contains(languageCode)) {
                        currentVisible.remove(languageCode);
                    } else {
                        currentVisible.add(languageCode);
                    }
                    stateManager.updateVisibleLanguages(currentVisible);
                    languagesListView.refresh();
                }
            });
            return cell;
        });

        languagesListView.setFocusTraversable(false);

        allButton.setOnAction(e -> {
            // Set all languages as visible
            List<String> allLanguages = new ArrayList<>(languagesListView.getItems());
            stateManager.updateVisibleLanguages(allLanguages);
            languagesListView.refresh();
        });

        noneButton.setOnAction(e -> {
            // Clear all visible languages
            stateManager.updateVisibleLanguages(new ArrayList<>());
            languagesListView.refresh();
        });

        buttonsBox.setPadding(new Insets(2));
        buttonsBox.setSpacing(8);
        allButton.setPadding(new Insets(2));
        noneButton.setPadding(new Insets(2));

        getChildren().setAll(buttonsBox, languagesListView);
        VBox.setVgrow(languagesListView, Priority.ALWAYS);

        // Listen for changes in visible languages to refresh the display
        stateManager.getVisibleLanguages().addListener((javafx.collections.ListChangeListener<String>) change -> {
            languagesListView.refresh();
        });
    }
}
