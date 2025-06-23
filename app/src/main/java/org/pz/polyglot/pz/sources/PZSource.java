package org.pz.polyglot.pz.sources;

import java.nio.file.Path;

import org.pz.polyglot.pz.core.PZBuild;

public class PZSource {
    private final String name;
    private final PZBuild build;
    private final Path path;
    private final boolean editable;

    public PZSource(String name, PZBuild build, Path path, boolean editable) {
        this.name = name;
        this.build = build;
        this.path = path;
        this.editable = editable;
    }

    public String getName() {
        return name;
    }

    public PZBuild getBuild() {
        return build;
    }

    public Path getPath() {
        return path;
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public String toString() {
        return name + " [" + build.getMajor() + "]";
    }
}
