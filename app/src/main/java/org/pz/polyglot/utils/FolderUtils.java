package org.pz.polyglot.utils;

import java.nio.file.Path;
import java.util.Optional;

import org.pz.polyglot.Config;

/**
 * Utility class for resolving and validating important domain-specific
 * directories
 * such as Workshop, Steam mods, mods, and game installation paths.
 * <p>
 * All methods return an {@link Optional} containing the resolved {@link Path}
 * if the directory exists and is valid.
 * The configuration is accessed via {@link Config}.
 */
public class FolderUtils {
    /**
     * Returns the path to the Workshop directory if it exists and is a directory.
     * The Workshop directory is resolved from the cache path in the configuration.
     *
     * @return an Optional containing the Workshop path if present, otherwise empty
     */
    public static Optional<Path> getWorkshopPath() {
        String cachePathStr = Config.getInstance().getCachePath();

        if (cachePathStr != null && !cachePathStr.isEmpty()) {
            Path path = Path.of(cachePathStr).resolve("Workshop");
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the path to the Steam mods directory if it exists and is a directory.
     * The path is taken directly from the configuration.
     *
     * @return an Optional containing the Steam mods path if present, otherwise
     *         empty
     */
    public static Optional<Path> getSteamModsPath() {
        String steamModsPathStr = Config.getInstance().getSteamModsPath();

        if (steamModsPathStr != null && !steamModsPathStr.isEmpty()) {
            Path path = Path.of(steamModsPathStr);
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the path to the mods directory if it exists and is a directory.
     * The mods directory is resolved from the cache path in the configuration.
     *
     * @return an Optional containing the mods path if present, otherwise empty
     */
    public static Optional<Path> getModsPath() {
        String cachePathStr = Config.getInstance().getCachePath();

        if (cachePathStr != null && !cachePathStr.isEmpty()) {
            Path path = Path.of(cachePathStr).resolve("mods");
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the path to the game directory if it exists and is a directory.
     * The path is taken directly from the configuration.
     *
     * @return an Optional containing the game path if present, otherwise empty
     */
    public static Optional<Path> getGamePath() {
        String gamePathStr = Config.getInstance().getGamePath();

        if (gamePathStr != null && !gamePathStr.isEmpty()) {
            Path path = Path.of(gamePathStr);
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }
}
