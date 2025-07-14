package org.pz.polyglot.structs;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A map-like data structure that maintains semantic version ordering.
 * Provides access by key and navigation to previous/next versions in semantic
 * order.
 *
 * @param <V> the type of values stored
 */

public class SemanticVersionMap<V> {
    /**
     * Stores values mapped to their semantic versions, maintaining natural order.
     */
    private final TreeMap<SemanticVersion, V> versionMap;

    /**
     * Maps version string representations to their SemanticVersion objects for
     * quick lookup.
     */
    private final Map<String, SemanticVersion> stringToVersion;

    /**
     * Constructs an empty SemanticVersionMap.
     */
    public SemanticVersionMap() {
        this.versionMap = new TreeMap<>();
        this.stringToVersion = new HashMap<>();
    }

    /**
     * Associates the specified value with the given semantic version.
     *
     * @param version the semantic version to associate with the value
     * @param value   the value to store
     * @return the previous value associated with this version, or null if none
     */
    public V put(SemanticVersion version, V value) {
        stringToVersion.put(version.toString(), version);
        return versionMap.put(version, value);
    }

    /**
     * Retrieves the value for the specified semantic version.
     *
     * @param version the semantic version to look up
     * @return an Optional containing the value, or empty if not found
     */
    public Optional<V> get(SemanticVersion version) {
        return Optional.ofNullable(versionMap.get(version));
    }

    /**
     * Removes the mapping for the specified semantic version.
     *
     * @param version the semantic version to remove
     * @return an Optional containing the previous value, or empty if none
     */
    public Optional<V> remove(SemanticVersion version) {
        SemanticVersion removedVersion = stringToVersion.remove(version.toString());
        return removedVersion != null ? Optional.ofNullable(versionMap.remove(removedVersion)) : Optional.empty();
    }

    /**
     * Checks if the map contains the specified semantic version.
     *
     * @param version the semantic version to check
     * @return true if the version exists in the map, false otherwise
     */
    public boolean containsKey(SemanticVersion version) {
        return stringToVersion.containsKey(version.toString());
    }

    /**
     * Returns the number of semantic versions in the map.
     *
     * @return the size of the map
     */
    public int size() {
        return versionMap.size();
    }

    /**
     * Checks if the map contains no semantic versions.
     *
     * @return true if the map is empty, false otherwise
     */
    public boolean isEmpty() {
        return versionMap.isEmpty();
    }

    /**
     * Removes all mappings from the map.
     */
    public void clear() {
        versionMap.clear();
        stringToVersion.clear();
    }

    /**
     * Returns all values from the specified starting version down to the lowest
     * version, in descending order.
     *
     * @param fromVersion the semantic version to start from (inclusive)
     * @return a LinkedHashSet of values from the starting version down to the
     *         lowest
     */
    public LinkedHashSet<V> getCharsetsDownFrom(SemanticVersion fromVersion) {
        LinkedHashSet<V> result = new LinkedHashSet<>();
        // Iterate over versions in descending order from the given version
        for (SemanticVersion version : versionMap.headMap(fromVersion, true).descendingKeySet()) {
            V value = versionMap.get(version);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Returns a string representation of the map.
     *
     * @return a string representation of the version map
     */
    @Override
    public String toString() {
        return versionMap.toString();
    }
}
