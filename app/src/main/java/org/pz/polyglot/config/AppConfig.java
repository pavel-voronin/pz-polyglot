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

    private static AppConfig instance;

    @JsonProperty("language")
    private String language = "en";
    @JsonProperty("gamePath")
    private String gamePath;
    @JsonProperty("steamModsPath")
    private String steamModsPath;
    @JsonProperty("cachePath")
    private String cachePath;

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = loadOrCreate();
        }
        return instance;
    }

    public void save() {
        Path configPath = getConfigFilePath();
        File configFile = configPath.toFile();
        try {
            objectMapper.writeValue(configFile, this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save config file", e);
        }
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

    public String getGamePath() {
        return gamePath;
    }

    public void setGamePath(String path) {
        this.gamePath = path;
    }

    public String getSteamModsPath() {
        return steamModsPath;
    }

    public void setSteamModsPath(String path) {
        this.steamModsPath = path;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String path) {
        this.cachePath = path;
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

    private static Path getConfigFilePath() {
        return Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
    }
}
