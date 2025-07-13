package org.pz.polyglot.models;

import org.pz.polyglot.models.translations.PZTranslationVariant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class TranslationSession {
    private static final TranslationSession INSTANCE = new TranslationSession();
    private final ObservableSet<PZTranslationVariant> variants = FXCollections.observableSet();

    private TranslationSession() {
    }

    public static TranslationSession getInstance() {
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
