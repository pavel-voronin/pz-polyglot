package org.pz.polyglot.models.translations;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.sources.PZSource;

/**
 * Represents a translation entry containing multiple translation variants for a
 * specific key.
 */
public class PZTranslationEntry {
    /**
     * The unique key identifying this translation entry.
     */
    private final String key;

    /**
     * The list of translation variants associated with this entry.
     */
    private final ArrayList<PZTranslationVariant> variants = new ArrayList<>();

    /**
     * Constructs a translation entry for the specified key.
     *
     * @param key the unique key for this translation entry
     */
    public PZTranslationEntry(String key) {
        this.key = key;
    }

    /**
     * Returns the key for this translation entry.
     *
     * @return the translation key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns all translation variants for this entry.
     *
     * @return the list of translation variants
     */
    public ArrayList<PZTranslationVariant> getVariants() {
        return variants;
    }

    /**
     * Returns the list of translation variants that have been changed.
     *
     * @return the list of changed translation variants
     */
    public ArrayList<PZTranslationVariant> getChangedVariants() {
        // Filter variants to only those marked as changed
        return variants.stream().filter(PZTranslationVariant::isChanged)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Adds a new translation variant to this entry.
     *
     * @param source   the source of the translation
     * @param language the language of the translation
     * @param type     the type of translation
     * @param text     the translation text
     * @param charset  the charset used for the translation text
     * @return the newly created translation variant
     */
    public PZTranslationVariant addVariant(PZSource source, PZLanguage language, PZTranslationType type, String text,
            Charset charset) {
        // Determine the supposed charset for the language and source version
        Charset supposedCharset = language.getCharset(source.getVersion()).orElse(null);
        PZTranslationVariant variant = new PZTranslationVariant(this, source, language, type, text, supposedCharset,
                charset);
        variants.add(variant);
        return variant;
    }

    /**
     * Returns the type of this entry, determined from the first variant, or
     * {@code null} if no variants exist.
     *
     * @return the translation type, or {@code null} if no variants exist
     */
    public PZTranslationType getType() {
        return variants.isEmpty() ? null : variants.get(0).getType();
    }
}
