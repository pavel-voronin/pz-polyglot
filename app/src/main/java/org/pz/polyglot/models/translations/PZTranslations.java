package org.pz.polyglot.models.translations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class PZTranslations {
    private static PZTranslations instance;
    private final ObservableMap<String, PZTranslationEntry> translations = FXCollections.observableHashMap();

    public static PZTranslations getInstance() {
        if (instance == null) {
            instance = new PZTranslations();
        }
        return instance;
    }

    public PZTranslationEntry getOrCreateTranslation(String key) {
        return this.translations.computeIfAbsent(key, k -> new PZTranslationEntry(k));
    }

    public ObservableMap<String, PZTranslationEntry> getAllTranslations() {
        return translations;
    }
}
