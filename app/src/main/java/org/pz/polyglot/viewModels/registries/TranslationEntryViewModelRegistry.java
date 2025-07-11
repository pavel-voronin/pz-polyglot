package org.pz.polyglot.viewModels.registries;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.pz.polyglot.components.SystemMonitor;
import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;

/**
 * Registry for managing TranslationEntryViewModel instances.
 * Uses WeakHashMap to allow garbage collection when entries are no longer
 * referenced.
 * Now supports lazy creation and removal for virtualization.
 */
public class TranslationEntryViewModelRegistry {
    private static final Map<PZTranslationEntry, TranslationEntryViewModel> cache = new WeakHashMap<>();
    private static Function<PZTranslationEntry, TranslationEntryViewModel> factory = TranslationEntryViewModel::new;
    static {
        SystemMonitor.addHook(() -> "EntryViewModel cache size: " + cache.size());
    }

    private TranslationEntryViewModelRegistry() {
    }

    /**
     * Gets or creates a ViewModel for the specified translation entry.
     * If virtualization is enabled, only visible entries are kept in cache.
     * 
     * @param entry the translation entry
     * @return the corresponding ViewModel
     */
    public static TranslationEntryViewModel getViewModel(PZTranslationEntry entry) {
        return cache.computeIfAbsent(entry, factory);
    }

    /**
     * Removes a ViewModel from the cache (for virtualization cleanup).
     */
    public static void removeViewModel(PZTranslationEntry entry) {
        cache.remove(entry);
    }

    /**
     * Sets a custom factory for ViewModel creation (for testing or advanced
     * virtualization).
     */
    public static void setFactory(Function<PZTranslationEntry, TranslationEntryViewModel> customFactory) {
        factory = customFactory;
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
