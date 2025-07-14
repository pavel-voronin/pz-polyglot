package org.pz.polyglot.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import org.pz.polyglot.models.translations.PZTranslationVariant;

/**
 * Singleton class managing the current translation session.
 * Holds the set of translation variants being edited or processed.
 */
public class TranslationSession {
    /**
     * The single instance of TranslationSession.
     */
    private static final TranslationSession INSTANCE = new TranslationSession();

    /**
     * The set of translation variants in the current session.
     */
    private final ObservableSet<PZTranslationVariant> variants = FXCollections.observableSet();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TranslationSession() {
    }

    /**
     * Returns the singleton instance of TranslationSession.
     * 
     * @return the TranslationSession instance
     */
    public static TranslationSession getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the set of translation variants in the session.
     * 
     * @return ObservableSet of PZTranslationVariant
     */
    public ObservableSet<PZTranslationVariant> getVariants() {
        return variants;
    }

    /**
     * Adds a translation variant to the session.
     * 
     * @param variant the translation variant to add
     */
    public void addVariant(PZTranslationVariant variant) {
        variants.add(variant);
    }

    /**
     * Removes a translation variant from the session.
     * 
     * @param variant the translation variant to remove
     */
    public void removeVariant(PZTranslationVariant variant) {
        variants.remove(variant);
    }
}
