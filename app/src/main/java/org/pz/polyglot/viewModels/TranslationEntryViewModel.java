package org.pz.polyglot.viewModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.models.translations.PZTranslationType;
import org.pz.polyglot.models.translations.PZTranslationVariant;
import org.pz.polyglot.viewModels.registries.TranslationVariantViewModelRegistry;

/**
 * ViewModel for PZTranslationEntry that provides observable properties
 * and manages the list of variant ViewModels.
 */
public class TranslationEntryViewModel {
    /**
     * Returns all language codes for which this entry has a translation.
     * 
     * @return a set of language codes present in the translation entry
     */
    public Set<String> getLanguages() {
        return getVariantViewModels().stream()
                .map(TranslationVariantViewModel::getLanguage)
                .filter(Objects::nonNull)
                .map(lang -> lang.getCode())
                .collect(Collectors.toSet());
    }

    /**
     * The underlying translation entry model.
     */
    private final PZTranslationEntry entry;

    /**
     * The translation key property.
     */
    private final StringProperty key = new SimpleStringProperty();

    /**
     * Observable list of variant ViewModels for this entry.
     */
    private final ObservableList<TranslationVariantViewModel> variantViewModels = FXCollections.observableArrayList();

    /**
     * Property indicating if this entry has any changes.
     */
    private final BooleanProperty hasChanges = new SimpleBooleanProperty();

    /**
     * Constructs a ViewModel for the given translation entry.
     * Initializes properties and listeners for change tracking.
     * 
     * @param entry the translation entry to wrap
     */
    public TranslationEntryViewModel(PZTranslationEntry entry) {
        this.entry = entry;

        key.set(entry.getKey());

        // Build initial variant ViewModels
        refreshVariantViewModels();

        // Listen for changes in variant ViewModels to update hasChanges property
        variantViewModels.addListener((ListChangeListener<TranslationVariantViewModel>) change -> {
            updateHasChangesProperty();
        });

        // Listen for changes in individual variant ViewModels
        for (TranslationVariantViewModel variantViewModel : variantViewModels) {
            variantViewModel.changedProperty().addListener((obs, oldVal, newVal) -> {
                updateHasChangesProperty();
            });
        }

        // Initial update of hasChanges property
        updateHasChangesProperty();
    }

    /**
     * Gets the translation key.
     * 
     * @return the translation key
     */
    public String getKey() {
        return key.get();
    }

    /**
     * Gets the translation key property.
     * 
     * @return the key property
     */
    public StringProperty keyProperty() {
        return key;
    }

    /**
     * Gets the underlying translation entry model.
     * 
     * @return the translation entry
     */
    public PZTranslationEntry getEntry() {
        return entry;
    }

    /**
     * Gets the observable list of variant ViewModels.
     * 
     * @return the list of variant ViewModels
     */
    public ObservableList<TranslationVariantViewModel> getVariantViewModels() {
        return variantViewModels;
    }

    /**
     * Gets variant ViewModels for a specific language code.
     * 
     * @param languageCode the language code to filter by
     * @return list of variant ViewModels for the language
     */
    public List<TranslationVariantViewModel> getVariantViewModelsForLanguage(String languageCode) {
        return variantViewModels.stream()
                .filter(vm -> vm.getLanguage() != null && languageCode.equals(vm.getLanguage().getCode()))
                .collect(Collectors.toList());
    }

    /**
     * Gets variant ViewModels for a specific language code filtered by enabled
     * sources.
     * 
     * @param languageCode   the language code to filter by
     * @param enabledSources set of enabled source names
     * @return list of variant ViewModels for the language and enabled sources
     */
    public List<TranslationVariantViewModel> getVariantViewModelsForLanguageFromEnabledSources(String languageCode,
            Set<String> enabledSources) {
        return variantViewModels.stream()
                .filter(vm -> vm.getLanguage() != null && languageCode.equals(vm.getLanguage().getCode()))
                .filter(vm -> enabledSources.isEmpty() || enabledSources.contains(vm.getSource()))
                .collect(Collectors.toList());
    }

    /**
     * Gets variant ViewModels for the specified language codes in the given order.
     * 
     * @param languageCodes list of language codes
     * @return list of variant ViewModels for the languages
     */
    public List<TranslationVariantViewModel> getVariantViewModelsForLanguages(List<String> languageCodes) {
        List<TranslationVariantViewModel> result = new ArrayList<>();
        for (String langCode : languageCodes) {
            result.addAll(getVariantViewModelsForLanguage(langCode));
        }
        return result;
    }

    /**
     * Checks if this translation entry has variants for the specified language.
     * 
     * @param languageCode the language code to check
     * @return true if there is at least one variant for the language
     */
    public boolean hasTranslationForLanguage(String languageCode) {
        return variantViewModels.stream()
                .anyMatch(vm -> vm.getLanguage() != null &&
                        languageCode.equals(vm.getLanguage().getCode()));
    }

    /**
     * Checks if this translation entry has changes for the specified language.
     * 
     * @param languageCode the language code to check
     * @return true if there are changes for the language
     */
    public boolean hasChangesForLanguage(String languageCode) {
        return variantViewModels.stream()
                .anyMatch(vm -> vm.getLanguage() != null &&
                        languageCode.equals(vm.getLanguage().getCode()) &&
                        vm.getVariant().isChanged());
    }

    /**
     * Returns whether this entry has any changes.
     * 
     * @return true if any variant has changes
     */
    public boolean getHasChanges() {
        return hasChanges.get();
    }

    /**
     * Gets the property indicating if this entry has any changes.
     * 
     * @return the hasChanges property
     */
    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    /**
     * Refreshes the variant ViewModels from the underlying entry and updates change
     * tracking.
     */
    public void refresh() {
        refreshVariantViewModels();
        updateHasChangesProperty();
    }

    /**
     * Builds a map indicating language presence for table display.
     * 
     * @return map of language code to presence (true/false)
     */
    public Map<String, Boolean> buildLanguagePresenceMap() {
        Map<String, Boolean> languagePresence = new HashMap<>();

        // Get all available languages
        PZLanguages pzLang = PZLanguages.getInstance();
        Set<String> availableLanguages = pzLang.getAllLanguageCodes();
        List<String> allLanguages = new ArrayList<>(availableLanguages);
        Collections.sort(allLanguages);
        if (allLanguages.remove("EN")) {
            allLanguages.add(0, "EN");
        }

        for (String lang : allLanguages) {
            languagePresence.put(lang, hasTranslationForLanguage(lang));
        }

        return languagePresence;
    }

    /**
     * Builds a map indicating language changes for table display.
     * 
     * @return map of language code to change status (true/false)
     */
    public Map<String, Boolean> buildLanguageChangesMap() {
        Map<String, Boolean> languageChanges = new HashMap<>();

        // Get all available languages
        PZLanguages pzLang = PZLanguages.getInstance();
        Set<String> availableLanguages = pzLang.getAllLanguageCodes();
        List<String> allLanguages = new ArrayList<>(availableLanguages);
        Collections.sort(allLanguages);
        if (allLanguages.remove("EN")) {
            allLanguages.add(0, "EN");
        }

        for (String lang : allLanguages) {
            languageChanges.put(lang, hasChangesForLanguage(lang));
        }

        return languageChanges;
    }

    /**
     * Gets the translation type for this entry.
     * 
     * @return the translation type
     */
    public PZTranslationType getType() {
        return entry.getType();
    }

    /**
     * Returns all translation types present in this entry (from its variants).
     * 
     * @return set of translation types from variants
     */
    public Set<PZTranslationType> getTypes() {
        return entry.getVariants().stream()
                .map(PZTranslationVariant::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Returns all sources present in this entry (from its variants).
     * 
     * @return set of source names from variants
     */
    public Set<String> getSources() {
        return entry.getVariants().stream()
                .map(variant -> variant.getSource().getName())
                .collect(Collectors.toSet());
    }

    /**
     * Rebuilds the list of variant ViewModels from the underlying entry.
     * Listeners for change tracking are also attached here.
     */
    private void refreshVariantViewModels() {
        variantViewModels.clear();

        // Create ViewModels for all variants
        for (PZTranslationVariant variant : entry.getVariants()) {
            TranslationVariantViewModel viewModel = TranslationVariantViewModelRegistry.getViewModel(variant);
            variantViewModels.add(viewModel);

            // Attach listener for changes in this variant ViewModel
            viewModel.changedProperty().addListener((obs, oldVal, newVal) -> {
                updateHasChangesProperty();
            });
        }
    }

    /**
     * Updates the hasChanges property based on the change status of all variants.
     */
    private void updateHasChangesProperty() {
        boolean hasAnyChanges = variantViewModels.stream()
                .anyMatch(vm -> vm.getVariant().isChanged());
        hasChanges.set(hasAnyChanges);
    }
}
