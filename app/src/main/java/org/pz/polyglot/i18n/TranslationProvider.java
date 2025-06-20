package org.pz.polyglot.i18n;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class TranslationProvider {
    private static final Logger logger = Logger.getLogger(TranslationProvider.class.getName());
    private static Map<String, String> ENGLISH_TRANSLATIONS = new HashMap<>();
    static {
        ENGLISH_TRANSLATIONS.put("language-dialog.title", "Select Project Zomboid Folders");
        ENGLISH_TRANSLATIONS.put("language-dialog.game.label", "Project Zomboid game folder:");
        ENGLISH_TRANSLATIONS.put("language-dialog.steam.label", "Steam mods folder:");
        ENGLISH_TRANSLATIONS.put("language-dialog.user.label", "User mods folder:");
        ENGLISH_TRANSLATIONS.put("language-dialog.browse.button", "Browse...");
        ENGLISH_TRANSLATIONS.put("language-dialog.ok.button", "OK");
        ENGLISH_TRANSLATIONS.put("language-dialog.choose.game.title", "Select Project Zomboid game folder");
        ENGLISH_TRANSLATIONS.put("language-dialog.choose.steam.title", "Select Steam mods folder");
        ENGLISH_TRANSLATIONS.put("language-dialog.choose.user.title", "Select user mods folder");

        ENGLISH_TRANSLATIONS.put("language-not-set-alert.exit.title", "Exit Confirmation");
        ENGLISH_TRANSLATIONS.put("language-not-set-alert.exit.header", "Folders not selected");
        ENGLISH_TRANSLATIONS.put("language-not-set-alert.exit.message",
                "Are you sure you want to exit the application?");

        ENGLISH_TRANSLATIONS.put("app.title", "PZ Polyglot");

        ENGLISH_TRANSLATIONS.put("menu.file", "File");
        ENGLISH_TRANSLATIONS.put("menu.help", "Help");
        ENGLISH_TRANSLATIONS.put("menu.quit", "Quit");
        ENGLISH_TRANSLATIONS.put("menu.about", "About");
        ENGLISH_TRANSLATIONS.put("menu.documentation", "Documentation");
        ENGLISH_TRANSLATIONS.put("menu.discord", "Discord");
    }

    private static final Map<Locale, Map<String, String>> externalTranslations = new HashMap<>();

    public static ResourceBundle getBundle(Locale locale) {
        Map<String, String> translations = getTranslationsForLocale(locale);
        return new EmbeddedResourceBundle(translations);
    }

    private static Map<String, String> getTranslationsForLocale(Locale locale) {
        Map<String, String> translations = ENGLISH_TRANSLATIONS;
        Map<String, String> external = loadExternalTranslations(locale);
        if (external != null && !external.isEmpty()) {
            Map<String, String> combined = new HashMap<>(translations);
            combined.putAll(external);
            return combined;
        }
        return translations;
    }

    private static Map<String, String> loadExternalTranslations(Locale locale) {
        if (externalTranslations.containsKey(locale)) {
            return externalTranslations.get(locale);
        }
        String fileName = "messages_" + locale.toString() + ".properties";
        File externalFile = new File(fileName);
        if (!externalFile.exists()) {
            fileName = "messages_" + locale.getLanguage() + ".properties";
            externalFile = new File(fileName);
        }
        Map<String, String> translations = null;
        if (externalFile.exists()) {
            translations = loadPropertiesFile(externalFile);
            System.out.println("Loaded external translations from: " + externalFile.getAbsolutePath());
        }
        externalTranslations.put(locale, translations);
        return translations;
    }

    private static Map<String, String> loadPropertiesFile(File file) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            props.load(reader);
            Map<String, String> map = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                map.put(key, props.getProperty(key));
            }
            return map;
        } catch (IOException e) {
            logger.severe(
                    "Failed to load external translations from " + file.getAbsolutePath() + ": " + e.getMessage());
            return null;
        }
    }

    public static void reloadExternalTranslations() {
        externalTranslations.clear();
    }

    public static List<Locale> getAvailableLocales() {
        Set<Locale> locales = new HashSet<>();
        locales.add(Locale.ENGLISH);
        locales.add(Locale.forLanguageTag("ru"));
        File currentDir = new File(".");
        File[] files = currentDir
                .listFiles((dir, name) -> name.startsWith("messages_") && name.endsWith(".properties"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String localeStr = name.substring("messages_".length(), name.length() - ".properties".length());
                try {
                    Locale locale = Locale.forLanguageTag(localeStr.replace('_', '-'));
                    locales.add(locale);
                } catch (Exception e) {
                    logger.warning("Invalid locale in filename: " + name);
                }
            }
        }
        return new ArrayList<>(locales);
    }

    // Make English translations accessible for fallback
    public static Map<String, String> getEnglishTranslations() {
        return ENGLISH_TRANSLATIONS;
    }
}
