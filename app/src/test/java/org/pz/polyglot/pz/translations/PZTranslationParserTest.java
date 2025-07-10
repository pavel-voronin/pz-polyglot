package org.pz.polyglot.pz.translations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.sources.PZSource;
import org.pz.polyglot.structs.SemanticVersion;

class PZTranslationParserTest {
    @Test
    void parsesSimpleKeyValue() throws Exception {
        // Prepare a temporary file with a simple key-value pair
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of("key1 = \"value1\""));

        // Prepare minimal language and file objects
        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        // Parse
        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext(), "Should have at least one key-value");
            var pair = it.next();
            assertEquals("key1", pair.key());
            assertEquals("value1", pair.value());
            assertFalse(it.hasNext(), "Should have only one key-value");
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void skipsCommentsAndEmptyLines() throws Exception {
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of(
                "-- This is a comment",
                "",
                "key2 = \"value2\"",
                "   ",
                "-- Another comment"));

        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext());
            var pair = it.next();
            assertEquals("key2", pair.key());
            assertEquals("value2", pair.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void parsesMultipleKeyValuePairs() throws Exception {
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of(
                "keyA = \"A value\"",
                "keyB = \"B value\""));

        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext());
            var pair1 = it.next();
            assertEquals("keyA", pair1.key());
            assertEquals("A value", pair1.value());
            assertTrue(it.hasNext());
            var pair2 = it.next();
            assertEquals("keyB", pair2.key());
            assertEquals("B value", pair2.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void ignoresMalformedLines() throws Exception {
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of(
                "notAKeyValue",
                "keyC = valueWithoutQuotes",
                "keyD = \"validValue\""));

        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext());
            var pair = it.next();
            assertEquals("keyD", pair.key());
            assertEquals("validValue", pair.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void parsesMultilineValue() throws Exception {
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of(
                "multi = \"This is the first line \" ..",
                "\"and this is the second line \" ..",
                "\"and this is the last line\""));

        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext());
            var pair = it.next();
            assertEquals("multi", pair.key());
            assertEquals("This is the first line and this is the second line and this is the last line", pair.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void returnsNullOnNextIfNoMoreElements() throws Exception {
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of("key1 = \"value1\""));

        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext());
            it.next();
            assertFalse(it.hasNext());
            assertNull(it.next());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testMultipleCharsetsSupport() throws Exception {
        // Create a file with non-UTF-8 encoding
        Path tempFile = Files.createTempFile("pztest", ".txt");
        String content = "key1 = \"value1ü\""; // contains non-ASCII character
        Files.write(tempFile, content.getBytes(StandardCharsets.ISO_8859_1));

        // Set up language with multiple charsets: UTF-8 first (will fail), then
        // ISO_8859_1 (will succeed)
        PZLanguage lang = new PZLanguage("de", "German");
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8);
        lang.setCharset(new SemanticVersion("41"), StandardCharsets.ISO_8859_1);

        PZSource source = new PZSource("Test", new SemanticVersion("42"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        // Parser should try UTF-8 first, fail, then try ISO_8859_1 and succeed
        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext(), "Should parse with second charset");
            var pair = it.next();
            assertEquals("key1", pair.key());
            assertEquals("value1ü", pair.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testCharsetFallbackOrder() throws Exception {
        // Create a file with UTF-8 encoding
        Path tempFile = Files.createTempFile("pztest", ".txt");
        String content = "key1 = \"value1€\""; // Euro symbol
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

        // Set up language with multiple charsets in descending version order
        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("43"), StandardCharsets.UTF_8);
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.ISO_8859_1);
        lang.setCharset(new SemanticVersion("41"), Charset.forName("windows-1252"));

        PZSource source = new PZSource("Test", new SemanticVersion("43"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        // Parser uses charsets in order from newest to oldest version: UTF-8 first
        // (which succeeds)
        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext(), "Should parse with first charset");
            var pair = it.next();
            assertEquals("key1", pair.key());
            assertEquals("value1€", pair.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testCharsetDeduplication() throws Exception {
        // Create a simple UTF-8 file
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of("key1 = \"value1\""));

        // Set up language with duplicate charsets
        PZLanguage lang = new PZLanguage("en", "English");
        lang.setCharset(new SemanticVersion("43"), StandardCharsets.UTF_8);
        lang.setCharset(new SemanticVersion("42"), StandardCharsets.UTF_8); // duplicate
        lang.setCharset(new SemanticVersion("41"), StandardCharsets.ISO_8859_1);

        PZSource source = new PZSource("Test", new SemanticVersion("43"), tempFile.getParent(), true);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang, source);

        // Should work fine despite duplicates - first UTF-8 should succeed
        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext(), "Should parse successfully");
            var pair = it.next();
            assertEquals("key1", pair.key());
            assertEquals("value1", pair.value());
            assertFalse(it.hasNext());
        }
        Files.deleteIfExists(tempFile);
    }
}
