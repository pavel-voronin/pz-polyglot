package org.pz.polyglot.pz.languages;

import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.pz.polyglot.pz.sources.PZSources;
import org.pz.polyglot.structs.SemanticVersion;

public class PZLanguageManager {
    private static final Logger logger = Logger.getLogger(PZLanguages.class.getName());

    public static void load() {
        PZSources.getInstance().getSources().forEach(source -> {
            // Use SemanticVersion directly from source
            loadTranslateDirectory(source.getPath(), source.getVersion());
        });
    }

    private static void loadTranslateDirectory(Path folder, SemanticVersion version) {
        Filter<Path> filter = p -> Files.isDirectory(p) && Files.exists(p.resolve("language.txt"));
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder, filter)) {
            directoryStream.forEach(languagePath -> loadLanguage(languagePath, version));
        } catch (java.io.IOException | java.nio.file.DirectoryIteratorException exception) {
            logger.warning("Failed to load language directory: " + folder.toString());
        }
    }

    private static void loadLanguage(Path path, SemanticVersion version) {
        try {
            String langCode = path.getFileName().toString();
            String body = Files.readString(path.resolve("language.txt"));
            PZLanguageParser.parse(langCode, body).ifPresent(parsed -> {
                // Add charset to singleton languages with version
                PZLanguages.getInstance().addLanguageCharset(langCode, version, parsed.charset(), parsed.text());
            });

        } catch (java.io.IOException e) {
            logger.warning("Failed to read language file: " + path.toString());
        }
    }
}
