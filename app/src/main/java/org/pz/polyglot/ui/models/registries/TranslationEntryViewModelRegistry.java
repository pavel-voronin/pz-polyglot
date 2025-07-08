package org.pz.polyglot.ui.models.registries;

import java.util.Map;
import java.util.WeakHashMap;

import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.ui.models.TranslationEntryViewModel;

/**
 * Registry for managing TranslationEntryViewModel instances.
 * Uses WeakHashMap to allow garbage collection when entries are no longer
 * referenced.
 */
public class TranslationEntryViewModelRegistry {
    private static final Map<PZTranslationEntry, TranslationEntryViewModel> cache = new WeakHashMap<>();

    private TranslationEntryViewModelRegistry() {
    }

    /**
     * Gets or creates a ViewModel for the specified translation entry.
     * 
     * @param entry the translation entry
     * @return the corresponding ViewModel
     */
    public static TranslationEntryViewModel getViewModel(PZTranslationEntry entry) {
        return cache.computeIfAbsent(entry, TranslationEntryViewModel::new);
    }

    /**
     * Gets the current cache size for debugging purposes.
     * 
     * @return the number of cached ViewModels
     */
    public static int getCacheSize() {
        return cache.size();
    }

    /**
     * Clears the cache. Use with caution as this will force recreation of all
     * ViewModels.
     */
    public static void clearCache() {
        cache.clear();
    }
}
