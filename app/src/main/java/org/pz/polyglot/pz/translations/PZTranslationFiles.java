package org.pz.polyglot.pz.translations;

import java.util.ArrayList;

public class PZTranslationFiles {
    private static PZTranslationFiles instance;
    private ArrayList<PZTranslationFile> files = new ArrayList<>();

    public static PZTranslationFiles getInstance() {
        if (instance == null) {
            instance = new PZTranslationFiles();
        }

        return instance;
    }

    public ArrayList<PZTranslationFile> getFiles() {
        return this.files;
    }
}
