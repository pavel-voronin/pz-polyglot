package org.pz.polyglot.pz.core;

import org.pz.polyglot.structs.SemanticVersion;

public class PZBuild {
    public static final PZBuild BUILD_41 = new PZBuild(new SemanticVersion("41"));
    public static final PZBuild BUILD_42 = new PZBuild(new SemanticVersion("42"));

    private SemanticVersion version;

    public PZBuild(SemanticVersion version) {
        this.version = version;
    }

    public int getMajor() {
        return version.getMajor();
    }

    public SemanticVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return version.toString();
    }
}
