package org.pz.polyglot.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import org.pz.polyglot.State;
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
/**
 * Dynamic multi-level context menu for creating new translation variants.
 * <p>
 * The menu structure is:
 * <ul>
 * <li>Level 1: Language selection</li>
 * <li>Level 2: Source selection</li>
 * <li>Level 3: Translation Type selection</li>
 * </ul>
 * The menu can be entered at any level if previous selections are already
 * known.
 */
public class DynamicContextMenu extends ContextMenu {

    /**
     * Immutable record representing the complete selection state for creating a new
     * translation variant.
     *
     * @param language        the selected language
     * @param source          the selected source
     * @param translationType the selected translation type
     */
    public record TranslationVariantSelection(
            PZLanguage language,
            PZSource source,
            PZTranslationType translationType) {
    }

    /**
     * Callback interface for handling a completed selection of translation variant.
     */
    @FunctionalInterface
    public interface SelectionCallback extends Consumer<TranslationVariantSelection> {
    }

    /**
     * Callback to execute when a complete selection is made.
     */
    private final SelectionCallback onSelectionComplete;

    /**
     * Constructs a new dynamic context menu starting from language selection (level
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
     * Constructs a new dynamic context menu starting from source selection (level
     * 2).
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
     * Populates the menu with enabled and disabled languages.
     */
    private void buildLanguageMenu() {
        getItems().clear();

        // Retrieve all available languages
        PZLanguages pzLanguages = PZLanguages.getInstance();
        var visibleLanguages = State.getInstance().getVisibleLanguages();

        // Split languages into enabled (visible) and disabled (hidden)
        List<String> enabledLanguagesList = new ArrayList<>();
        List<String> disabledLanguagesList = new ArrayList<>();

        for (String languageCode : pzLanguages.getAllLanguageCodes()) {
            if (visibleLanguages.contains(languageCode)) {
                enabledLanguagesList.add(languageCode);
            } else {
                disabledLanguagesList.add(languageCode);
            }
        }

        // Add enabled languages as regular menu items
        for (String languageCode : enabledLanguagesList) {
            pzLanguages.getLanguage(languageCode).ifPresent(language -> {
                MenuItem languageItem = new MenuItem(language.getCode() + " - " + language.getName());
                languageItem.setOnAction(e -> buildSourceMenu(language));
                getItems().add(languageItem);
            });
        }

        // Add disabled languages submenu if there are any disabled languages
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
     * Populates the menu with enabled and disabled sources for the selected
     * language.
     *
     * @param selectedLanguage the language selected in the previous menu level
     */
    private void buildSourceMenu(PZLanguage selectedLanguage) {
        getItems().clear();

        var enabledSources = State.getInstance().getEnabledSources();

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
     * Adds translation type submenu to a source menu.
     * Populates the menu with enabled and disabled translation types for the given
     * source and language.
     *
     * @param sourceMenu       the menu to which type submenus are added
     * @param selectedLanguage the selected language
     * @param source           the selected source
     */
    private void addTypeSubmenu(Menu sourceMenu, PZLanguage selectedLanguage, PZSource source) {
        var selectedTypes = State.getInstance().getSelectedTypes();

        // Split types into enabled and disabled
        List<PZTranslationType> enabledTypesList = Arrays.stream(PZTranslationType.values())
                .filter(type -> selectedTypes.contains(type))
                .toList();

        List<PZTranslationType> disabledTypesList = Arrays.stream(PZTranslationType.values())
                .filter(type -> !selectedTypes.contains(type))
                .toList();

        // Add enabled types as regular menu items
        for (PZTranslationType type : enabledTypesList) {
            MenuItem typeItem = new MenuItem(type.name());
            typeItem.setOnAction(e -> {
                // Notify callback with the completed selection and hide menu
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
                    // Notify callback with the completed selection and hide menu
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
