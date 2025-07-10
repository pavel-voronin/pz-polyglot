package org.pz.polyglot.models.sources;

import java.nio.file.Path;

import org.pz.polyglot.structs.SemanticVersion;

public class PZSource {
    private final String name;
    private final SemanticVersion version;
    private final Path path;
    private final boolean editable;

    public PZSource(String name, SemanticVersion version, Path path, boolean editable) {
        this.name = name;
        this.version = version;
        this.path = path;
        this.editable = editable;
    }

    public String getName() {
        return name;
    }

    public SemanticVersion getVersion() {
        return version;
    }

    public Path getPath() {
        return path;
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public String toString() {
        return name + " [" + version.getMajor() + "]";
    }
}
