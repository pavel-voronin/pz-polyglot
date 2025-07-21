package org.pz.polyglot.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import org.pz.polyglot.components.SystemMonitor;
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
     * The set of translation keys in the current session.
     */
    private final ObservableList<String> sessionKeys = FXCollections.observableArrayList();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TranslationSession() {
        SystemMonitor.addHook(() -> "Session keys: " + sessionKeys.size() +
                ", Variants: " + variants.size());
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
     * Gets the observable list of translation keys in the session.
     *
     * @return ObservableList of String keys
     */
    public ObservableList<String> getSessionKeys() {
        return sessionKeys;
    }

    /**
     * Adds a translation key to the session if not already present.
     *
     * @param key the translation key to add
     */
    public void addSessionKey(String key) {
        if (key != null && !sessionKeys.contains(key)) {
            sessionKeys.add(key);
        }
    }

    /**
     * Removes a translation key from the session.
     *
     * @param key the translation key to remove
     */
    public void removeSessionKey(String key) {
        sessionKeys.remove(key);
    }

    /**
     * Returns the number of translation keys in the session.
     *
     * @return the count of session keys
     */
    public int getSessionKeyCount() {
        return sessionKeys.size();
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
