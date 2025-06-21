package org.pz.polyglot.pz.sources;

import java.io.File;

public class PZSource {
    private String name;
    private String version;
    private File folder;

    public PZSource(String name, String version, File folder) {
        this.name = name;
        this.version = version;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public File getFolder() {
        return folder;
    }

    @Override
    public String toString() {
        return name + " [" + version + "]";
    }
}
