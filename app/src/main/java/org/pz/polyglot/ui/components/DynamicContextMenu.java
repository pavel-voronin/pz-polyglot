package org.pz.polyglot.ui.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.pz.sources.PZSource;
import org.pz.polyglot.pz.sources.PZSources;
import org.pz.polyglot.pz.translations.PZTranslationType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dynamic multi-level context menu for creating new translation variants.
 * 
 * The menu structure is:
 * Level 1: Language selection
 * Level 2: Source selection
 * Level 3: Translation Type selection
 * 
 * The menu can be entered at any level if previous selections are already
 * known.
 */
public class DynamicContextMenu extends ContextMenu {

    /**
     * Represents the complete selection state for creating a new translation
     * variant.
     */
    public record TranslationVariantSelection(
            PZLanguage language,
            PZSource source,
            PZTranslationType translationType) {
    }

    /**
     * Callback interface for when a complete selection is made.
     */
    @FunctionalInterface
    public interface SelectionCallback extends Consumer<TranslationVariantSelection> {
    }

    private final SelectionCallback onSelectionComplete;

    /**
     * Creates a new dynamic context menu starting from language selection (level
     * 1).
     * 
     * @param onSelectionComplete callback to execute when a complete selection is
     *                            made
     */
    public DynamicContextMenu(SelectionCallback onSelectionComplete) {
        this.onSelectionComplete = onSelectionComplete;
        buildLanguageMenu();
    }

    /**
     * Creates a new dynamic context menu starting from source selection (level 2).
     * 
     * @param preselectedLanguage the already selected language
     * @param onSelectionComplete callback to execute when a complete selection is
     *                            made
     */
    public DynamicContextMenu(PZLanguage preselectedLanguage, SelectionCallback onSelectionComplete) {
        this.onSelectionComplete = onSelectionComplete;
        buildSourceMenu(preselectedLanguage);
    }

    /**
     * Builds the language selection menu (level 1).
     */
    private void buildLanguageMenu() {
        getItems().clear();

        // Get all available languages
        PZLanguages pzLanguages = PZLanguages.getInstance();

        // Add language menu items
        for (String languageCode : pzLanguages.getAllLanguageCodes()) {
            pzLanguages.getLanguage(languageCode).ifPresent(language -> {
                MenuItem languageItem = new MenuItem(language.getCode() + " - " + language.getName());
                languageItem.setOnAction(e -> buildSourceMenu(language));
                getItems().add(languageItem);
            });
        }
    }

    /**
     * Builds the source selection menu (level 2).
     */
    private void buildSourceMenu(PZLanguage selectedLanguage) {
        getItems().clear();

        // Get all available sources - only show editable ones
        List<PZSource> editableSources = PZSources.getInstance().getSources().stream()
                .filter(PZSource::isEditable)
                .toList();

        // Add each editable source as a submenu
        for (PZSource source : editableSources) {
            Menu sourceMenu = new Menu(source.toString());

            // Add all translation types to this source submenu
            for (PZTranslationType type : PZTranslationType.values()) {
                MenuItem typeItem = new MenuItem(type.name());
                typeItem.setOnAction(e -> {
                    // Complete selection - call the callback
                    TranslationVariantSelection selection = new TranslationVariantSelection(
                            selectedLanguage, source, type);
                    onSelectionComplete.accept(selection);
                    hide(); // Close the context menu
                });
                sourceMenu.getItems().add(typeItem);
            }

            getItems().add(sourceMenu);
        }
    }

}
