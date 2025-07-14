package org.pz.polyglot.models.translations;

import java.nio.charset.Charset;

import org.pz.polyglot.models.TranslationSession;
import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.sources.PZSource;

/**
 * Represents a translation variant for a specific key, language, and source.
 * Holds both the original and edited text, along with charset information.
 */
public class PZTranslationVariant {
    /** The translation entry key associated with this variant. */
    private final PZTranslationEntry key;

    /** The original text of the translation variant. */
    private String originalText;

    /** The edited text of the translation variant. */
    private String editedText;

    /** The source from which this translation variant originates. */
    private final PZSource source;

    /** The language of this translation variant. */
    private final PZLanguage language;

    /** The type of translation (e.g., machine, human, etc.). */
    private final PZTranslationType type;

    /** The charset that is supposed to be used for this variant. */
    private final Charset supposedCharset;

    /** The charset that was actually detected and used. */
    private final Charset usedCharset;

    /**
     * Constructs a new translation variant.
     *
     * @param key             the translation entry key
     * @param source          the source of the translation
     * @param language        the language of the translation
     * @param type            the type of translation
     * @param text            the original and initial edited text
     * @param supposedCharset the expected charset
     * @param detectedCharset the detected charset
     */
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

    /**
     * Gets the translation entry key.
     * 
     * @return the translation entry key
     */
    public PZTranslationEntry getKey() {
        return key;
    }

    /**
     * Gets the original text of the translation variant.
     * 
     * @return the original text
     */
    public String getOriginalText() {
        return originalText;
    }

    /**
     * Sets the original text of the translation variant.
     * 
     * @param text the new original text
     */
    public void setOriginalText(String text) {
        this.originalText = text;
    }

    /**
     * Gets the source of the translation variant.
     * 
     * @return the source
     */
    public PZSource getSource() {
        return source;
    }

    /**
     * Gets the language of the translation variant.
     * 
     * @return the language
     */
    public PZLanguage getLanguage() {
        return language;
    }

    /**
     * Gets the type of translation.
     * 
     * @return the translation type
     */
    public PZTranslationType getType() {
        return type;
    }

    /**
     * Gets the supposed charset for this variant.
     * 
     * @return the supposed charset
     */
    public Charset getSupposedCharset() {
        return supposedCharset;
    }

    /**
     * Gets the charset that was actually used/detected.
     * 
     * @return the used charset
     */
    public Charset getUsedCharset() {
        return usedCharset;
    }

    /**
     * Gets the edited text of the translation variant.
     * 
     * @return the edited text
     */
    public String getEditedText() {
        return this.editedText;
    }

    /**
     * Sets the edited text and updates the translation session accordingly.
     * If the text is changed, adds this variant to the session; otherwise removes
     * it.
     * 
     * @param editedText the new edited text
     */
    public void setEditedText(String editedText) {
        this.editedText = editedText;
        // Update session with changed variants
        if (this.isChanged()) {
            TranslationSession.getInstance().addVariant(this);
        } else {
            TranslationSession.getInstance().removeVariant(this);
        }
    }

    /**
     * Checks if the edited text differs from the original text.
     * 
     * @return true if changed, false otherwise
     */
    public boolean isChanged() {
        return !this.editedText.equals(this.originalText);
    }

    /**
     * Resets the edited text to the original text.
     */
    public void reset() {
        this.setEditedText(this.originalText);
    }

    /**
     * Marks the variant as saved, updating the original text and removing from
     * session.
     */
    public void markSaved() {
        this.originalText = editedText;
        TranslationSession.getInstance().removeVariant(this);
    }
}
