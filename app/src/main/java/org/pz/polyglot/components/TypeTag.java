package org.pz.polyglot.components;

import org.pz.polyglot.models.translations.PZTranslationType;

public class TypeTag extends Tag {
    public TypeTag(PZTranslationType type) {
        super(Tag.Theme.PURPLE, type.name(), null, null);
    }
}
