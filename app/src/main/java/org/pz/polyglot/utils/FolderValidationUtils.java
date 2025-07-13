package org.pz.polyglot.utils;

import java.io.File;

import org.pz.polyglot.Config;

/**
 * Utility class for folder validation logic.
 */
public class FolderValidationUtils {
    /**
     * Checks if the given AppConfig has valid folder paths (exist and are
     * directories).
     * 
     * @param config the AppConfig instance
     * @return true if all folders are valid, false otherwise
     */
    public static boolean hasValidFolders(Config config) {
        return isValidFolder(config.getGamePath()) &&
                isValidFolder(config.getSteamModsPath()) &&
                isValidFolder(config.getCachePath());
    }

    private static boolean isValidFolder(String path) {
        if (path == null || path.isEmpty())
            return false;
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
