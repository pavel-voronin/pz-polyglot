package org.pz.polyglot.pz.translations;

import java.nio.charset.Charset;

public class PZTranslationVariant {
    private final PZTranslationEntry key;
    private String originalText;
    private String editedText;
    private PZTranslationFile file;
    private final Charset supposedCharset;
    private final Charset usedCharset;
    private final int startLine;
    private final int endLine;
    private final boolean isNew;

    public PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text, Charset supposedCharset,
            Charset detectedCharset) {
        this(key, file, text, supposedCharset, detectedCharset, -1, -1, true);
    }

    public PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text, Charset supposedCharset,
            Charset detectedCharset, int startLine, int endLine) {
        this(key, file, text, supposedCharset, detectedCharset, startLine, endLine, false);
    }

    public PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text, Charset supposedCharset,
            Charset detectedCharset, int startLine, int endLine, boolean isNew) {
        this.key = key;
        this.file = file;
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
