package org.pz.polyglot.i18n;

import java.text.MessageFormat;
import java.util.*;

public class I18nManager {
    private static I18nManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    private I18nManager() {
        this.currentLocale = Locale.getDefault();
        loadBundle();
    }

    public static I18nManager getInstance() {
        if (instance == null) {
            instance = new I18nManager();
        }
        return instance;
    }

    private void loadBundle() {
        try {
            bundle = TranslationProvider.getBundle(currentLocale);
        } catch (Exception e) {
            bundle = TranslationProvider.getBundle(Locale.ENGLISH);
        }
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    public String getString(String key, Object... params) {
        String pattern = getString(key);
        return MessageFormat.format(pattern, params);
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        Locale.setDefault(locale);
        loadBundle();
    }

    public void setLocale(String localeCode) {
        if (localeCode == null || localeCode.trim().isEmpty()) {
            this.currentLocale = Locale.ENGLISH;
        } else {
            Locale locale = Locale.forLanguageTag(localeCode.replace('_', '-'));
            if (locale.getLanguage().isEmpty()) {
                this.currentLocale = Locale.ENGLISH;
            } else {
                this.currentLocale = locale;
            }
        }
        Locale.setDefault(this.currentLocale);
        loadBundle();
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public List<Locale> getSupportedLocales() {
        return TranslationProvider.getAvailableLocales();
    }

    public void reloadTranslations() {
        TranslationProvider.reloadExternalTranslations();
        loadBundle();
    }

    public ResourceBundle getBundle() {
        return bundle;
    }
}
