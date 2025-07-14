package org.pz.polyglot.models.translations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Singleton class that manages all translation entries for the application.
 */
public class PZTranslations {
    /**
     * The singleton instance of {@code PZTranslations}.
     */
    private static PZTranslations instance;

    /**
     * Stores all translation entries mapped by their keys.
     */
    private final ObservableMap<String, PZTranslationEntry> translations = FXCollections.observableHashMap();

    /**
     * Returns the singleton instance of {@code PZTranslations}.
     * If the instance does not exist, it is created.
     *
     * @return the singleton instance
     */
    public static PZTranslations getInstance() {
        if (instance == null) {
            instance = new PZTranslations();
        }
        return instance;
    }

    /**
     * Retrieves the translation entry for the specified key, creating it if it does
     * not exist.
     *
     * @param key the translation key
     * @return the translation entry associated with the key
     */
    public PZTranslationEntry getOrCreateTranslation(String key) {
        // Creates a new entry if the key is not present
        return this.translations.computeIfAbsent(key, k -> new PZTranslationEntry(k));
    }

    /**
     * Returns all translation entries.
     *
     * @return an observable map of all translation entries
     */
    public ObservableMap<String, PZTranslationEntry> getAllTranslations() {
        return translations;
    }
}
