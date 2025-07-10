package org.pz.polyglot.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;

import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.models.translations.PZTranslations;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;
import org.pz.polyglot.viewModels.registries.TranslationEntryViewModelRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * Component encapsulating the translation entries TableView and its logic.
 */
public class TranslationTable extends TableView<TranslationEntryViewModel> {
    private ObservableList<TranslationEntryViewModel> allTableItems = FXCollections.observableArrayList();
    private ColumnManager columnManager;

    public TranslationTable() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/TranslationTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
        columnManager = new ColumnManager(this);
        columnManager.createColumns();
    }

    /**
     * Set the items to display in the table. This is the only way to set data.
     */
    public void setTableItems(ObservableList<TranslationEntryViewModel> items) {
        this.setItems(items);
    }

    public void populateTranslationsTable() {
        PZTranslations translations = PZTranslations.getInstance();
        Map<String, PZTranslationEntry> allTranslations = translations.getAllTranslations();
        allTableItems.clear();
        for (Map.Entry<String, PZTranslationEntry> entry : allTranslations.entrySet()) {
            PZTranslationEntry translationEntry = entry.getValue();
            TranslationEntryViewModel entryViewModel = TranslationEntryViewModelRegistry.getViewModel(translationEntry);
            allTableItems.add(entryViewModel);
        }
        setItems(allTableItems);
    }

    public void refreshTableIndicators() {
        if (allTableItems.isEmpty())
            return;
        for (TranslationEntryViewModel entryViewModel : allTableItems) {
            if (entryViewModel != null) {
                entryViewModel.refresh();
            }
        }
        refresh();
    }

    public void refreshTableIndicatorsForKey(String translationKey) {
        if (allTableItems.isEmpty())
            return;
        for (TranslationEntryViewModel item : allTableItems) {
            if (item.getKey().equals(translationKey)) {
                item.refresh();
                refresh();
                break;
            }
        }
    }

    public ColumnManager getColumnManager() {
        return columnManager;
    }
}
