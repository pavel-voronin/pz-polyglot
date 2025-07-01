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
        versionMap.put("41", "Value 41");
        versionMap.put("42.9", "Value 42.9");
        versionMap.put("42.3", "Value 42.3");
        versionMap.put("40.1", "Value 40.1");
        versionMap.put("43.0", "Value 43.0");
    }

    @Test
    void testGetExactVersion() {
        assertEquals("Value 42.3", versionMap.get("42.3"));
        assertEquals("Value 41", versionMap.get("41"));
        assertNull(versionMap.get("99.99"));
    }

    @Test
    void testGetPreviousVersion() {
        // Previous to 42.5 should be 42.3
        assertEquals("Value 42.3", versionMap.getPrevious("42.5"));

        // Previous to 42.3 should be 41
        assertEquals("Value 41", versionMap.getPrevious("42.3"));

        // Previous to 41 should be 40.1
        assertEquals("Value 40.1", versionMap.getPrevious("41"));

        // Previous to 40.0 should be null (no lower version)
        assertNull(versionMap.getPrevious("40.0"));

        // Previous to 40.1 should be null (it's the lowest)
        assertNull(versionMap.getPrevious("40.1"));
    }

    @Test
    void testGetFloorVersion() {
        // Floor for 42.5 should be 42.3 (highest <= 42.5)
        assertEquals("Value 42.3", versionMap.getFloor("42.5"));

        // Floor for exact match should return exact value
        assertEquals("Value 42.3", versionMap.getFloor("42.3"));

        // Floor for version higher than all should return highest
        assertEquals("Value 43.0", versionMap.getFloor("44.0"));

        // Floor for version lower than all should return null
        assertNull(versionMap.getFloor("39.0"));
    }

    @Test
    void testGetCeilingVersion() {
        // Ceiling for 42.5 should be 42.9 (lowest >= 42.5)
        assertEquals("Value 42.9", versionMap.getCeiling("42.5"));

        // Ceiling for exact match should return exact value
        assertEquals("Value 42.3", versionMap.getCeiling("42.3"));

        // Ceiling for version lower than all should return lowest
        assertEquals("Value 40.1", versionMap.getCeiling("39.0"));

        // Ceiling for version higher than all should return null
        assertNull(versionMap.getCeiling("44.0"));
    }

    @Test
    void testGetNextVersion() {
        // Next to 42.3 should be 42.9
        assertEquals("Value 42.9", versionMap.getNext("42.3"));

        // Next to 41 should be 42.3
        assertEquals("Value 42.3", versionMap.getNext("41"));

        // Next to highest version should be null
        assertEquals("Value 43.0", versionMap.getNext("42.9"));
        assertNull(versionMap.getNext("43.0"));
    }

    @Test
    void testSemanticVersionSorting() {
        // Test that versions are properly sorted
        SemanticVersion[] versions = versionMap.versionKeySet().toArray(new SemanticVersion[0]);

        assertEquals("40.1", versions[0].toString());
        assertEquals("41", versions[1].toString());
        assertEquals("42.3", versions[2].toString());
        assertEquals("42.9", versions[3].toString());
        assertEquals("43.0", versions[4].toString());
    }

    @Test
    void testContainsKey() {
        assertTrue(versionMap.containsKey("42.3"));
        assertFalse(versionMap.containsKey("99.99"));
    }

    @Test
    void testSize() {
        assertEquals(5, versionMap.size());

        versionMap.put("44.0", "Value 44.0");
        assertEquals(6, versionMap.size());

        versionMap.remove("44.0");
        assertEquals(5, versionMap.size());
    }

    @Test
    void testRemove() {
        assertEquals("Value 42.3", versionMap.remove("42.3"));
        assertNull(versionMap.get("42.3"));
        assertEquals(4, versionMap.size());

        assertNull(versionMap.remove("99.99"));
    }

    @Test
    void testFirstAndLastVersion() {
        assertEquals("40.1", versionMap.firstVersion().toString());
        assertEquals("43.0", versionMap.lastVersion().toString());
    }

    @Test
    void testClear() {
        versionMap.clear();
        assertTrue(versionMap.isEmpty());
        assertEquals(0, versionMap.size());
        assertNull(versionMap.firstVersion());
        assertNull(versionMap.lastVersion());
    }
}
