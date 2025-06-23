package org.pz.polyglot.pz.translations;

import java.util.ArrayList;

public class PZTranslationEntry {
    private final String key;
    private ArrayList<PZTranslationVariant> translations = new ArrayList<>();

    public PZTranslationEntry(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<PZTranslationVariant> getTranslations() {
        return translations;
    }

    public PZTranslationVariant addVariant(PZTranslationFile file, String text) {
        PZTranslationVariant variant = new PZTranslationVariant(this, file, text);
        translations.add(variant);
        return variant;
    }
}
