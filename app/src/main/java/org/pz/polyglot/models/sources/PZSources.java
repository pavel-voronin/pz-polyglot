package org.pz.polyglot.models.sources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pz.polyglot.Config;
import org.pz.polyglot.Logger;
import org.pz.polyglot.State;
import org.pz.polyglot.structs.SemanticVersion;
import org.pz.polyglot.utils.FolderUtils;

/**
 * Singleton class responsible for discovering and managing all available
 * Project Zomboid sources (mods and game files).
 * <p>
 * Sources are parsed from Steam, Workshop, local mods, and game files. New
 * sources are auto-enabled in the state.
 */
public class PZSources {
    /**
     * Singleton instance of PZSources.
     */
    private static PZSources instance;

    /**
     * List of all discovered sources, unsorted.
     */
    private final ArrayList<PZSource> sources = new ArrayList<>();

    /**
     * Returns the singleton instance of PZSources.
     * 
     * @return the singleton instance
     */
    public static PZSources getInstance() {
        if (instance == null) {
            instance = new PZSources();
        }
        return instance;
    }

    /**
     * Private constructor. Parses sources on instantiation.
     */
    private PZSources() {
        this.parseSources();
        Logger.info("Parsed sources: " + this.sources.size());
    }

    /**
     * Returns all sources sorted by priority.
     * 
     * @return sorted list of sources
     */
    public List<PZSource> getSources() {
        return this.sources.stream()
                .sorted(java.util.Comparator.comparingInt(PZSource::getPriority))
                .toList();
    }

    /**
     * Discovers and parses all available sources (mods and game files).
     * Also auto-enables new sources in the state.
     */
    public void parseSources() {
        this.sources.clear();

        // Discover Steam mods: [SteamItemId]/<mods>/[ModName]/
        FolderUtils.getSteamModsPath().ifPresent(this::processSteamMods);

        // Discover Workshop mods: [WorkshopProject]/<Contents>/<mods>/[ModName]/
        FolderUtils.getWorkshopPath().ifPresent(this::processWorkshopMods);

        // Discover local mods: [ModName]/
        FolderUtils.getModsPath().ifPresent(this::processLocalMods);

        // Discover game files
        FolderUtils.getGamePath().ifPresent(this::processGameFiles);

        // Detect new sources and auto-enable them in the state
        List<String> currentSources = this.sources.stream()
                .map(PZSource::getName)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        State state = State.getInstance();
        Set<String> knownSources = state.getAllKnownSources();

        for (String sourceName : currentSources) {
            if (!knownSources.contains(sourceName)) {
                state.addNewSource(sourceName);
            }
        }
    }

    /**
     * Processes Steam mods and adds them as sources.
     * 
     * @param steamPath path to Steam mods root
     */
    private void processSteamMods(Path steamPath) {
        boolean editable = Config.getInstance().isSteamModsPathEditable();
        int priority = 3;
        for (Path userFolder : listDirectories(steamPath)) {
            Path modsFolder = userFolder.resolve("mods");
            if (Files.isDirectory(modsFolder)) {
                for (Path modFolder : listDirectories(modsFolder)) {
                    String modName = modFolder.getFileName().toString();
                    addSourcesFromFolder(modName, modFolder, editable, priority);
                }
            }
        }
    }

    /**
     * Processes Workshop mods and adds them as sources.
     * 
     * @param workshopPath path to Workshop mods root
     */
    private void processWorkshopMods(Path workshopPath) {
        boolean editable = Config.getInstance().isCachePathEditable();
        int priority = 1;
        for (Path workshopFolder : listDirectories(workshopPath)) {
            Path modsFolder = workshopFolder.resolve("Contents").resolve("mods");
            if (Files.isDirectory(modsFolder)) {
                for (Path modFolder : listDirectories(modsFolder)) {
                    String modName = modFolder.getFileName().toString();
                    addSourcesFromFolder(modName, modFolder, editable, priority);
                }
            }
        }
    }

    /**
     * Processes local mods and adds them as sources.
     * 
     * @param modsPath path to local mods root
     */
    private void processLocalMods(Path modsPath) {
        boolean editable = Config.getInstance().isCachePathEditable();
        int priority = 2;
        for (Path modFolder : listDirectories(modsPath)) {
            String modName = modFolder.getFileName().toString();
            addSourcesFromFolder(modName, modFolder, editable, priority);
        }
    }

    /**
     * Processes game files and adds them as sources. Game files always use
     * BUILD_42.
     * 
     * @param gamePath path to game files root
     */
    private void processGameFiles(Path gamePath) {
        boolean editable = Config.getInstance().isGamePathEditable();
        int priority = 0;
        // Game files always use BUILD_42, regardless of detected structure
        for (Path translationPath : findTranslationPaths(gamePath)) {
            this.sources
                    .add(createSource("Game Files", translationPath, new SemanticVersion("42"), editable, priority));
        }
    }

    /**
     * Adds sources discovered in a mod or game folder.
     * 
     * @param sourceName   name of the source
     * @param sourceFolder folder containing the source
     * @param editable     whether the source is editable
     * @param priority     priority of the source
     */
    private void addSourcesFromFolder(String sourceName, Path sourceFolder, boolean editable, int priority) {
        for (Path translationPath : findTranslationPaths(sourceFolder)) {
            SemanticVersion version = detectBuildType(translationPath);
            // todo: 42-common, 42.9, etc.
            // Current BUILD implementation is not enough for all cases.
            // Easy to fix but Languages management will need to be reworked.
            this.sources.add(
                    createSource(sourceName + " [" + version.getMajor() + "]", translationPath, version, editable,
                            priority));
        }
    }

    /**
     * Returns all subdirectories of the given path, or an empty list if not a
     * directory.
     * 
     * @param path the path to list directories from
     * @return list of subdirectories
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
            // Directory listing failed, return empty result
        }
        return result;
    }

    /**
     * Finds all translation paths within a source folder.
     * Looks for both BUILD_41 and BUILD_42 structures, and version-specific
     * subfolders.
     * 
     * @param sourcePath the source folder to search
     * @return list of translation paths
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
            // Subdirectory listing failed, return what was found
        }

        return translationPaths;
    }

    /**
     * Detects the build type based on the translation path structure.
     * <ul>
     * <li>BUILD_41: .../media/lua/shared/Translate (without common/ and without
     * version 42)</li>
     * <li>BUILD_42: .../common/media/lua/shared/Translate or
     * .../42[.x.x]/media/lua/shared/Translate</li>
     * </ul>
     * <p>
     * Note: This method should NOT be used for game files directory - game files
     * always use BUILD_42 regardless of structure.
     * 
     * @param translationPath path to translation folder
     * @return detected build version
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
     * 
     * @param name            source name
     * @param translationPath path to translation folder
     * @param version         semantic version
     * @param editable        whether the source is editable
     * @param priority        source priority
     * @return new PZSource instance
     */
    private PZSource createSource(String name, Path translationPath, SemanticVersion version, boolean editable,
            int priority) {
        return new PZSource(name, version, translationPath, editable, priority);
    }
}
