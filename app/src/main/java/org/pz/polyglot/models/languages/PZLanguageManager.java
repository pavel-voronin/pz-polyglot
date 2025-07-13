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

public class PZLanguageManager {
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
        } catch (IOException | DirectoryIteratorException exception) {
            Logger.warning("Failed to load language directory: " + folder.toString());
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

        } catch (IOException e) {
            Logger.warning("Failed to read language file: " + path.toString());
        }
    }
}
