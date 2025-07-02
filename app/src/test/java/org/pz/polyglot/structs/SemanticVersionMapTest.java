package org.pz.polyglot.structs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemanticVersionMapTest {

    private SemanticVersionMap<String> versionMap;

    @BeforeEach
    void setUp() {
        versionMap = new SemanticVersionMap<>();
        // Add test data as described in the example
        versionMap.put(new SemanticVersion("41"), "Value 41");
        versionMap.put(new SemanticVersion("42.9"), "Value 42.9");
        versionMap.put(new SemanticVersion("42.3"), "Value 42.3");
        versionMap.put(new SemanticVersion("40.1"), "Value 40.1");
        versionMap.put(new SemanticVersion("43.0"), "Value 43.0");
    }

    @Test
    void testGetExactVersion() {
        assertEquals("Value 42.3", versionMap.get(new SemanticVersion("42.3")).orElse(null));
        assertEquals("Value 41", versionMap.get(new SemanticVersion("41")).orElse(null));
        assertTrue(versionMap.get(new SemanticVersion("99.99")).isEmpty());
    }

    @Test
    void testContainsKey() {
        assertTrue(versionMap.containsKey(new SemanticVersion("42.3")));
        assertFalse(versionMap.containsKey(new SemanticVersion("99.99")));
    }

    @Test
    void testSize() {
        assertEquals(5, versionMap.size());

        versionMap.put(new SemanticVersion("44.0"), "Value 44.0");
        assertEquals(6, versionMap.size());

        versionMap.remove(new SemanticVersion("44.0"));
        assertEquals(5, versionMap.size());
    }

    @Test
    void testRemove() {
        assertEquals("Value 42.3", versionMap.remove(new SemanticVersion("42.3")).orElse(null));
        assertTrue(versionMap.get(new SemanticVersion("42.3")).isEmpty());
        assertEquals(4, versionMap.size());

        assertTrue(versionMap.remove(new SemanticVersion("99.99")).isEmpty());
    }

    @Test
    void testClear() {
        versionMap.clear();
        assertTrue(versionMap.isEmpty());
        assertEquals(0, versionMap.size());
    }

    @Test
    void testGetCharsetsDownFrom() {
        // Test getting values from version that exists
        var fromVersion42_9 = versionMap.getCharsetsDownFrom(new SemanticVersion("42.9"));
        assertEquals(4, fromVersion42_9.size());
        var iterator = fromVersion42_9.iterator();
        assertEquals("Value 42.9", iterator.next());
        assertEquals("Value 42.3", iterator.next());
        assertEquals("Value 41", iterator.next());
        assertEquals("Value 40.1", iterator.next());

        // Test getting values from version that doesn't exist but is in the middle
        var fromVersion42_5 = versionMap.getCharsetsDownFrom(new SemanticVersion("42.5"));
        assertEquals(3, fromVersion42_5.size());
        iterator = fromVersion42_5.iterator();
        assertEquals("Value 42.3", iterator.next());
        assertEquals("Value 41", iterator.next());
        assertEquals("Value 40.1", iterator.next());

        // Test getting values from highest version
        var fromVersion43_0 = versionMap.getCharsetsDownFrom(new SemanticVersion("43.0"));
        assertEquals(5, fromVersion43_0.size());
        iterator = fromVersion43_0.iterator();
        assertEquals("Value 43.0", iterator.next());
        assertEquals("Value 42.9", iterator.next());
        assertEquals("Value 42.3", iterator.next());
        assertEquals("Value 41", iterator.next());
        assertEquals("Value 40.1", iterator.next());

        // Test getting values from version higher than any existing
        var fromVersion50_0 = versionMap.getCharsetsDownFrom(new SemanticVersion("50.0"));
        assertEquals(5, fromVersion50_0.size());

        // Test getting values from version lower than any existing
        var fromVersion30_0 = versionMap.getCharsetsDownFrom(new SemanticVersion("30.0"));
        assertEquals(0, fromVersion30_0.size());

        // Test uniqueness - add duplicate value and ensure it's not duplicated in
        // result
        versionMap.put(new SemanticVersion("42.8"), "Value 42.3"); // same value as 42.3
        var fromVersionWithDuplicates = versionMap.getCharsetsDownFrom(new SemanticVersion("43.0"));
        assertEquals(5, fromVersionWithDuplicates.size()); // should still be unique values
        var valuesList = fromVersionWithDuplicates.stream().toList();
        assertEquals("Value 43.0", valuesList.get(0));
        assertEquals("Value 42.9", valuesList.get(1));
        assertEquals("Value 42.3", valuesList.get(2)); // first occurrence wins
        assertEquals("Value 41", valuesList.get(3));
        assertEquals("Value 40.1", valuesList.get(4));
    }
}
