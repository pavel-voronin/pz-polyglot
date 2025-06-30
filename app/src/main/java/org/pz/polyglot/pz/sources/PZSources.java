package org.pz.polyglot.pz.sources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.pz.core.PZBuild;
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

    public List<PZSource> getSources() {
        return this.sources;
    }

    public void refreshSources() {
        parseSources();
        logger.info("Refreshed sources: " + this.sources.size());
    }

    public void parseSources() {
        this.sources.clear();
        
        AppConfig config = AppConfig.getInstance();

        // Steam mods: [SteamItemId]/<mods>/[ModName]/
        FolderUtils.getSteamModsPath().ifPresent(path -> {
            for (Path userFolder : listDirectories(path)) {
                Path modsFolder = userFolder.resolve("mods");
                if (Files.isDirectory(modsFolder)) {
                    for (Path mod : listDirectories(modsFolder)) {
                        checkAndAddSource(mod, config.isSteamModsPathEditable());
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
                        checkAndAddSource(mod, config.isCachePathEditable());
                    }
                }
            }
        });

        // Local mods: [ModName]/
        FolderUtils.getModsPath().ifPresent(path -> {
            for (Path mod : listDirectories(path)) {
                checkAndAddSource(mod, config.isCachePathEditable());
            }
        });

        // Game folder — check directly
        FolderUtils.getGamePath().ifPresent(path -> {
            checkAndAddSource(path, config.isGamePathEditable());
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
    private void checkAndAddSource(Path sourcePath, boolean editable) {
        if (Files.exists(sourcePath.resolve("media/lua/shared/Translate"))) {
            this.sources.add(
                    new PZSource(sourcePath.getFileName().toString(), PZBuild.BUILD_41,
                            sourcePath.resolve("media/lua/shared/Translate"), editable));
        }

        if (Files.exists(sourcePath.resolve("common/media/lua/shared/Translate"))) {
            this.sources.add(new PZSource(sourcePath.getFileName().toString(), PZBuild.BUILD_42,
                    sourcePath.resolve("common/media/lua/shared/Translate"), editable));
        }

        try (DirectoryStream<Path> subdirs = Files.newDirectoryStream(sourcePath, Files::isDirectory)) {
            for (Path subdir : subdirs) {
                String name = subdir.getFileName().toString();

                if (name.equals("common"))
                    continue;

                Path versionTranslate = subdir.resolve("media/lua/shared/Translate");

                if (Files.exists(versionTranslate)) {
                    this.sources
                            .add(new PZSource(sourcePath.getFileName().toString(), PZBuild.BUILD_42, versionTranslate,
                                    editable));
                }
            }
        } catch (IOException ignored) {
        }
    }
}
