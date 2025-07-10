package org.pz.polyglot.pz.translations;

import java.nio.charset.Charset;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.sources.PZSource;

public class PZTranslationVariant {
    private final PZTranslationEntry key;
    private String originalText;
    private String editedText;
    private PZTranslationFile file;
    private final PZSource source;
    private final PZLanguage language;
    private final PZTranslationType type;
    private final Charset supposedCharset;
    private final Charset usedCharset;
    private final int startLine;
    private final int endLine;
    private final boolean isNew;

    public PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text, Charset supposedCharset,
            Charset detectedCharset, int startLine, int endLine) {
        this(key, file, text, supposedCharset, detectedCharset, startLine, endLine, false);
    }

    public PZTranslationVariant(PZTranslationEntry key, PZSource source, PZLanguage language, PZTranslationType type,
            String text, Charset supposedCharset, Charset detectedCharset) {
        this(key, null, source, language, type, text, supposedCharset, detectedCharset, 0, 0, true);
    }

    private PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text, Charset supposedCharset,
            Charset detectedCharset, int startLine, int endLine, boolean isNew) {
        this(key, file,
                file != null ? file.getSource() : null,
                file != null ? file.getLanguage() : null,
                file != null ? file.getType() : null,
                text, supposedCharset, detectedCharset, startLine, endLine, isNew);
    }

    private PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, PZSource source, PZLanguage language,
            PZTranslationType type, String text, Charset supposedCharset, Charset detectedCharset,
            int startLine, int endLine, boolean isNew) {
        this.key = key;
        this.file = file;
        this.source = source;
        this.language = language;
        this.type = type;
        this.originalText = text;
        this.editedText = text;
        this.supposedCharset = supposedCharset;
        this.usedCharset = detectedCharset;
        this.startLine = startLine;
        this.endLine = endLine;
        this.isNew = isNew;
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

    public PZTranslationFile getFile() {
        return file;
    }

    public void setFile(PZTranslationFile file) {
        this.file = file;
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

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public boolean isNew() {
        return isNew;
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
