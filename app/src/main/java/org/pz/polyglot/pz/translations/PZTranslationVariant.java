package org.pz.polyglot.pz.translations;

import java.nio.charset.Charset;

public class PZTranslationVariant {
    private final PZTranslationEntry key;
    private String text; // Original text
    private String editedText; // Store edited text
    private boolean isEdited; // Track if this variant has been edited
    private PZTranslationFile file;
    private final Charset supposedCharset;
    private final Charset usedCharset; // Charset used to read this variant
    private final int startLine; // Line number where this translation starts (1-based)
    private final int endLine; // Line number where this translation ends (1-based)
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
        this.text = text;
        this.supposedCharset = supposedCharset;
        this.usedCharset = detectedCharset;
        this.startLine = startLine;
        this.endLine = endLine;
        this.editedText = null;
        this.isEdited = false;
        this.isNew = isNew;
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

    /**
     * Gets the current display text - returns edited text if available, otherwise
     * original text
     */
    public String getCurrentText() {
        return isEdited ? editedText : text;
    }

    /**
     * Gets the original text before any edits
     */
    public String getOriginalText() {
        return text;
    }

    /**
     * Sets the edited text and marks this variant as edited
     */
    public void setEditedText(String editedText) {
        this.editedText = editedText;
        this.isEdited = true;
    }

    /**
     * Checks if this variant has been edited
     */
    public boolean isEdited() {
        return isEdited;
    }

    /**
     * Resets the variant to its original state, removing any edits
     */
    public void resetToOriginal() {
        this.editedText = null;
        this.isEdited = false;
    }

    public void markSaved() {
        this.text = editedText; // Update original text to the edited version
        this.isEdited = false; // Mark as not edited anymore
    }
}
