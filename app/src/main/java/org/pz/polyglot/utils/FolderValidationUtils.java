package org.pz.polyglot.utils;

import java.io.File;

import org.pz.polyglot.Config;

/**
 * Provides utility methods for validating folder paths in the application
 * configuration.
 * <p>
 * This class is not intended to be instantiated.
 */
public final class FolderValidationUtils {

    /**
     * Checks whether all required folder paths in the provided {@link Config}
     * instance are valid.
     * <p>
     * A folder path is considered valid if it is not null, not empty, exists on the
     * filesystem, and is a directory.
     *
     * @param config the configuration containing folder paths to validate
     * @return {@code true} if all folders are valid; {@code false} otherwise
     */
    public static boolean hasValidFolders(Config config) {
        return isValidFolder(config.getGamePath()) &&
                isValidFolder(config.getSteamModsPath()) &&
                isValidFolder(config.getCachePath());
    }

    /**
     * Validates a single folder path.
     * <p>
     * The path must be non-null, non-empty, exist, and be a directory.
     *
     * @param path the folder path to validate
     * @return {@code true} if the path is a valid directory; {@code false}
     *         otherwise
     */
    private static boolean isValidFolder(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
