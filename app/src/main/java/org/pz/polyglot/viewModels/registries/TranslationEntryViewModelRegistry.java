package org.pz.polyglot.viewModels.registries;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.pz.polyglot.models.translations.PZTranslationEntry;
import org.pz.polyglot.viewModels.TranslationEntryViewModel;

/**
 * Registry for managing TranslationEntryViewModel instances.
 * Uses WeakHashMap to allow garbage collection when entries are no longer
 * referenced.
 * Now supports lazy creation and removal for virtualization.
 */
/**
 * Registry for managing {@link TranslationEntryViewModel} instances associated
 * with {@link PZTranslationEntry}.
 * <p>
 * Uses a {@link WeakHashMap} to allow garbage collection of entries when no
 * longer referenced.
 * Supports lazy creation and removal for UI virtualization scenarios.
 */
public class TranslationEntryViewModelRegistry {
    /**
     * Cache mapping translation entries to their corresponding view models.
     * Uses weak references to allow garbage collection when entries are no longer
     * in use.
     */
    private static final Map<PZTranslationEntry, TranslationEntryViewModel> cache = new WeakHashMap<>();

    /**
     * Factory function for creating new {@link TranslationEntryViewModel}
     * instances.
     * Can be replaced for testing or advanced virtualization.
     */
    private static Function<PZTranslationEntry, TranslationEntryViewModel> factory = TranslationEntryViewModel::new;

    /**
     * Private constructor to prevent instantiation.
     */
    private TranslationEntryViewModelRegistry() {
    }

    /**
     * Returns the {@link TranslationEntryViewModel} for the specified
     * {@link PZTranslationEntry}.
     * If not present, creates a new instance using the factory and caches it.
     * <p>
     * Only visible entries should be kept in cache when virtualization is enabled.
     *
     * @param entry the translation entry
     * @return the corresponding view model
     */
    public static TranslationEntryViewModel getViewModel(PZTranslationEntry entry) {
        return cache.computeIfAbsent(entry, factory);
    }

    /**
     * Removes the {@link TranslationEntryViewModel} associated with the given entry
     * from the cache.
     * Used for cleanup during UI virtualization.
     *
     * @param entry the translation entry whose view model should be removed
     */
    public static void removeViewModel(PZTranslationEntry entry) {
        cache.remove(entry);
    }

    /**
     * Sets a custom factory for creating {@link TranslationEntryViewModel}
     * instances.
     * Useful for testing or advanced virtualization scenarios.
     *
     * @param customFactory the factory function to use
     */
    public static void setFactory(Function<PZTranslationEntry, TranslationEntryViewModel> customFactory) {
        factory = customFactory;
    }

    /**
     * Returns the current number of cached view models.
     * Useful for debugging and monitoring cache usage.
     *
     * @return the number of cached view models
     */
    public static int getCacheSize() {
        return cache.size();
    }

    /**
     * Clears all cached view models.
     * <p>
     * Use with caution: this will force recreation of all view models and may
     * impact performance.
     */
    public static void clearCache() {
        cache.clear();
    }
}
