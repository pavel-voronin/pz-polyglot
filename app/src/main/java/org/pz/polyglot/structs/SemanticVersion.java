package org.pz.polyglot.structs;

import java.util.Objects;

/**
 * Represents a semantic version (major.minor.patch) for comparison and sorting.
 * Immutable and safely parses version strings.
 */
public class SemanticVersion implements Comparable<SemanticVersion> {

    /**
     * The original version string as provided.
     */
    private final String original;

    /**
     * Major version number.
     */
    private final int major;

    /**
     * Minor version number.
     */
    private final int minor;

    /**
     * Patch version number.
     */
    private final int patch;

    /**
     * Constructs a SemanticVersion from a string in the format "major.minor.patch".
     * Missing components default to zero. Non-numeric components are treated as
     * zero.
     *
     * @param version the version string to parse
     */
    public SemanticVersion(String version) {
        this.original = version;
        String[] parts = version.split("\\.");

        // Parse each part, defaulting to zero if missing or invalid
        this.major = parts.length > 0 ? parseIntSafe(parts[0]) : 0;
        this.minor = parts.length > 1 ? parseIntSafe(parts[1]) : 0;
        this.patch = parts.length > 2 ? parseIntSafe(parts[2]) : 0;
    }

    /**
     * Safely parses an integer from a string, returning zero if parsing fails.
     *
     * @param str the string to parse
     * @return the parsed integer, or zero if invalid
     */
    private int parseIntSafe(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Compares this version to another by major, minor, then patch.
     *
     * @param other the other SemanticVersion to compare to
     * @return negative if less, positive if greater, zero if equal
     */
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

    /**
     * Checks equality based on major, minor, and patch values.
     *
     * @param obj the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        SemanticVersion that = (SemanticVersion) obj;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    /**
     * Computes hash code based on major, minor, and patch values.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    /**
     * Returns the original version string.
     *
     * @return the original version string
     */
    @Override
    public String toString() {
        return original;
    }

    /**
     * Gets the major version number.
     *
     * @return the major version
     */
    public int getMajor() {
        return major;
    }

    /**
     * Gets the minor version number.
     *
     * @return the minor version
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Gets the patch version number.
     *
     * @return the patch version
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Gets the original version string.
     *
     * @return the original version string
     */
    public String getOriginal() {
        return original;
    }
}
