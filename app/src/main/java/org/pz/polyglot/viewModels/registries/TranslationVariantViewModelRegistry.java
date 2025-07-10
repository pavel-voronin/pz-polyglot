package org.pz.polyglot.viewModels.registries;

import java.util.Map;
import java.util.WeakHashMap;

import org.pz.polyglot.components.SystemMonitor;
import org.pz.polyglot.models.translations.PZTranslationVariant;
import org.pz.polyglot.viewModels.TranslationVariantViewModel;

public class TranslationVariantViewModelRegistry {
    private static final Map<PZTranslationVariant, TranslationVariantViewModel> cache = new WeakHashMap<>();

    static {
        SystemMonitor.addHook(() -> "Translation Variants VMs: " + getCacheSize());
    }

    private TranslationVariantViewModelRegistry() {
    }

    public static TranslationVariantViewModel getViewModel(PZTranslationVariant variant) {
        return cache.computeIfAbsent(variant, TranslationVariantViewModel::new);
    }

    public static int getCacheSize() {
        return cache.size();
    }
}
