package org.pz.polyglot.components;

import java.util.function.Consumer;

import org.pz.polyglot.models.languages.PZLanguage;

/**
 * A tag component specialized for displaying a language.
 * Accepts a domain language model and displays its code and name.
 */
public class LanguageTag extends Tag {
    /**
     * Creates a new LanguageTag with the specified language.
     *
     * @param language the language to display
     */
    public LanguageTag(PZLanguage language) {
        this(language, null);
    }

    /**
     * Creates a new LanguageTag with the specified language and click callback.
     *
     * @param language        the language to display
     * @param onClickCallback optional callback to execute when the tag is clicked
     */
    public LanguageTag(PZLanguage language, Consumer<Tag> onClickCallback) {
        super(language.getCode(), language.getName(), onClickCallback);
    }
}
