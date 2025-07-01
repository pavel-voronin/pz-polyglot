package org.pz.polyglot.structs;

import java.util.*;

/**
 * A data structure that acts like a HashMap but also maintains semantic version
 * ordering.
 * Allows access by key and navigation to previous versions in semantic order.
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
     * Adds a value for the given version string
     * 
     * @param versionString the version string (e.g., "41", "42.9", "42.3")
     * @param value         the value to store
     * @return the previous value associated with this version, or null if none
     */
    public V put(String versionString, V value) {
        SemanticVersion version = new SemanticVersion(versionString);
        stringToVersion.put(versionString, version);
        return versionMap.put(version, value);
    }

    /**
     * Gets the value for the exact version string
     * 
     * @param versionString the version string to look up
     * @return the value, or null if not found
     */
    public V get(String versionString) {
        SemanticVersion version = stringToVersion.get(versionString);
        if (version == null)
            return null;
        return versionMap.get(version);
    }

    /**
     * Gets the value for the exact semantic version
     * 
     * @param version the semantic version to look up
     * @return the value, or null if not found
     */
    public V get(SemanticVersion version) {
        return versionMap.get(version);
    }

    /**
     * Gets the value for the previous available version relative to the given
     * version string
     * 
     * @param versionString the version string to find the previous version for
     * @return the value of the previous version, or null if no previous version
     *         exists
     */
    public V getPrevious(String versionString) {
        SemanticVersion targetVersion = new SemanticVersion(versionString);
        return getPrevious(targetVersion);
    }

    /**
     * Gets the value for the previous available version relative to the given
     * semantic version
     * 
     * @param targetVersion the version to find the previous version for
     * @return the value of the previous version, or null if no previous version
     *         exists
     */
    public V getPrevious(SemanticVersion targetVersion) {
        SemanticVersion lowerVersion = versionMap.lowerKey(targetVersion);
        return lowerVersion != null ? versionMap.get(lowerVersion) : null;
    }

    /**
     * Gets the entry (version and value) for the previous available version
     * 
     * @param versionString the version string to find the previous version for
     * @return the previous entry, or null if no previous version exists
     */
    public Map.Entry<SemanticVersion, V> getPreviousEntry(String versionString) {
        SemanticVersion targetVersion = new SemanticVersion(versionString);
        return getPreviousEntry(targetVersion);
    }

    /**
     * Gets the entry (version and value) for the previous available version
     * 
     * @param targetVersion the version to find the previous version for
     * @return the previous entry, or null if no previous version exists
     */
    public Map.Entry<SemanticVersion, V> getPreviousEntry(SemanticVersion targetVersion) {
        return versionMap.lowerEntry(targetVersion);
    }

    /**
     * Gets the value for the next available version relative to the given version
     * string
     * 
     * @param versionString the version string to find the next version for
     * @return the value of the next version, or null if no next version exists
     */
    public V getNext(String versionString) {
        SemanticVersion targetVersion = new SemanticVersion(versionString);
        return getNext(targetVersion);
    }

    /**
     * Gets the value for the next available version relative to the given semantic
     * version
     * 
     * @param targetVersion the version to find the next version for
     * @return the value of the next version, or null if no next version exists
     */
    public V getNext(SemanticVersion targetVersion) {
        SemanticVersion higherVersion = versionMap.higherKey(targetVersion);
        return higherVersion != null ? versionMap.get(higherVersion) : null;
    }

    /**
     * Gets the highest available version that is less than or equal to the given
     * version
     * 
     * @param versionString the version string to find the floor for
     * @return the value of the floor version, or null if no such version exists
     */
    public V getFloor(String versionString) {
        SemanticVersion targetVersion = new SemanticVersion(versionString);
        SemanticVersion floorVersion = versionMap.floorKey(targetVersion);
        return floorVersion != null ? versionMap.get(floorVersion) : null;
    }

    /**
     * Gets the lowest available version that is greater than or equal to the given
     * version
     * 
     * @param versionString the version string to find the ceiling for
     * @return the value of the ceiling version, or null if no such version exists
     */
    public V getCeiling(String versionString) {
        SemanticVersion targetVersion = new SemanticVersion(versionString);
        SemanticVersion ceilingVersion = versionMap.ceilingKey(targetVersion);
        return ceilingVersion != null ? versionMap.get(ceilingVersion) : null;
    }

    /**
     * Removes the mapping for the given version string
     * 
     * @param versionString the version string to remove
     * @return the previous value, or null if none
     */
    public V remove(String versionString) {
        SemanticVersion version = stringToVersion.remove(versionString);
        return version != null ? versionMap.remove(version) : null;
    }

    /**
     * Checks if the map contains the given version string
     * 
     * @param versionString the version string to check
     * @return true if the version exists in the map
     */
    public boolean containsKey(String versionString) {
        return stringToVersion.containsKey(versionString);
    }

    /**
     * Checks if the map contains the given semantic version
     * 
     * @param version the semantic version to check
     * @return true if the version exists in the map
     */
    public boolean containsKey(SemanticVersion version) {
        return versionMap.containsKey(version);
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
     * Returns a set of all version strings in the map
     * 
     * @return set of version strings
     */
    public Set<String> versionStringKeySet() {
        return new HashSet<>(stringToVersion.keySet());
    }

    /**
     * Returns a navigable set of all semantic versions in sorted order
     * 
     * @return navigable set of semantic versions
     */
    public NavigableSet<SemanticVersion> versionKeySet() {
        return versionMap.navigableKeySet();
    }

    /**
     * Returns a collection of all values in the map
     * 
     * @return collection of values
     */
    public Collection<V> values() {
        return versionMap.values();
    }

    /**
     * Returns a set of all entries in version-sorted order
     * 
     * @return set of entries
     */
    public Set<Map.Entry<SemanticVersion, V>> entrySet() {
        return versionMap.entrySet();
    }

    /**
     * Returns the first (lowest) version in the map
     * 
     * @return the first semantic version, or null if map is empty
     */
    public SemanticVersion firstVersion() {
        return versionMap.isEmpty() ? null : versionMap.firstKey();
    }

    /**
     * Returns the last (highest) version in the map
     * 
     * @return the last semantic version, or null if map is empty
     */
    public SemanticVersion lastVersion() {
        return versionMap.isEmpty() ? null : versionMap.lastKey();
    }

    @Override
    public String toString() {
        return versionMap.toString();
    }
}
