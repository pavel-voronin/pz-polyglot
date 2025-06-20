package org.pz.polyglot.util;

import java.io.File;
import org.pz.polyglot.config.AppConfig;

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
    public static boolean hasValidFolders(AppConfig config) {
        return isValidFolder(config.getGamePath()) &&
                isValidFolder(config.getSteamModsPath()) &&
                isValidFolder(config.getUserModsPath());
    }

    private static boolean isValidFolder(String path) {
        if (path == null || path.isEmpty())
            return false;
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
