package org.pz.polyglot.ui.models.registries;

import java.util.Map;
import java.util.WeakHashMap;

import org.pz.polyglot.pz.translations.PZTranslationVariant;
import org.pz.polyglot.ui.components.SystemMonitor;
import org.pz.polyglot.ui.models.TranslationVariantViewModel;

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
