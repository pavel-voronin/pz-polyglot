package org.pz.polyglot.viewModels.registries;

import java.util.Map;
import java.util.WeakHashMap;

import org.pz.polyglot.models.translations.PZTranslationVariant;
import org.pz.polyglot.viewModels.TranslationVariantViewModel;

/**
 * Registry for caching and retrieving {@link TranslationVariantViewModel}
 * instances
 * associated with {@link PZTranslationVariant} objects. Uses a
 * {@link WeakHashMap}
 * to avoid memory leaks by allowing garbage collection of unused variants.
 */
public class TranslationVariantViewModelRegistry {
    /**
     * Cache mapping translation variants to their corresponding view models.
     * Uses weak references for keys to prevent memory leaks.
     */
    private static final Map<PZTranslationVariant, TranslationVariantViewModel> cache = new WeakHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private TranslationVariantViewModelRegistry() {
    }

    /**
     * Returns the {@link TranslationVariantViewModel} for the given
     * {@link PZTranslationVariant}.
     * If not present, creates and caches a new instance.
     *
     * @param variant the translation variant
     * @return the corresponding view model
     */
    public static TranslationVariantViewModel getViewModel(PZTranslationVariant variant) {
        // Compute and cache the view model if not already present
        return cache.computeIfAbsent(variant, TranslationVariantViewModel::new);
    }

    /**
     * Returns the current size of the cache.
     *
     * @return number of cached view models
     */
    public static int getCacheSize() {
        return cache.size();
    }
}
