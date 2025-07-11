package org.pz.polyglot.models.translations;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.sources.PZSource;

public class PZTranslationEntry {
    private final String key;
    private ArrayList<PZTranslationVariant> variants = new ArrayList<>();

    public PZTranslationEntry(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<PZTranslationVariant> getVariants() {
        return variants;
    }

    public ArrayList<PZTranslationVariant> getChangedVariants() {
        return variants.stream().filter(variant -> variant.isChanged())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public PZTranslationVariant addVariant(PZSource source, PZLanguage language, PZTranslationType type, String text,
            Charset charset) {
        Charset supposedCharset = language.getCharset(source.getVersion()).orElse(null);
        PZTranslationVariant variant = new PZTranslationVariant(this, source, language, type, text, supposedCharset,
                charset);
        variants.add(variant);
        return variant;
    }

    /**
     * Returns the type of this entry (from the first variant), or null if none
     * exist.
     */
    public PZTranslationType getType() {
        return variants.isEmpty() ? null : variants.get(0).getType();
    }
}
