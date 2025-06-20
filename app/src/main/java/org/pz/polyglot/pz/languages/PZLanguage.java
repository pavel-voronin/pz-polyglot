package org.pz.polyglot.pz.languages;

public final class PZLanguage {
    private final String name;
    private final String text;
    private final String charset;

    public PZLanguage(String name, String text, String charset) {
        this.name = name;
        this.text = text;
        this.charset = charset;
    }

    public String getName() {
        return this.name;
    }

    public String getText() {
        return this.text;
    }

    public String getCharset() {
        return this.charset;
    }
}
