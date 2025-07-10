package org.pz.polyglot.pz.translations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class PZTranslationSession {
    private static final PZTranslationSession INSTANCE = new PZTranslationSession();
    private final ObservableSet<PZTranslationVariant> variants = FXCollections.observableSet();

    private PZTranslationSession() {
    }

    public static PZTranslationSession getInstance() {
        return INSTANCE;
    }

    public ObservableSet<PZTranslationVariant> getVariants() {
        return variants;
    }

    public void addVariant(PZTranslationVariant variant) {
        variants.add(variant);
    }

    public void removeVariant(PZTranslationVariant variant) {
        variants.remove(variant);
    }
}
