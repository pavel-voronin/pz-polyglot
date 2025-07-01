package org.pz.polyglot.structs;

import java.util.Objects;

/**
 * Represents a semantic version for comparison and sorting
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
    private final String original;
    private final int major;
    private final int minor;
    private final int patch;

    public SemanticVersion(String version) {
        this.original = version;
        String[] parts = version.split("\\.");

        this.major = parts.length > 0 ? parseIntSafe(parts[0]) : 0;
        this.minor = parts.length > 1 ? parseIntSafe(parts[1]) : 0;
        this.patch = parts.length > 2 ? parseIntSafe(parts[2]) : 0;
    }

    private int parseIntSafe(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int compareTo(SemanticVersion other) {
        int majorCompare = Integer.compare(this.major, other.major);
        if (majorCompare != 0)
            return majorCompare;

        int minorCompare = Integer.compare(this.minor, other.minor);
        if (minorCompare != 0)
            return minorCompare;

        return Integer.compare(this.patch, other.patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        SemanticVersion that = (SemanticVersion) obj;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return original;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getOriginal() {
        return original;
    }
}
