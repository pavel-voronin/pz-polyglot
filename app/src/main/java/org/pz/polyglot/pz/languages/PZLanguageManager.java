package org.pz.polyglot.pz.languages;

import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.pz.polyglot.pz.core.PZBuild;
import org.pz.polyglot.pz.sources.PZSources;

public class PZLanguageManager {
    private static final Logger logger = Logger.getLogger(PZLanguages.class.getName());

    public static void load() {
        PZSources.getInstance().getSources().forEach(source -> {
            loadTranslateDirectory(source.getPath());
        });
    }

    private static void loadTranslateDirectory(Path folder) {
        Filter<Path> filter = p -> Files.isDirectory(p) && Files.exists(p.resolve("language.txt"));
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder, filter)) {
            directoryStream.forEach(PZLanguageManager::loadLanguage);
        } catch (java.io.IOException | java.nio.file.DirectoryIteratorException exception) {
            logger.warning("Failed to load language directory: " + folder.toString());
        }
    }

    private static void enrichLanguageWithFallbackCharset(PZLanguage language) {
        PZBuild.BUILD_41.getLanguages().getLanguage(language.getCode()).ifPresent(l -> {
            language.setFallbackCharset(l.getCharset());
        });
    }

    private static void loadLanguage(Path path) {
        try {
            String code = path.getFileName().toString();
            String body = Files.readString(path.resolve("language.txt"));
            PZLanguage language = PZLanguageParser.parse(code, body);
            if (language != null) {
                enrichLanguageWithFallbackCharset(language);
                PZBuild.BUILD_42.getLanguages().addLanguage(language);
            }
        } catch (java.io.IOException e) {
            logger.warning("Failed to read language file: " + path.toString());
        }
    }
}
