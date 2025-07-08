package org.pz.polyglot.pz.translations;

import java.util.HashSet;

public class PZTranslationSession {
    private static final PZTranslationSession INSTANCE = new PZTranslationSession();
    private HashSet<PZTranslationVariant> variants = new HashSet<>();

    private PZTranslationSession() {
    }

    public static PZTranslationSession getInstance() {
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
