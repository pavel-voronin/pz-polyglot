package org.pz.polyglot.pz.languages;

public final class PZLanguage {
    private final String code;
    private final String text;
    private final String charset;

    public PZLanguage(String code, String text, String charset) {
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

    public String getCharset() {
        return this.charset;
    }
}
