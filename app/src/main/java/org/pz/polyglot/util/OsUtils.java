package org.pz.polyglot.util;

import java.io.File;
import java.util.Optional;

// todo: check macos and linux paths
public class OsUtils {
    public enum OSType {
        WINDOWS, LINUX, MAC, UNKNOWN
    }

    public static OSType getOsType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return OSType.WINDOWS;
        if (os.contains("mac"))
            return OSType.MAC;
        if (os.contains("nux") || os.contains("nix"))
            return OSType.LINUX;
        return OSType.UNKNOWN;
    }

    public static Optional<File> guessGameFolder() {
        OSType os = getOsType();
        String userHome = System.getProperty("user.home");
        File path = null;
        switch (os) {
            case WINDOWS:
                path = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\ProjectZomboid");
                break;
            case MAC:
                path = new File(userHome + "/Library/Application Support/Steam/steamapps/common/ProjectZomboid");
                break;
            case LINUX:
                path = new File(userHome + "/.steam/steam/steamapps/common/ProjectZomboid");
                break;
            default:
                return Optional.empty();
        }
        return path.exists() ? Optional.of(path) : Optional.empty();
    }

    public static Optional<File> guessSteamModsFolder() {
        OSType os = getOsType();
        String userHome = System.getProperty("user.home");
        File path = null;
        switch (os) {
            case WINDOWS:
                path = new File("C:\\Program Files (x86)\\Steam\\steamapps\\workshop\\content\\108600");
                break;
            case MAC:
                path = new File(userHome + "/Library/Application Support/Steam/steamapps/workshop/content/108600");
                break;
            case LINUX:
                path = new File(userHome + "/.steam/steam/steamapps/workshop/content/108600");
                break;
            default:
                return Optional.empty();
        }
        return path.exists() ? Optional.of(path) : Optional.empty();
    }

    public static Optional<File> guessUserModsFolder() {
        OSType os = getOsType();
        String userHome = System.getProperty("user.home");
        File path = null;
        switch (os) {
            case WINDOWS:
                path = new File(userHome + "\\Zomboid\\Workshop");
                break;
            case MAC:
                path = new File(userHome + "/Zomboid/Workshop");
                break;
            case LINUX:
                path = new File(userHome + "/Zomboid/Workshop");
                break;
            default:
                return Optional.empty();
        }
        return path.exists() ? Optional.of(path) : Optional.empty();
    }
}