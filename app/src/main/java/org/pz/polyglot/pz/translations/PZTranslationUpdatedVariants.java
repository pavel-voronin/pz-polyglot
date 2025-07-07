package org.pz.polyglot.pz.translations;

import java.util.HashSet;

public class PZTranslationUpdatedVariants {
    private static final PZTranslationUpdatedVariants INSTANCE = new PZTranslationUpdatedVariants();
    private HashSet<PZTranslationVariant> variants = new HashSet<>();

    private PZTranslationUpdatedVariants() {
    }

    public static PZTranslationUpdatedVariants getInstance() {
        return INSTANCE;
    }

    public HashSet<PZTranslationVariant> getVariants() {
        return variants;
    }

    public void addVariant(PZTranslationVariant variant) {
        variants.add(variant);
    }

    public void removeVariant(PZTranslationVariant variant) {
        variants.remove(variant);
    }
}
