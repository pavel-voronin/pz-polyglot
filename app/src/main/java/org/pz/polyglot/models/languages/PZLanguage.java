package org.pz.polyglot.models.languages;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.pz.polyglot.structs.SemanticVersion;
import org.pz.polyglot.structs.SemanticVersionMap;

public final class PZLanguage {
    /**
     * ISO 639-1 or custom language code.
     */
    private final String code;

    /**
     * Human-readable language name.
     */
    private final String name;

    /**
     * Map of charsets by semantic version.
     */
    private SemanticVersionMap<Charset> charsets;

    /**
     * Constructs a language with the given code and name.
     * Initializes charset map with UTF-8 for version 0.
     *
     * @param code ISO 639-1 or custom language code
     * @param name Human-readable language name
     */
    public PZLanguage(String code, String name) {
        this.code = code;
        this.name = name;
        this.charsets = new SemanticVersionMap<>();
        // Default charset for all languages. Should be revised when version 43+ is
        // released
        this.charsets.put(new SemanticVersion("0"), StandardCharsets.UTF_8);
    }

    /**
     * Gets the language code.
     *
     * @return language code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Gets the language name.
     *
     * @return language name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Associates a charset with a specific semantic version.
     *
     * @param version semantic version
     * @param charset charset to associate
     */
    public void setCharset(SemanticVersion version, Charset charset) {
        this.charsets.put(version, charset);
    }

    /**
     * Gets the charset for the specified semantic version, if present.
     *
     * @param fromVersion semantic version
     * @return optional charset for the version
     */
    public Optional<Charset> getCharset(SemanticVersion fromVersion) {
        return this.charsets.get(fromVersion);
    }

    /**
     * Gets all charsets in descending order from the given version (inclusive).
     *
     * @param fromVersion the version to start from (inclusive)
     * @return LinkedHashSet of charsets from the starting version down to the
     *         lowest
     */
    public LinkedHashSet<Charset> getCharsetsDownFrom(SemanticVersion fromVersion) {
        return this.charsets.getCharsetsDownFrom(fromVersion);
    }
}
