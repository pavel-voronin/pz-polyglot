package org.pz.polyglot.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.List;
import java.util.function.Consumer;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.sources.PZSource;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.models.translations.PZTranslationType;

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
        var visibleLanguages = org.pz.polyglot.State.getInstance().getVisibleLanguages();

        // Split languages into visible and hidden
        List<String> enabledLanguagesList = new java.util.ArrayList<>();
        List<String> disabledLanguagesList = new java.util.ArrayList<>();

        for (String languageCode : pzLanguages.getAllLanguageCodes()) {
            if (visibleLanguages.contains(languageCode)) {
                enabledLanguagesList.add(languageCode);
            } else {
                disabledLanguagesList.add(languageCode);
            }
        }

        // Add visible languages as regular menu items
        for (String languageCode : enabledLanguagesList) {
            pzLanguages.getLanguage(languageCode).ifPresent(language -> {
                MenuItem languageItem = new MenuItem(language.getCode() + " - " + language.getName());
                languageItem.setOnAction(e -> buildSourceMenu(language));
                getItems().add(languageItem);
            });
        }

        // Add hidden languages submenu if there are any hidden languages
        if (!disabledLanguagesList.isEmpty()) {
            Menu disabledLanguagesMenu = new Menu("Disabled Languages");

            for (String languageCode : disabledLanguagesList) {
                pzLanguages.getLanguage(languageCode).ifPresent(language -> {
                    MenuItem languageItem = new MenuItem(language.getCode() + " - " + language.getName());
                    languageItem.setOnAction(e -> buildSourceMenu(language));
                    disabledLanguagesMenu.getItems().add(languageItem);
                });
            }

            getItems().add(disabledLanguagesMenu);
        }
    }

    /**
     * Builds the source selection menu (level 2).
     */
    private void buildSourceMenu(PZLanguage selectedLanguage) {
        getItems().clear();

        var enabledSources = org.pz.polyglot.State.getInstance().getEnabledSources();

        // Get all editable sources and split them into enabled and disabled
        List<PZSource> allEditableSources = PZSources.getInstance().getSources().stream()
                .filter(PZSource::isEditable)
                .toList();

        List<PZSource> enabledSourcesList = allEditableSources.stream()
                .filter(source -> enabledSources.contains(source.getName()))
                .toList();

        List<PZSource> disabledSourcesList = allEditableSources.stream()
                .filter(source -> !enabledSources.contains(source.getName()))
                .toList();

        // Add enabled sources as regular menu items
        for (PZSource source : enabledSourcesList) {
            Menu sourceMenu = new Menu(source.toString());
            addTypeSubmenu(sourceMenu, selectedLanguage, source);
            getItems().add(sourceMenu);
        }

        // Add disabled sources submenu if there are any disabled sources
        if (!disabledSourcesList.isEmpty()) {
            Menu disabledSourcesMenu = new Menu("Disabled Sources");

            for (PZSource source : disabledSourcesList) {
                Menu sourceMenu = new Menu(source.toString());
                addTypeSubmenu(sourceMenu, selectedLanguage, source);
                disabledSourcesMenu.getItems().add(sourceMenu);
            }

            getItems().add(disabledSourcesMenu);
        }
    }

    /**
     * Adds type submenu to a source menu.
     */
    private void addTypeSubmenu(Menu sourceMenu, PZLanguage selectedLanguage, PZSource source) {
        var selectedTypes = org.pz.polyglot.State.getInstance().getSelectedTypes();

        // Split types into enabled and disabled
        List<PZTranslationType> enabledTypesList = java.util.Arrays.stream(PZTranslationType.values())
                .filter(type -> selectedTypes.contains(type))
                .toList();

        List<PZTranslationType> disabledTypesList = java.util.Arrays.stream(PZTranslationType.values())
                .filter(type -> !selectedTypes.contains(type))
                .toList();

        // Add enabled types as regular menu items
        for (PZTranslationType type : enabledTypesList) {
            MenuItem typeItem = new MenuItem(type.name());
            typeItem.setOnAction(e -> {
                TranslationVariantSelection selection = new TranslationVariantSelection(
                        selectedLanguage, source, type);
                onSelectionComplete.accept(selection);
                hide();
            });
            sourceMenu.getItems().add(typeItem);
        }

        // Add disabled types submenu if there are any disabled types
        if (!disabledTypesList.isEmpty()) {
            Menu disabledTypesMenu = new Menu("Disabled Types");

            for (PZTranslationType type : disabledTypesList) {
                MenuItem typeItem = new MenuItem(type.name());
                typeItem.setOnAction(e -> {
                    TranslationVariantSelection selection = new TranslationVariantSelection(
                            selectedLanguage, source, type);
                    onSelectionComplete.accept(selection);
                    hide();
                });
                disabledTypesMenu.getItems().add(typeItem);
            }

            sourceMenu.getItems().add(disabledTypesMenu);
        }
    }

}
