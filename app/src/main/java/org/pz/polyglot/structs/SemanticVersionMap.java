package org.pz.polyglot.structs;

import java.util.*;

/**
 * A data structure that acts like a HashMap but also maintains semantic version
 * ordering.
 * Allows access by key and navigation to previous/next versions in semantic
 * order.
 * 
 * @param <V> the type of values stored
 */
public class SemanticVersionMap<V> {
    private final TreeMap<SemanticVersion, V> versionMap;
    private final Map<String, SemanticVersion> stringToVersion;

    public SemanticVersionMap() {
        this.versionMap = new TreeMap<>();
        this.stringToVersion = new HashMap<>();
    }

    /**
     * Adds a value for the given version
     * 
     * @param version the version to associate with the value
     * @param value   the value to store
     * @return the previous value associated with this version, or null if none
     */
    public V put(SemanticVersion version, V value) {
        stringToVersion.put(version.toString(), version);
        return versionMap.put(version, value);
    }

    /**
     * Gets the value for the exact version
     * 
     * @param version the version to look up
     * @return Optional containing the value, or empty if not found
     */
    public Optional<V> get(SemanticVersion version) {
        return Optional.ofNullable(versionMap.get(version));
    }

    /**
     * Removes the mapping for the given version
     * 
     * @param version the version to remove
     * @return Optional containing the previous value, or empty if none
     */
    public Optional<V> remove(SemanticVersion version) {
        SemanticVersion removedVersion = stringToVersion.remove(version.toString());
        return removedVersion != null ? Optional.ofNullable(versionMap.remove(removedVersion)) : Optional.empty();
    }

    /**
     * Checks if the map contains the given version
     * 
     * @param version the version to check
     * @return true if the version exists in the map
     */
    public boolean containsKey(SemanticVersion version) {
        return stringToVersion.containsKey(version.toString());
    }

    /**
     * Returns the number of versions in the map
     * 
     * @return the size of the map
     */
    public int size() {
        return versionMap.size();
    }

    /**
     * Checks if the map is empty
     * 
     * @return true if the map is empty
     */
    public boolean isEmpty() {
        return versionMap.isEmpty();
    }

    /**
     * Clears all mappings from the map
     */
    public void clear() {
        versionMap.clear();
        stringToVersion.clear();
    }

    /**
     * Gets all values from a starting version in descending order
     * 
     * @param fromVersion the version to start from (inclusive)
     * @return LinkedHashSet of values from the starting version down to the lowest
     */
    public LinkedHashSet<V> getCharsetsDownFrom(SemanticVersion fromVersion) {
        LinkedHashSet<V> result = new LinkedHashSet<>();
        for (SemanticVersion version : versionMap.headMap(fromVersion, true).descendingKeySet()) {
            V value = versionMap.get(version);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return versionMap.toString();
    }
}
