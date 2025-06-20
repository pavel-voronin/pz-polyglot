package org.pz.polyglot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());

    private static volatile AppConfig instance;
    private File gameFolder, steamModsFolder, userModsFolder;

    @JsonProperty("language")
    private String language = "en";

    @JsonProperty("gameFolder")
    private String gameFolderPath;
    @JsonProperty("steamModsFolder")
    private String steamModsFolderPath;
    @JsonProperty("userModsFolder")
    private String userModsFolderPath;

    private static Path getConfigFilePath() {
        return Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }
        // Validate using Locale.forLanguageTag (accepts any valid BCP 47 tag)
        Locale locale = Locale.forLanguageTag(languageCode.trim());
        if (locale.getLanguage().isEmpty()) {
            throw new IllegalArgumentException("Invalid language code: " + languageCode);
        }
        this.language = languageCode.trim();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = loadOrCreate();
                }
            }
        }
        return instance;
    }

    private static AppConfig loadOrCreate() {
        Path configPath = getConfigFilePath();
        File configFile = configPath.toFile();
        if (configFile.exists()) {
            try {
                AppConfig loaded = objectMapper.readValue(configFile, AppConfig.class);
                logger.info("Config loaded successfully");
                return loaded;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load config, creating new one", e);
            }
        }
        logger.info("Creating new config file");
        AppConfig config = new AppConfig();
        config.save();
        return config;
    }

    public synchronized void save() {
        Path configPath = getConfigFilePath();
        File configFile = configPath.toFile();
        try {
            objectMapper.writeValue(configFile, this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save config file", e);
        }
    }

    public boolean hasValidFolders() {
        return getGameFolder() != null && getGameFolder().exists() &&
                getSteamModsFolder() != null && getSteamModsFolder().exists() &&
                getUserModsFolder() != null && getUserModsFolder().exists();
    }

    public File getGameFolder() {
        if (gameFolder == null && gameFolderPath != null) {
            gameFolder = new File(gameFolderPath);
        }
        return gameFolder;
    }

    public void setGameFolder(File f) {
        this.gameFolder = f;
        this.gameFolderPath = (f != null) ? f.getAbsolutePath() : null;
    }

    public File getSteamModsFolder() {
        if (steamModsFolder == null && steamModsFolderPath != null) {
            steamModsFolder = new File(steamModsFolderPath);
        }
        return steamModsFolder;
    }

    public void setSteamModsFolder(File f) {
        this.steamModsFolder = f;
        this.steamModsFolderPath = (f != null) ? f.getAbsolutePath() : null;
    }

    public File getUserModsFolder() {
        if (userModsFolder == null && userModsFolderPath != null) {
            userModsFolder = new File(userModsFolderPath);
        }
        return userModsFolder;
    }

    public void setUserModsFolder(File f) {
        this.userModsFolder = f;
        this.userModsFolderPath = (f != null) ? f.getAbsolutePath() : null;
    }
}
