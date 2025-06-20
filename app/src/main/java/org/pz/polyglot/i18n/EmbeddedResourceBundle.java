package org.pz.polyglot.i18n;

import java.util.*;

public class EmbeddedResourceBundle extends ResourceBundle {
    private final Map<String, String> translations;
    private static final Map<String, String> ENGLISH_TRANSLATIONS = TranslationProvider.getEnglishTranslations();

    public EmbeddedResourceBundle(Map<String, String> translations) {
        this.translations = translations;
    }

    @Override
    protected Object handleGetObject(String key) {
        Object value = translations.get(key);
        if (value == null && translations != ENGLISH_TRANSLATIONS) {
            value = ENGLISH_TRANSLATIONS.get(key);
        }
        return value;
    }

    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<>(translations.keySet());
        keys.addAll(ENGLISH_TRANSLATIONS.keySet());
        return Collections.enumeration(keys);
    }
}
