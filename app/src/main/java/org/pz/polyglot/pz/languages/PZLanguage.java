package org.pz.polyglot.pz.languages;

import java.nio.charset.Charset;
import java.util.LinkedHashSet;

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
        this.charsets.put(new SemanticVersion("41"), java.nio.charset.StandardCharsets.UTF_8);
        this.charsets.put(new SemanticVersion("42"), java.nio.charset.StandardCharsets.UTF_8);
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
