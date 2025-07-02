package org.pz.polyglot.pz.translations;

import java.nio.charset.Charset;
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

    public PZTranslationVariant addVariant(PZTranslationFile file, String text, Charset charset) {
        PZTranslationVariant variant = new PZTranslationVariant(this, file, text, charset);
        translations.add(variant);
        return variant;
    }
}
