package org.pz.polyglot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());

    public enum Language {
        ENGLISH("en");

        @JsonValue
        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonCreator
        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equalsIgnoreCase(code)) {
                    return lang;
                }
            }
            throw new IllegalArgumentException("Unsupported language: " + code);
        }
    }

    @JsonProperty("language")
    private Language language = Language.ENGLISH;

    private static volatile AppConfig instance;

    private static Path getConfigFilePath() {
        return Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
    }

    public String getLanguage() {
        return language.getCode();
    }

    public void setLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }
        this.language = Language.fromCode(languageCode.trim());
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
}
