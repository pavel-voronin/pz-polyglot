package org.pz.polyglot.pz.languages;

import java.util.HashMap;
import java.util.Optional;

public final class PZLanguages {
    private final HashMap<String, PZLanguage> languages = new HashMap<>();

    public void addLanguage(PZLanguage language) {
        if (language != null) {
            this.languages.put(language.getCode(), language);
        }
    }

    public Optional<PZLanguage> getLanguage(String code) {
        return Optional.ofNullable(this.languages.get(code));
    }

    public int size() {
        return this.languages.size();
    }

    public java.util.Set<String> getAllLanguageCodes() {
        return new java.util.HashSet<>(languages.keySet());
    }
}
