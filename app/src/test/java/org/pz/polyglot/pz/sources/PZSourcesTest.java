package org.pz.polyglot.pz.sources;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.pz.polyglot.pz.core.PZBuild;

class PZSourcesTest {

    private PZSources pzSources;

    @BeforeEach
    void setUp() {
        pzSources = PZSources.getInstance();
    }

    // BUILD_41 tests - should be BUILD_41 when no common/ and no version 42

    @Test
    void detectsBuild41ForModNames() {
        // Real mod names - BUILD_41
        String[] modNames = {
            "BetterInventory", 
            "zombiesheardistance", 
            "some-mod-name",
            "mod_with_underscores"
        };
        
        for (String modName : modNames) {
            Path path = Paths.get("C:/Mods/MyMod/" + modName + "/media/lua/shared/Translate");
            PZBuild result = pzSources.detectBuildType(path);
            assertEquals(PZBuild.BUILD_41, result, 
                "Should detect BUILD_41 for mod folder: " + modName);
        }
    }

    // BUILD_42 tests - should be BUILD_42 when common/ or version 42 only

    @Test
    void detectsBuild42ForCommonStructure() {
        // common/ structure - BUILD_42
        Path path = Paths.get("C:/SomeFolder/MyMod/common/media/lua/shared/Translate");
        PZBuild result = pzSources.detectBuildType(path);
        assertEquals(PZBuild.BUILD_42, result, "Should detect BUILD_42 for common structure");
    }

    @Test
    void detectsBuild42ForNestedCommon() {
        // Nested common/ structure - BUILD_42
        Path path = Paths.get("C:/Games/ProjectZomboid/data/common/media/lua/shared/Translate");
        PZBuild result = pzSources.detectBuildType(path);
        assertEquals(PZBuild.BUILD_42, result, "Should detect BUILD_42 for nested common structure");
    }

    @Test
    void detectsBuild42ForVersion42() {
        // Only version 42 - BUILD_42
        Path path = Paths.get("C:/Mods/MyMod/42/media/lua/shared/Translate");
        PZBuild result = pzSources.detectBuildType(path);
        assertEquals(PZBuild.BUILD_42, result, "Should detect BUILD_42 for version 42");
    }

    @Test
    void detectsBuild42ForSemverVersions42() {
        // Only semver versions 42.x - BUILD_42
        String[] semver42Versions = {
            "42.0", "42.1", "42.10", 
            "42.0.1", "42.2.5", 
            "42.78.123"
        };
        
        for (String version : semver42Versions) {
            Path path = Paths.get("C:/Mods/MyMod/" + version + "/media/lua/shared/Translate");
            PZBuild result = pzSources.detectBuildType(path);
            assertEquals(PZBuild.BUILD_42, result, 
                "Should detect BUILD_42 for 42 semver: " + version);
        }
    }

    // Edge cases

    @Test
    void handlesWindowsPaths() {
        // Windows-style paths with backslashes
        Path windowsPath = Paths.get("C:\\Games\\Mods\\MyMod\\media\\lua\\shared\\Translate");
        PZBuild result = pzSources.detectBuildType(windowsPath);
        assertEquals(PZBuild.BUILD_41, result, "Should handle Windows paths correctly for BUILD_41");

        Path windowsCommonPath = Paths.get("C:\\Games\\Mods\\MyMod\\common\\media\\lua\\shared\\Translate");
        result = pzSources.detectBuildType(windowsCommonPath);
        assertEquals(PZBuild.BUILD_42, result, "Should handle Windows paths correctly for BUILD_42");
    }

    @Test
    void handlesUnixPaths() {
        // Unix-style paths
        Path unixPath = Paths.get("/home/user/mods/MyMod/media/lua/shared/Translate");
        PZBuild result = pzSources.detectBuildType(unixPath);
        assertEquals(PZBuild.BUILD_41, result, "Should handle Unix paths correctly for BUILD_41");

        Path unixCommonPath = Paths.get("/home/user/mods/MyMod/common/media/lua/shared/Translate");
        result = pzSources.detectBuildType(unixCommonPath);
        assertEquals(PZBuild.BUILD_42, result, "Should handle Unix paths correctly for BUILD_42");
    }

    @Test
    void detectsBuild41WhenCommonInModName() {
        // When "common" appears in mod name but not as folder structure
        Path path = Paths.get("C:/Games/common-mod-name/media/lua/shared/Translate");
        PZBuild result = pzSources.detectBuildType(path);
        assertEquals(PZBuild.BUILD_41, result, "Should detect BUILD_41 when 'common' is in mod name");
    }
}
