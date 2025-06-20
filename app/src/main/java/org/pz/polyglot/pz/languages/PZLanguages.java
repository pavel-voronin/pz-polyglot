package org.pz.polyglot.pz.languages;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.pz.polyglot.config.AppConfig;

public final class PZLanguages {
    private static final Logger logger = Logger.getLogger(PZLanguages.class.getName());

    private static final String MEDIA_LUA_SHARED_TRANSLATE = "media/lua/shared/Translate";
    private final ArrayList<PZLanguage> languages = new ArrayList<>();
    public static PZLanguages instance = new PZLanguages();

    public void load() {
        String gamePath = AppConfig.getInstance().getGamePath();
        if (gamePath == null || gamePath.isEmpty()) {
            logger.warning("Game folder path is null or empty in AppConfig. Languages will not load translations.");
        } else {
            this.loadTranslateDirectory(
                    Path.of(gamePath, MEDIA_LUA_SHARED_TRANSLATE));
        }
    }

    private void loadTranslateDirectory(Path path) {
        Filter<Path> filter = p -> Files.isDirectory(p) && Files.exists(p.resolve("language.txt"));
        if (Files.exists(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, filter)) {
                for (Path path1 : directoryStream) {
                    PZLanguage language = this.loadLanguage(path1.toAbsolutePath());
                    if (language != null) {
                        this.languages.add(language);
                    }
                }
            } catch (java.io.IOException | java.nio.file.DirectoryIteratorException exception) {
                logger.warning("Failed to load language directory: " + path.toString());
            }
        }

    }

    private PZLanguage loadLanguage(Path path) {
        String name = path.getFileName().toString();
        String body;

        try {
            body = Files.readString(path.resolve("language.txt"));
        } catch (java.io.IOException e) {
            logger.warning("Failed to read language file: " + path.toString());
            return null;
        }

        return PZLanguageParser.parse(name, body);
    }
}
