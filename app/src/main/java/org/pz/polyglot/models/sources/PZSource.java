package org.pz.polyglot.models.sources;

import java.nio.file.Path;

import org.pz.polyglot.structs.SemanticVersion;

/**
 * Represents a source of translations or data in Polyglot.
 * Contains metadata such as name, version, file path, editability, and
 * priority.
 */
public class PZSource {
    /**
     * The name of the source (e.g., mod or base game).
     */
    private final String name;

    /**
     * The semantic version of the source.
     */
    private final SemanticVersion version;

    /**
     * The file system path to the source.
     */
    private final Path path;

    /**
     * Indicates whether the source is editable by the user.
     */
    private final boolean editable;

    /**
     * Priority of the source; higher values mean higher precedence in merging.
     */
    private final int priority;

    /**
     * Constructs a new PZSource instance.
     *
     * @param name     the name of the source
     * @param version  the semantic version
     * @param path     the file system path
     * @param editable whether the source is editable
     * @param priority the priority for merging
     */
    public PZSource(String name, SemanticVersion version, Path path, boolean editable, int priority) {
        this.name = name;
        this.version = version;
        this.path = path;
        this.editable = editable;
        this.priority = priority;
    }

    /**
     * Gets the name of the source.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the semantic version of the source.
     * 
     * @return the version
     */
    public SemanticVersion getVersion() {
        return version;
    }

    /**
     * Gets the file system path to the source.
     * 
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Checks if the source is editable by the user.
     * 
     * @return true if editable, false otherwise
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Gets the priority of the source for merging purposes.
     * 
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns a string representation of the source, including its name and major
     * version.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return name + " [" + version.getMajor() + "]";
    }
}
