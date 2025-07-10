package org.pz.polyglot.models.languages;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.pz.polyglot.structs.SemanticVersion;
import org.pz.polyglot.structs.SemanticVersionMap;

public final class PZLanguage {
    private final String code;
    private final String name;
    private SemanticVersionMap<Charset> charsets;

    public PZLanguage(String code, String name) {
        this.code = code;
        this.name = name;

        this.charsets = new SemanticVersionMap<>();
        // Default charset for all languages. Should be revised in future when 43+
        // released
        this.charsets.put(new SemanticVersion("0"), StandardCharsets.UTF_8);
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    // Add charset for specific version
    public void setCharset(SemanticVersion version, Charset charset) {
        this.charsets.put(version, charset);
    }

    public Optional<Charset> getCharset(SemanticVersion fromVersion) {
        return this.charsets.get(fromVersion);
    }

    /**
     * Gets all charsets in descending order from the given version
     * 
     * @param fromVersion the version to start from (inclusive)
     * @return LinkedHashSet of charsets from the starting version down to the
     *         lowest
     */
    public LinkedHashSet<Charset> getCharsetsDownFrom(SemanticVersion fromVersion) {
        return this.charsets.getCharsetsDownFrom(fromVersion);
    }
}
