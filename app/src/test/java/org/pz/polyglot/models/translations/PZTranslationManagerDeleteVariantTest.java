package org.pz.polyglot.models.translations;

import org.junit.jupiter.api.*;
import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.sources.PZSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PZTranslationManagerDeleteVariantTest {
    private Path tempDir;
    private PZSource source;
    private PZLanguage language;
    private PZTranslationType type;
    private PZTranslationEntry key;
    private PZTranslationVariant variant;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("pz-polyglot-test");
        source = new org.pz.polyglot.models.sources.PZSource(
                "test",
                new org.pz.polyglot.structs.SemanticVersion("1.0.0"),
                tempDir,
                true,
                1);
        language = new PZLanguage("EN", "English");
        type = PZTranslationType.IG_UI;
        key = new PZTranslationEntry("TestKey");
        variant = new PZTranslationVariant(key, source, language, type, "TestValue", StandardCharsets.UTF_8,
                StandardCharsets.UTF_8);
        // Prepare file
        Path langDir = tempDir.resolve("EN");
        Files.createDirectories(langDir);
        Path file = langDir.resolve("IG_UI_EN.txt");
        Files.write(file, List.of(
                "IG_UI_EN = {",
                "    TestKey = \"TestValue\",",
                "    OtherKey = \"OtherValue\",",
                "}"), StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void deleteVariant_removesKeyFromFile() throws IOException {
        Path file = tempDir.resolve("EN").resolve("IG_UI_EN.txt");
        assertTrue(Files.exists(file));
        List<String> before = Files.readAllLines(file);
        assertTrue(before.stream().anyMatch(l -> l.contains("TestKey")));
        PZTranslationManager.deleteVariant(variant);
        List<String> after = Files.readAllLines(file);
        assertFalse(after.stream().anyMatch(l -> l.contains("TestKey")));
        assertTrue(after.stream().anyMatch(l -> l.contains("OtherKey")));
    }

    @Test
    void deleteVariant_doesNothingIfKeyNotPresent() throws IOException {
        Path file = tempDir.resolve("EN").resolve("IG_UI_EN.txt");
        PZTranslationEntry missingKey = new PZTranslationEntry("MissingKey");
        PZTranslationVariant missingVariant = new PZTranslationVariant(missingKey, source, language, type, "",
                StandardCharsets.UTF_8, StandardCharsets.UTF_8);
        List<String> before = Files.readAllLines(file);
        PZTranslationManager.deleteVariant(missingVariant);
        List<String> after = Files.readAllLines(file);
        assertEquals(before, after);
    }

    @Test
    void deleteVariant_doesNothingIfFileDoesNotExist() {
        PZTranslationEntry newKey = new PZTranslationEntry("NewKey");
        PZTranslationVariant newVariant = new PZTranslationVariant(newKey, source, language, type, "",
                StandardCharsets.UTF_8, StandardCharsets.UTF_8);
        Path file = tempDir.resolve("EN").resolve("IG_UI_EN.txt");
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
        assertFalse(Files.exists(file));
        assertDoesNotThrow(() -> PZTranslationManager.deleteVariant(newVariant));
    }

    @Test
    void deleteVariant_printsErrorOnIOException() {
        java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(errContent));
        try {
            Path file = tempDir.resolve("EN").resolve("IG_UI_EN.txt");
            assertTrue(Files.exists(file));
            file.toFile().setReadOnly();
            try {
                PZTranslationManager.deleteVariant(variant);
            } finally {
                file.toFile().setWritable(true);
            }
            String err = errContent.toString();
            assertTrue(err.contains("Failed to delete variant"),
                    "System.err did not contain expected message. Actual output: " + err);
        } finally {
            System.setErr(originalErr);
        }
    }
}
