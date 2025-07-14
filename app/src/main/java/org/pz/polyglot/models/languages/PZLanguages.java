package org.pz.polyglot.models.languages;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import org.pz.polyglot.structs.SemanticVersion;

/**
 * Manages all supported languages and their character sets for different
 * versions.
 */
public final class PZLanguages {
    /**
     * Singleton instance of PZLanguages.
     */
    private static final PZLanguages INSTANCE = new PZLanguages();

    /**
     * Map of language code to PZLanguage instance.
     */
    private final HashMap<String, PZLanguage> languages = new HashMap<>();

    /**
     * Initializes the language map with supported languages and their character
     * sets for versions 41 and 42.
     */
    private PZLanguages() {
        // Add languages for version 41
        addLanguageCharset("AR", new SemanticVersion("41"), Charset.forName("Cp1252"),
                "Espanol (AR) - Argentina Spanish");
        addLanguageCharset("CA", new SemanticVersion("41"), Charset.forName("ISO-8859-15"), "Catalan");
        addLanguageCharset("CH", new SemanticVersion("41"), Charset.forName("UTF-8"), "Traditional Chinese");
        addLanguageCharset("CN", new SemanticVersion("41"), Charset.forName("UTF-8"), "Simplified Chinese");
        addLanguageCharset("CS", new SemanticVersion("41"), Charset.forName("Cp1250"), "Czech");
        addLanguageCharset("DA", new SemanticVersion("41"), Charset.forName("Cp1252"), "Danish");
        addLanguageCharset("DE", new SemanticVersion("41"), Charset.forName("Cp1252"), "Deutsch - German");
        addLanguageCharset("EN", new SemanticVersion("41"), Charset.forName("UTF-8"), "English");
        addLanguageCharset("ES", new SemanticVersion("41"), Charset.forName("Cp1252"), "Espanol (ES) - Spanish");
        addLanguageCharset("FI", new SemanticVersion("41"), Charset.forName("Cp1252"), "Finnish");
        addLanguageCharset("FR", new SemanticVersion("41"), Charset.forName("Cp1252"), "Francais - French");
        addLanguageCharset("HU", new SemanticVersion("41"), Charset.forName("Cp1250"), "Hungarian");
        addLanguageCharset("ID", new SemanticVersion("41"), Charset.forName("UTF-8"), "Indonesia");
        addLanguageCharset("IT", new SemanticVersion("41"), Charset.forName("Cp1252"), "Italiano");
        addLanguageCharset("JP", new SemanticVersion("41"), Charset.forName("UTF-8"), "Japanese");
        addLanguageCharset("KO", new SemanticVersion("41"), Charset.forName("UTF-16"), "Korean");
        addLanguageCharset("NL", new SemanticVersion("41"), Charset.forName("Cp1252"), "Nederlands - Dutch");
        addLanguageCharset("NO", new SemanticVersion("41"), Charset.forName("Cp1252"), "Norsk - Norwegian");
        addLanguageCharset("PH", new SemanticVersion("41"), Charset.forName("UTF-8"), "Tagalog - Filipino");
        addLanguageCharset("PL", new SemanticVersion("41"), Charset.forName("Cp1250"), "Polish");
        addLanguageCharset("PT", new SemanticVersion("41"), Charset.forName("Cp1252"), "Portuguese");
        addLanguageCharset("PTBR", new SemanticVersion("41"), Charset.forName("Cp1252"), "Brazilian Portuguese");
        addLanguageCharset("RO", new SemanticVersion("41"), Charset.forName("UTF-8"), "Romanian");
        addLanguageCharset("RU", new SemanticVersion("41"), Charset.forName("Cp1251"), "Russian");
        addLanguageCharset("TH", new SemanticVersion("41"), Charset.forName("UTF-8"), "Thai");
        addLanguageCharset("TR", new SemanticVersion("41"), Charset.forName("Cp1254"), "Turkish");
        addLanguageCharset("UA", new SemanticVersion("41"), Charset.forName("Cp1251"), "Ukrainian");

        // Add languages for version 42
        addLanguageCharset("AR", new SemanticVersion("42"), Charset.forName("Cp1252"),
                "Espanol (AR) - Argentina Spanish");
        addLanguageCharset("CA", new SemanticVersion("42"), Charset.forName("ISO-8859-15"), "Catalan");
        addLanguageCharset("CH", new SemanticVersion("42"), Charset.forName("UTF-8"), "Traditional Chinese");
        addLanguageCharset("CN", new SemanticVersion("42"), Charset.forName("UTF-8"), "Simplified Chinese");
        addLanguageCharset("CS", new SemanticVersion("42"), Charset.forName("Cp1250"), "Czech");
        addLanguageCharset("DA", new SemanticVersion("42"), Charset.forName("UTF-8"), "Danish");
        addLanguageCharset("DE", new SemanticVersion("42"), Charset.forName("UTF-8"), "Deutsch - German");
        addLanguageCharset("EN", new SemanticVersion("42"), Charset.forName("UTF-8"), "English");
        addLanguageCharset("ES", new SemanticVersion("42"), Charset.forName("UTF-8"), "Espanol (ES) - Spanish");
        addLanguageCharset("FI", new SemanticVersion("42"), Charset.forName("UTF-8"), "Finnish");
        addLanguageCharset("FR", new SemanticVersion("42"), Charset.forName("UTF-8"), "Francais - French");
        addLanguageCharset("HU", new SemanticVersion("42"), Charset.forName("UTF-8"), "Hungarian");
        addLanguageCharset("ID", new SemanticVersion("42"), Charset.forName("UTF-8"), "Indonesia");
        addLanguageCharset("IT", new SemanticVersion("42"), Charset.forName("UTF-8"), "Italiano");
        addLanguageCharset("JP", new SemanticVersion("42"), Charset.forName("UTF-8"), "Japanese");
        addLanguageCharset("KO", new SemanticVersion("42"), Charset.forName("UTF-16"), "Korean");
        addLanguageCharset("NL", new SemanticVersion("42"), Charset.forName("UTF-8"), "Nederlands - Dutch");
        addLanguageCharset("NO", new SemanticVersion("42"), Charset.forName("UTF-8"), "Norsk - Norwegian");
        addLanguageCharset("PH", new SemanticVersion("42"), Charset.forName("UTF-8"), "Tagalog - Filipino");
        addLanguageCharset("PL", new SemanticVersion("42"), Charset.forName("UTF-8"), "Polish");
        addLanguageCharset("PT", new SemanticVersion("42"), Charset.forName("UTF-8"), "Portuguese");
        addLanguageCharset("PTBR", new SemanticVersion("42"), Charset.forName("UTF-8"), "Brazilian Portuguese");
        addLanguageCharset("RO", new SemanticVersion("42"), Charset.forName("UTF-8"), "Romanian");
        addLanguageCharset("RU", new SemanticVersion("42"), Charset.forName("UTF-8"), "Russian");
        addLanguageCharset("TH", new SemanticVersion("42"), Charset.forName("UTF-8"), "Thai");
        addLanguageCharset("TR", new SemanticVersion("42"), Charset.forName("UTF-8"), "Turkish");
        addLanguageCharset("UA", new SemanticVersion("42"), Charset.forName("UTF-8"), "Ukrainian");
    }

    /**
     * Returns the singleton instance of PZLanguages.
     * 
     * @return the singleton instance
     */
    public static PZLanguages getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a language to the map.
     * 
     * @param language the language to add
     */
    public void addLanguage(PZLanguage language) {
        if (language != null) {
            this.languages.put(language.getCode(), language);
        }
    }

    /**
     * Adds a charset for a language and version. If the language exists, updates
     * its charset; otherwise, creates a new language.
     * 
     * @param code    language code
     * @param version semantic version
     * @param charset charset to use
     * @param text    display name
     */
    public void addLanguageCharset(String code, SemanticVersion version, Charset charset, String text) {
        PZLanguage existingLanguage = this.languages.get(code);
        if (existingLanguage != null) {
            // Update charset for existing language
            existingLanguage.setCharset(version, charset);
        } else {
            // Create new language with specified charset
            PZLanguage newLanguage = new PZLanguage(code, text);
            newLanguage.setCharset(version, charset);
            this.languages.put(code, newLanguage);
        }
    }

    /**
     * Retrieves a language by its code.
     * 
     * @param code language code
     * @return Optional containing the language if found
     */
    public Optional<PZLanguage> getLanguage(String code) {
        return Optional.ofNullable(this.languages.get(code));
    }

    /**
     * Returns the number of languages managed.
     * 
     * @return number of languages
     */
    public int size() {
        return this.languages.size();
    }

    /**
     * Returns all language codes, sorted with "EN" first, then alphabetically.
     * 
     * @return sorted set of language codes
     */
    public Set<String> getAllLanguageCodes() {
        // Custom sort: "EN" always first, then alphabetical
        return languages.keySet().stream()
                .sorted((a, b) -> {
                    if (a.equals("EN"))
                        return -1;
                    if (b.equals("EN"))
                        return 1;
                    return a.compareTo(b);
                })
                .collect(java.util.LinkedHashSet::new, java.util.Set::add, java.util.Set::addAll);
    }
}
