package org.pz.polyglot.pz.translations;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.sources.PZSource;

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
}
