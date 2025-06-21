package org.pz.polyglot.pz.sources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.pz.polyglot.util.FolderUtils;

public class PZSources {
    private static final Logger logger = Logger.getLogger(PZSources.class.getName());
    private static PZSources instance;

    private ArrayList<PZSource> sources = new ArrayList<>();

    public static PZSources getInstance() {
        if (instance == null) {
            instance = new PZSources();
        }
        return instance;
    }

    private PZSources() {
        this.parseSources();

        logger.info("Parsed sources: " + this.sources.size());
    }

    public void parseSources() {
        this.sources.clear();

        // Steam mods: [SteamItemId]/<mods>/[ModName]/
        FolderUtils.getSteamModsPath().ifPresent(path -> {
            for (Path userFolder : listDirectories(path)) {
                Path modsFolder = userFolder.resolve("mods");
                if (Files.isDirectory(modsFolder)) {
                    for (Path mod : listDirectories(modsFolder)) {
                        checkAndAddSource(mod);
                    }
                }
            }
        });

        // Workshop mods: [WorkshopProject]/<Contents>/<mods>/[ModName]/
        FolderUtils.getWorkshopPath().ifPresent(path -> {
            for (Path workshopFolder : listDirectories(path)) {
                Path contentsFolder = workshopFolder.resolve("Contents");
                Path modsFolder = contentsFolder.resolve("mods");
                if (Files.isDirectory(modsFolder)) {
                    for (Path mod : listDirectories(modsFolder)) {
                        checkAndAddSource(mod);
                    }
                }
            }
        });

        // Local mods: [ModName]/
        FolderUtils.getModsPath().ifPresent(path -> {
            for (Path mod : listDirectories(path)) {
                checkAndAddSource(mod);
            }
        });

        // Game folder — check directly
        FolderUtils.getGamePath().ifPresent(path -> {
            checkAndAddSource(path);
        });
    }

    /**
     * Returns all subdirectories of the given path, or empty list if not a
     * directory.
     */
    private List<Path> listDirectories(Path path) {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(path))
            return result;
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(path, Files::isDirectory)) {
            for (Path dir : dirs) {
                result.add(dir);
            }
        } catch (IOException ignored) {
        }
        return result;
    }

    /**
     * Checks a source folder for known translation structures and adds to sources
     * list if
     * found.
     */
    private void checkAndAddSource(Path sourcePath) {
        if (Files.exists(sourcePath.resolve("media/lua/shared/Translate"))) {
            this.sources.add(
                    new PZSource(sourcePath.getFileName().toString(), "41",
                            sourcePath.resolve("media/lua/shared/Translate").toFile()));
        }

        if (Files.exists(sourcePath.resolve("common/media/lua/shared/Translate"))) {
            this.sources.add(new PZSource(sourcePath.getFileName().toString(), "42-common",
                    sourcePath.resolve("common/media/lua/shared/Translate").toFile()));
        }

        try (DirectoryStream<Path> subdirs = Files.newDirectoryStream(sourcePath, Files::isDirectory)) {
            for (Path subdir : subdirs) {
                String name = subdir.getFileName().toString();

                if (name.equals("common"))
                    continue;

                Path versionTranslate = subdir.resolve("media/lua/shared/Translate");

                if (Files.exists(versionTranslate)) {
                    this.sources
                            .add(new PZSource(sourcePath.getFileName().toString(), name, versionTranslate.toFile()));
                }
            }
        } catch (IOException ignored) {
        }
    }
}
