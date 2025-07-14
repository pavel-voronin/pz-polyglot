package org.pz.polyglot.utils;

import java.io.File;
import java.util.Optional;

/**
 * Utility class for operating system-specific path detection.
 */
public class OsUtils {
    /**
     * Enum representing supported operating system types.
     */
    public enum OSType {
        WINDOWS, LINUX, MAC, UNKNOWN
    }

    /**
     * Detects the current operating system type.
     * 
     * @return the detected {@link OSType}
     */
    public static OSType getOsType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OSType.WINDOWS;
        }
        if (os.contains("mac")) {
            return OSType.MAC;
        }
        if (os.contains("nux") || os.contains("nix")) {
            return OSType.LINUX;
        }
        return OSType.UNKNOWN;
    }

    /**
     * Attempts to guess the Project Zomboid game installation folder based on OS.
     * 
     * @return an {@link Optional} containing the game folder if it exists,
     *         otherwise empty
     */
    public static Optional<File> guessGameFolder() {
        OSType os = getOsType();
        String userHome = System.getProperty("user.home");
        File path;
        switch (os) {
            case WINDOWS -> path = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\ProjectZomboid");
            case MAC ->
                path = new File(userHome + "/Library/Application Support/Steam/steamapps/common/ProjectZomboid");
            case LINUX -> path = new File(userHome + "/.steam/steam/steamapps/common/ProjectZomboid");
            default -> {
                return Optional.empty();
            }
        }
        // Return the path only if it exists
        return path.exists() ? Optional.of(path) : Optional.empty();
    }

    /**
     * Attempts to guess the Steam Workshop mods folder for Project Zomboid based on
     * OS.
     * 
     * @return an {@link Optional} containing the mods folder if it exists,
     *         otherwise empty
     */
    public static Optional<File> guessSteamModsFolder() {
        OSType os = getOsType();
        String userHome = System.getProperty("user.home");
        File path;
        switch (os) {
            case WINDOWS -> path = new File("C:\\Program Files (x86)\\Steam\\steamapps\\workshop\\content\\108600");
            case MAC ->
                path = new File(userHome + "/Library/Application Support/Steam/steamapps/workshop/content/108600");
            case LINUX -> path = new File(userHome + "/.steam/steam/steamapps/workshop/content/108600");
            default -> {
                return Optional.empty();
            }
        }
        // Return the path only if it exists
        return path.exists() ? Optional.of(path) : Optional.empty();
    }

    /**
     * Attempts to guess the Project Zomboid cache folder based on OS.
     * 
     * @return an {@link Optional} containing the cache folder if it exists,
     *         otherwise empty
     */
    public static Optional<File> guessCacheFolder() {
        OSType os = getOsType();
        String userHome = System.getProperty("user.home");
        File path;
        switch (os) {
            case WINDOWS -> path = new File(userHome + "\\Zomboid");
            case MAC, LINUX -> path = new File(userHome + "/Zomboid");
            default -> {
                return Optional.empty();
            }
        }
        return path.exists() ? Optional.of(path) : Optional.empty();
    }
}