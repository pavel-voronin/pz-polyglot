package org.pz.polyglot.pz.sources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.structs.SemanticVersion;
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

    public void parseSources() {
        this.sources.clear();

        // Steam mods: [SteamItemId]/<mods>/[ModName]/
        FolderUtils.getSteamModsPath().ifPresent(this::processSteamMods);

        // Workshop mods: [WorkshopProject]/<Contents>/<mods>/[ModName]/
        FolderUtils.getWorkshopPath().ifPresent(this::processWorkshopMods);

        // Local mods: [ModName]/
        FolderUtils.getModsPath().ifPresent(this::processLocalMods);

        // Game files
        FolderUtils.getGamePath().ifPresent(this::processGameFiles);
    }

    private void processSteamMods(Path steamPath) {
        boolean editable = AppConfig.getInstance().isSteamModsPathEditable();
        for (Path userFolder : listDirectories(steamPath)) {
            Path modsFolder = userFolder.resolve("mods");
            if (Files.isDirectory(modsFolder)) {
                for (Path modFolder : listDirectories(modsFolder)) {
                    String modName = modFolder.getFileName().toString();
                    addSourcesFromFolder(modName, modFolder, editable);
                }
            }
        }
    }

    private void processWorkshopMods(Path workshopPath) {
        boolean editable = AppConfig.getInstance().isCachePathEditable();
        for (Path workshopFolder : listDirectories(workshopPath)) {
            Path modsFolder = workshopFolder.resolve("Contents").resolve("mods");
            if (Files.isDirectory(modsFolder)) {
                for (Path modFolder : listDirectories(modsFolder)) {
                    String modName = modFolder.getFileName().toString();
                    addSourcesFromFolder(modName, modFolder, editable);
                }
            }
        }
    }

    private void processLocalMods(Path modsPath) {
        boolean editable = AppConfig.getInstance().isCachePathEditable();
        for (Path modFolder : listDirectories(modsPath)) {
            String modName = modFolder.getFileName().toString();
            addSourcesFromFolder(modName, modFolder, editable);
        }
    }

    private void processGameFiles(Path gamePath) {
        boolean editable = AppConfig.getInstance().isGamePathEditable();
        // Game files always use BUILD_42, regardless of detected structure
        for (Path translationPath : findTranslationPaths(gamePath)) {
            this.sources.add(createSource("Game Files", translationPath, new SemanticVersion("42"), editable));
        }
    }

    private void addSourcesFromFolder(String sourceName, Path sourceFolder, boolean editable) {
        for (Path translationPath : findTranslationPaths(sourceFolder)) {
            SemanticVersion version = detectBuildType(translationPath);
            // todo: 42-common, 42.9, etc.
            // So current BUILD implementation is not enough
            // Easy to fix but Languages management will need to be reworked
            this.sources.add(
                    createSource(sourceName + " [" + version.getMajor() + "]", translationPath, version, editable));
        }
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
     * Finds all translation paths within a source folder.
     * Looks for both BUILD_41 and BUILD_42 structures.
     */
    private List<Path> findTranslationPaths(Path sourcePath) {
        List<Path> translationPaths = new ArrayList<>();

        // Check for BUILD_41 structure: media/lua/shared/Translate
        Path build41Path = sourcePath.resolve("media/lua/shared/Translate");
        if (Files.exists(build41Path)) {
            translationPaths.add(build41Path);
        }

        // Check for BUILD_42 structure: common/media/lua/shared/Translate
        Path build42Path = sourcePath.resolve("common/media/lua/shared/Translate");
        if (Files.exists(build42Path)) {
            translationPaths.add(build42Path);
        }

        // Check subdirectories for version-specific structures (e.g., "41", "42", etc.)
        try (DirectoryStream<Path> subdirs = Files.newDirectoryStream(sourcePath, Files::isDirectory)) {
            for (Path subdir : subdirs) {
                String subdirName = subdir.getFileName().toString();
                if ("common".equals(subdirName))
                    continue; // Already checked above

                Path versionTranslate = subdir.resolve("media/lua/shared/Translate");
                if (Files.exists(versionTranslate)) {
                    translationPaths.add(versionTranslate);
                }
            }
        } catch (IOException ignored) {
        }

        return translationPaths;
    }

    /**
     * Detects the build type based on the translation path structure.
     * BUILD_41: .../media/lua/shared/Translate (without common/ and without version
     * 42)
     * BUILD_42: .../common/media/lua/shared/Translate or
     * .../42[.x.x]/media/lua/shared/Translate
     * 
     * <p>
     * Note: This method should NOT be used for game files directory -
     * game files always use BUILD_42 regardless of structure.
     */
    SemanticVersion detectBuildType(Path translationPath) {
        String pathString = translationPath.toString().replace('\\', '/');

        // Check if path contains common folder - always BUILD_42
        if (pathString.contains("/common/media/lua/shared/Translate")) {
            return new SemanticVersion("42");
        }

        // Check for version 42 (simple or semver: 42, 42.x, 42.x.x, etc.)
        if (pathString.matches(".*/42(?:\\.\\d+)*+/media/lua/shared/Translate$")) {
            return new SemanticVersion("42");
        }

        // Everything else (including direct media/lua/shared/Translate or other
        // versions) is BUILD_41
        return new SemanticVersion("41");
    }

    /**
     * Creates a new PZSource with the given parameters.
     */
    private PZSource createSource(String name, Path translationPath, SemanticVersion version, boolean editable) {
        return new PZSource(name, version, translationPath, editable);
    }
}
