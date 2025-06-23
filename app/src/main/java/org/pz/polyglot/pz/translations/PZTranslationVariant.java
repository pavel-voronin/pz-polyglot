package org.pz.polyglot.pz.translations;

public class PZTranslationVariant {
    private final PZTranslationEntry key;
    private String text;
    private PZTranslationFile file;

    public PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text) {
        this.key = key;
        this.file = file;
        this.text = text;
    }

    public PZTranslationEntry getKey() {
        return key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PZTranslationFile getFile() {
        return file;
    }
}
