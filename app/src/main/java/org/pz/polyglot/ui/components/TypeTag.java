package org.pz.polyglot.ui.components;

import org.pz.polyglot.pz.translations.PZTranslationType;

public class TypeTag extends Tag {
    public TypeTag(PZTranslationType type) {
        super(Tag.Theme.PURPLE, type.name(), null, null);
    }
}
