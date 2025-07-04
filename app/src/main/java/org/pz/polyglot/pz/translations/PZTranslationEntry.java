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

    public PZTranslationVariant addVariant(PZTranslationFile file, String text, Charset charset, int startLine,
            int endLine) {
        Charset supposedCharset = file.getLanguage().getCharset(file.getSource().getVersion()).orElse(null);
        PZTranslationVariant variant = new PZTranslationVariant(this, file, text, supposedCharset, charset, startLine,
                endLine);
        translations.add(variant);
        return variant;
    }
}
