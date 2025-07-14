package org.pz.polyglot.models.languages;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.pz.polyglot.Logger;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.structs.SemanticVersion;

/**
 * Manages loading and parsing language data from source directories.
 */
public class PZLanguageManager {
    /**
     * Loads all available languages from the sources defined in {@link PZSources}.
     */
    public static void load() {
        PZSources.getInstance().getSources().forEach(source -> {
            // Use SemanticVersion directly from source
            loadTranslateDirectory(source.getPath(), source.getVersion());
        });
    }

    /**
     * Loads all language directories from the given folder for a specific version.
     * Only directories containing a 'language.txt' file are considered as language
     * directories.
     *
     * @param folder  the root directory containing language subdirectories
     * @param version the semantic version associated with the source
     */
    private static void loadTranslateDirectory(Path folder, SemanticVersion version) {
        // Only include directories that contain a 'language.txt' file
        Filter<Path> filter = p -> Files.isDirectory(p) && Files.exists(p.resolve("language.txt"));
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder, filter)) {
            directoryStream.forEach(languagePath -> loadLanguage(languagePath, version));
        } catch (IOException | DirectoryIteratorException exception) {
            Logger.warning("Failed to load language directory: " + folder.toString());
        }
    }

    /**
     * Loads and parses a single language from the specified directory.
     * Adds the parsed language and charset to the {@link PZLanguages} singleton.
     *
     * @param path    the path to the language directory
     * @param version the semantic version associated with the source
     */
    private static void loadLanguage(Path path, SemanticVersion version) {
        try {
            String langCode = path.getFileName().toString();
            String body = Files.readString(path.resolve("language.txt"));
            // Parse the language file and add its charset and text to the language registry
            PZLanguageParser.parse(langCode, body).ifPresent(parsed -> {
                PZLanguages.getInstance().addLanguageCharset(langCode, version, parsed.charset(), parsed.text());
            });
        } catch (IOException e) {
            Logger.warning("Failed to read language file: " + path.toString());
        }
    }
}
