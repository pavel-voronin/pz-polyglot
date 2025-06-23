package org.pz.polyglot.pz.translations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.pz.polyglot.pz.languages.PZLanguage;

class PZTranslationParserTest {
    @Test
    void parsesSimpleKeyValue() throws Exception {
        // Prepare a temporary file with a simple key-value pair
        Path tempFile = Files.createTempFile("pztest", ".txt");
        Files.write(tempFile, List.of("key1 = \"value1\""));

        // Prepare minimal language and file objects
        PZLanguage lang = new PZLanguage("en", "English", StandardCharsets.UTF_8);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang);

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

        PZLanguage lang = new PZLanguage("en", "English", StandardCharsets.UTF_8);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang);

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

        PZLanguage lang = new PZLanguage("en", "English", StandardCharsets.UTF_8);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang);

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

        PZLanguage lang = new PZLanguage("en", "English", StandardCharsets.UTF_8);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang);

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

        PZLanguage lang = new PZLanguage("en", "English", StandardCharsets.UTF_8);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang);

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

        PZLanguage lang = new PZLanguage("en", "English", StandardCharsets.UTF_8);
        PZTranslationFile file = new PZTranslationFile(tempFile, PZTranslationType.UI, lang);

        try (PZTranslationParser parser = new PZTranslationParser(file)) {
            var it = parser.iterator();
            assertTrue(it.hasNext());
            it.next();
            assertFalse(it.hasNext());
            assertNull(it.next());
        }
        Files.deleteIfExists(tempFile);
    }
}
