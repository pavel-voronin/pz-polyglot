package org.pz.polyglot.models.translations;

import java.nio.charset.Charset;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.sources.PZSource;

public class PZTranslationVariant {
    private final PZTranslationEntry key;
    private String originalText;
    private String editedText;
    private final PZSource source;
    private final PZLanguage language;
    private final PZTranslationType type;
    private final Charset supposedCharset;
    private final Charset usedCharset;

    public PZTranslationVariant(PZTranslationEntry key, PZSource source, PZLanguage language, PZTranslationType type,
            String text, Charset supposedCharset, Charset detectedCharset) {
        this.key = key;
        this.source = source;
        this.language = language;
        this.type = type;
        this.originalText = text;
        this.editedText = text;
        this.supposedCharset = supposedCharset;
        this.usedCharset = detectedCharset;
    }

    public PZTranslationEntry getKey() {
        return key;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String text) {
        this.originalText = text;
    }

    public PZSource getSource() {
        return source;
    }

    public PZLanguage getLanguage() {
        return language;
    }

    public PZTranslationType getType() {
        return type;
    }

    public Charset getSupposedCharset() {
        return supposedCharset;
    }

    public Charset getUsedCharset() {
        return usedCharset;
    }

    public String getEditedText() {
        return this.editedText;
    }

    public void setEditedText(String editedText) {
        this.editedText = editedText;
        if (this.isChanged()) {
            PZTranslationSession.getInstance().addVariant(this);
        } else {
            PZTranslationSession.getInstance().removeVariant(this);
        }
    }

    public boolean isChanged() {
        return !this.editedText.equals(this.originalText);
    }

    public void reset() {
        this.setEditedText(this.originalText);
    }

    public void markSaved() {
        this.originalText = editedText;
        PZTranslationSession.getInstance().removeVariant(this);
    }
}
