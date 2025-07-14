package org.pz.polyglot.components;

import java.util.function.Consumer;

import org.pz.polyglot.models.languages.PZLanguage;

/**
 * Represents a tag component specialized for displaying a language.
 * Accepts a domain language model and displays its code and name.
 */
public class LanguageTag extends Tag {

    /**
     * Constructs a LanguageTag for the specified language.
     * This constructor does not set a click callback.
     *
     * @param language the language to display
     */
    public LanguageTag(PZLanguage language) {
        this(language, null);
    }

    /**
     * Constructs a LanguageTag for the specified language and click callback.
     *
     * @param language        the language to display
     * @param onClickCallback the callback to execute when the tag is clicked, or
     *                        {@code null} if no callback is needed
     */
    public LanguageTag(PZLanguage language, Consumer<Tag> onClickCallback) {
        super(language.getCode(), language.getName(), onClickCallback);
    }
}
