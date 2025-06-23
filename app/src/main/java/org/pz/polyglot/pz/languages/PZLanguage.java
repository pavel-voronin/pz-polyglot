package org.pz.polyglot.pz.languages;

import java.nio.charset.Charset;
import java.util.Optional;

public final class PZLanguage {
    private final String code;
    private final String text;
    private final Charset charset;
    private Charset fallbackCharset;

    public PZLanguage(String code, String text, Charset charset) {
        this.code = code;
        this.text = text;
        this.charset = charset;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public Optional<Charset> getFallbackCharset() {
        return Optional.ofNullable(this.fallbackCharset);
    }

    public void setFallbackCharset(Charset fallbackCharset) {
        this.fallbackCharset = fallbackCharset;
    }
}
