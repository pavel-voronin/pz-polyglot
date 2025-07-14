package org.pz.polyglot.components;

import org.pz.polyglot.models.translations.PZTranslationType;

/**
 * Represents a tag for a specific translation type in the Polyglot application.
 * Used for UI labeling and categorization of translation types.
 */
public class TypeTag extends Tag {

    /**
     * Constructs a TypeTag for the given translation type.
     * The tag uses the purple theme and the name of the translation type.
     *
     * @param type the translation type to represent
     */
    public TypeTag(PZTranslationType type) {
        super(Tag.Theme.PURPLE, type.name(), null, null);
    }
}
