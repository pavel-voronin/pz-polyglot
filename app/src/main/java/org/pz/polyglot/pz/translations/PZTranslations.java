package org.pz.polyglot.pz.translations;

import java.util.HashMap;

public class PZTranslations {
    private static PZTranslations instance;
    private HashMap<String, PZTranslationEntry> translations = new HashMap<>();

    public static PZTranslations getInstance() {
        if (instance == null) {
            instance = new PZTranslations();
        }

        return instance;
    }

    public PZTranslationEntry getOrCreateTranslation(String key) {
        return this.translations.computeIfAbsent(key, k -> new PZTranslationEntry(k));
    }
}
