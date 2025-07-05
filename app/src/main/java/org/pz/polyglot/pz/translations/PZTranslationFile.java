package org.pz.polyglot.pz.translations;

import java.nio.file.Path;
import java.util.ArrayList;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.sources.PZSource;

public class PZTranslationFile {
    private final Path path;
    private final PZTranslationType type;
    private final PZLanguage language;
    private final PZSource source;
    private ArrayList<PZTranslationVariant> variants = new ArrayList<>();

    public PZTranslationFile(Path path, PZTranslationType type, PZLanguage language, PZSource source) {
        this.path = path;
        this.type = type;
        this.language = language;
        this.source = source;
    }

    public Path getPath() {
        return path;
    }

    public ArrayList<PZTranslationVariant> getVariants() {
        return variants;
    }

    public void addVariant(PZTranslationVariant variant) {
        variants.add(variant);
    }

    public PZTranslationType getType() {
        return type;
    }

    public PZLanguage getLanguage() {
        return language;
    }

    public PZSource getSource() {
        return source;
    }
}
