package org.pz.polyglot.util;

import java.nio.file.Path;
import java.util.Optional;

import org.pz.polyglot.AppConfig;

public class FolderUtils {
    public static Optional<Path> getWorkshopPath() {
        String cachePathStr = AppConfig.getInstance().getCachePath();

        if (cachePathStr != null && !cachePathStr.isEmpty()) {
            Path path = Path.of(cachePathStr).resolve("Workshop");
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    public static Optional<Path> getSteamModsPath() {
        String steamModsPathStr = AppConfig.getInstance().getSteamModsPath();

        if (steamModsPathStr != null && !steamModsPathStr.isEmpty()) {
            Path path = Path.of(steamModsPathStr);
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    public static Optional<Path> getModsPath() {
        String cachePathStr = AppConfig.getInstance().getCachePath();

        if (cachePathStr != null && !cachePathStr.isEmpty()) {
            Path path = Path.of(cachePathStr).resolve("mods");
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    public static Optional<Path> getGamePath() {
        String gamePathStr = AppConfig.getInstance().getGamePath();

        if (gamePathStr != null && !gamePathStr.isEmpty()) {
            Path path = Path.of(gamePathStr);
            if (path.toFile().exists() && path.toFile().isDirectory()) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }
}
