package org.pz.polyglot.pz.translations;

public class PZTranslationVariant {
    private final PZTranslationEntry key;
    private String text; // Original text
    private String editedText; // Store edited text
    private boolean isEdited; // Track if this variant has been edited
    private PZTranslationFile file;

    public PZTranslationVariant(PZTranslationEntry key, PZTranslationFile file, String text) {
        this.key = key;
        this.file = file;
        this.text = text;
        this.editedText = null;
        this.isEdited = false;
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

    /**
     * Gets the current display text - returns edited text if available, otherwise original text
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
}
