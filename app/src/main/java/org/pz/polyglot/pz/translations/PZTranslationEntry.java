package org.pz.polyglot.pz.translations;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    public PZTranslationVariant addVariant(PZTranslationFile file, String text, Charset charset, int startLine,
            int endLine) {
        Charset supposedCharset = file.getLanguage().getCharset(file.getSource().getVersion()).orElse(null);
        PZTranslationVariant variant = new PZTranslationVariant(this, file, text, supposedCharset, charset, startLine,
                endLine);
        variants.add(variant);
        return variant;
    }
}
