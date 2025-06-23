package org.pz.polyglot.pz.core;

import java.nio.charset.Charset;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.languages.PZLanguages;

public class PZBuild {
    public static final PZBuild BUILD_41 = new PZBuild(41);
    static {
        BUILD_41.getLanguages()
                .addLanguage(new PZLanguage("AR", "Espanol (AR) - Argentina Spanish", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("CA", "Catalan", Charset.forName("ISO-8859-15")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("CH", "Traditional Chinese", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("CN", "Simplified Chinese", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("CS", "Czech", Charset.forName("Cp1250")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("DA", "Danish", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("DE", "Deutsch - German", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("EN", "English", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("ES", "Espanol (ES) - Spanish", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("FI", "Finnish", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("FR", "Francais - French", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("HU", "Hungarian", Charset.forName("Cp1250")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("ID", "Indonesia", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("IT", "Italiano", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("JP", "Japanese", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("KO", "Korean", Charset.forName("UTF-16")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("NL", "Nederlands - Dutch", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("NO", "Norsk - Norwegian", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("PH", "Tagalog - Filipino", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("PL", "Polish", Charset.forName("Cp1250")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("PT", "Portuguese", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("PTBR", "Brazilian Portuguese", Charset.forName("Cp1252")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("RO", "Romanian", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("RU", "Russian", Charset.forName("Cp1251")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("TH", "Thai", Charset.forName("UTF-8")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("TR", "Turkish", Charset.forName("Cp1254")));
        BUILD_41.getLanguages().addLanguage(new PZLanguage("UA", "Ukrainian", Charset.forName("Cp1251")));
    }
    public static final PZBuild BUILD_42 = new PZBuild(42);

    private int major;
    private PZLanguages languages = new PZLanguages();

    public PZBuild(int major) {
        this.major = major;
    }

    public int getMajor() {
        return major;
    }

    public PZLanguages getLanguages() {
        return languages;
    }
}
