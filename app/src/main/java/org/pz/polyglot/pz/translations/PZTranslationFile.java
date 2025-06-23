package org.pz.polyglot.pz.translations;

import java.nio.file.Path;

import org.pz.polyglot.pz.languages.PZLanguage;

public class PZTranslationFile {
    private final Path path;
    private final PZTranslationType type;
    private final PZLanguage language;

    public PZTranslationFile(Path path, PZTranslationType type, PZLanguage language) {
        this.path = path;
        this.type = type;
        this.language = language;
    }

    public Path getPath() {
        return path;
    }

    public PZTranslationType getType() {
        return type;
    }

    public PZLanguage getLanguage() {
        return language;
    }
}
