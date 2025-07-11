package org.pz.polyglot.viewModels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.models.translations.PZTranslationType;
import org.pz.polyglot.models.translations.PZTranslationVariant;
import org.pz.polyglot.viewModels.registries.TranslationVariantViewModelRegistry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ViewModel for PZTranslationEntry that provides observable properties
 * and manages the list of variant ViewModels.
 */
public class TranslationEntryViewModel {
    private final PZTranslationEntry entry;

    private final StringProperty key = new SimpleStringProperty();
    private final ObservableList<TranslationVariantViewModel> variantViewModels = FXCollections.observableArrayList();
    private final BooleanProperty hasChanges = new SimpleBooleanProperty();

    public TranslationEntryViewModel(PZTranslationEntry entry) {
        this.entry = entry;

        // Initialize properties
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
     */
    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    /**
     * Gets the underlying PZTranslationEntry.
     */
    public PZTranslationEntry getEntry() {
        return entry;
    }

    /**
     * Gets the observable list of variant ViewModels.
     */
    public ObservableList<TranslationVariantViewModel> getVariantViewModels() {
        return variantViewModels;
    }

    /**
     * Gets variant ViewModels for a specific language code.
     */
    public List<TranslationVariantViewModel> getVariantViewModelsForLanguage(String languageCode) {
        return variantViewModels.stream()
                .filter(vm -> vm.getLanguage() != null && languageCode.equals(vm.getLanguage().getCode()))
                .collect(Collectors.toList());
    }

    /**
     * Gets variant ViewModels for the specified language codes in the given order.
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
     */
    public boolean hasTranslationForLanguage(String languageCode) {
        return variantViewModels.stream()
                .anyMatch(vm -> vm.getLanguage() != null &&
                        languageCode.equals(vm.getLanguage().getCode()) &&
                        vm.getVariant().getOriginalText() != null &&
                        !vm.getVariant().getOriginalText().isEmpty());
    }

    /**
     * Checks if this translation entry has changes for the specified language.
     */
    public boolean hasChangesForLanguage(String languageCode) {
        return variantViewModels.stream()
                .anyMatch(vm -> vm.getLanguage() != null &&
                        languageCode.equals(vm.getLanguage().getCode()) &&
                        vm.getVariant().isChanged());
    }

    /**
     * Property indicating if this entry has any changes.
     */
    public boolean getHasChanges() {
        return hasChanges.get();
    }

    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    /**
     * Refreshes the variant ViewModels from the underlying entry.
     */
    public void refresh() {
        refreshVariantViewModels();
        updateHasChangesProperty();
    }

    /**
     * Builds language presence and changes maps for table display.
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
     * Builds language changes map for table display.
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
     */
    public PZTranslationType getType() {
        return entry.getType();
    }

    /**
     * Returns all types present in this entry (from its variants).
     */
    public Set<PZTranslationType> getTypes() {
        return entry.getVariants().stream()
                .map(PZTranslationVariant::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void refreshVariantViewModels() {
        // Clear current ViewModels
        variantViewModels.clear();

        // Create ViewModels for all variants
        for (PZTranslationVariant variant : entry.getVariants()) {
            TranslationVariantViewModel viewModel = TranslationVariantViewModelRegistry.getViewModel(variant);
            variantViewModels.add(viewModel);

            // Add listener for changes in this variant ViewModel
            viewModel.changedProperty().addListener((obs, oldVal, newVal) -> {
                updateHasChangesProperty();
            });
        }
    }

    private void updateHasChangesProperty() {
        boolean hasAnyChanges = variantViewModels.stream()
                .anyMatch(vm -> vm.getVariant().isChanged());
        hasChanges.set(hasAnyChanges);
    }
}
