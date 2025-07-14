package org.pz.polyglot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.translations.PZTranslationType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    /** Name of the config file. */
    private static final String CONFIG_FILE_NAME = "config.json";
    /** ObjectMapper for JSON serialization. */
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** Singleton instance of Config. */
    private static Config instance;

    /** Path to the game installation. */
    @JsonProperty("gamePath")
    private String gamePath;
    /** Path to Steam mods directory. */
    @JsonProperty("steamModsPath")
    private String steamModsPath;
    /** Path to cache directory. */
    @JsonProperty("cachePath")
    private String cachePath;
    /** Supported Project Zomboid languages. */
    @JsonProperty("pzLanguages")
    private String[] pzLanguages = new String[0];
    /** Supported Project Zomboid translation types. */
    @JsonProperty("pzTranslationTypes")
    private String[] pzTranslationTypes = new String[0];
    /** Enabled translation sources. */
    @JsonProperty("enabledSources")
    private String[] enabledSources = new String[0];
    /** Disabled translation sources. */
    @JsonProperty("disabledSources")
    private String[] disabledSources = new String[0];
    /** Whether the game path is editable. */
    @JsonProperty("gamePathEditable")
    private boolean gamePathEditable = false;
    /** Whether the Steam mods path is editable. */
    @JsonProperty("steamModsPathEditable")
    private boolean steamModsPathEditable = false;
    /** Whether the cache path is editable. */
    @JsonProperty("cachePathEditable")
    private boolean cachePathEditable = true;

    /** Last time the config was saved. */
    private volatile Instant lastSaveTime = Instant.EPOCH;
    /** Scheduled future for throttled save. */
    private volatile ScheduledFuture<?> scheduledSave = null;
    /** Minimum duration between saves. */
    private static final Duration SAVE_THROTTLE = Duration.ofSeconds(1);
    /** Whether auto-save is enabled. */
    private volatile boolean autoSaveEnabled = false;
    /** Scheduler for throttled saves. */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread t = Executors.defaultThreadFactory().newThread(runnable);
        t.setDaemon(true);
        return t;
    });

    /**
     * Returns the singleton instance of Config, loading or creating it if
     * necessary.
     * 
     * @return Config instance
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = loadOrCreate();
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private Config() {
    }

    /**
     * Enables throttled auto-save for config changes. Should be called after app
     * initialization.
     */
    public void enableAutoSave() {
        autoSaveEnabled = true;
        save();
    }

    /**
     * Saves the config file, throttled to avoid frequent disk writes.
     * If called too frequently, schedules a save after the throttle duration.
     */
    private void save() {
        if (!autoSaveEnabled) {
            Logger.debug("Auto-save is not enabled, skipping save");
            return;
        }
        Instant now = Instant.now();
        Duration sinceLast = Duration.between(lastSaveTime, now);
        if (sinceLast.compareTo(SAVE_THROTTLE) >= 0) {
            store();
        } else {
            // Only schedule a save if one is not already scheduled
            if (scheduledSave != null && !scheduledSave.isDone()) {
                return;
            }
            long delay = SAVE_THROTTLE.minus(sinceLast).toMillis();
            scheduledSave = scheduler.schedule(this::store, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Immediately saves the config file to disk.
     */
    private void store() {
        lastSaveTime = Instant.now();
        Path configPath = getConfigFilePath();
        File configFile = configPath.toFile();
        try {
            objectMapper.writeValue(configFile, this);
        } catch (IOException e) {
            Logger.error("Failed to save config file", e);
        }
    }

    /**
     * Loads the config from disk, or creates a new one if not found or failed to
     * load.
     * 
     * @return Config instance
     */
    private static Config loadOrCreate() {
        Path configPath = getConfigFilePath();
        File configFile = configPath.toFile();
        if (configFile.exists()) {
            try {
                Config loaded = objectMapper.readValue(configFile, Config.class);
                Logger.info("Config loaded successfully");
                return loaded;
            } catch (IOException e) {
                Logger.error("Failed to load config, creating new one", e);
            }
        }
        Logger.info("Creating new config file");
        Config config = new Config();
        config.setPzLanguages(PZLanguages.getInstance().getAllLanguageCodes()
                .toArray(new String[0]));
        config.setPzTranslationTypes(Arrays.stream(PZTranslationType.values())
                .map(Enum::name).toArray(String[]::new));
        return config;
    }

    /**
     * Returns the path to the config file in the current working directory.
     * 
     * @return Path to config.json
     */
    private static Path getConfigFilePath() {
        return Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
    }

    // Config values related methods

    /**
     * Gets the game installation path.
     * 
     * @return game path
     */
    public String getGamePath() {
        return gamePath;
    }

    /**
     * Sets the game installation path and saves config.
     * 
     * @param path game path
     */
    public void setGamePath(String path) {
        this.gamePath = path;
        save();
    }

    /**
     * Gets the Steam mods path.
     * 
     * @return Steam mods path
     */
    public String getSteamModsPath() {
        return steamModsPath;
    }

    /**
     * Sets the Steam mods path and saves config.
     * 
     * @param path Steam mods path
     */
    public void setSteamModsPath(String path) {
        this.steamModsPath = path;
        save();
    }

    /**
     * Gets the cache directory path.
     * 
     * @return cache path
     */
    public String getCachePath() {
        return cachePath;
    }

    /**
     * Sets the cache directory path and saves config.
     * 
     * @param path cache path
     */
    public void setCachePath(String path) {
        this.cachePath = path;
        save();
    }

    /**
     * Gets the supported Project Zomboid languages.
     * 
     * @return array of language codes
     */
    public String[] getPzLanguages() {
        return pzLanguages;
    }

    /**
     * Sets the supported Project Zomboid languages and saves config.
     * Removes duplicates and preserves order.
     * 
     * @param pzLanguages array of language codes
     */
    public void setPzLanguages(String[] pzLanguages) {
        this.pzLanguages = new LinkedHashSet<>(Arrays.asList(pzLanguages)).toArray(new String[0]);
        save();
    }

    /**
     * Gets the supported Project Zomboid translation types.
     * 
     * @return array of translation type names
     */
    public String[] getPzTranslationTypes() {
        return pzTranslationTypes;
    }

    /**
     * Sets the supported Project Zomboid translation types and saves config.
     * Removes duplicates and preserves order.
     * 
     * @param pzTranslationTypes array of translation type names
     */
    public void setPzTranslationTypes(String[] pzTranslationTypes) {
        this.pzTranslationTypes = new LinkedHashSet<>(Arrays.asList(pzTranslationTypes)).toArray(new String[0]);
        save();
    }

    /**
     * Returns whether the game path is editable.
     * 
     * @return true if editable
     */
    public boolean isGamePathEditable() {
        return gamePathEditable;
    }

    /**
     * Sets whether the game path is editable and saves config.
     * 
     * @param gamePathEditable editable flag
     */
    public void setGamePathEditable(boolean gamePathEditable) {
        this.gamePathEditable = gamePathEditable;
        save();
    }

    /**
     * Returns whether the Steam mods path is editable.
     * 
     * @return true if editable
     */
    public boolean isSteamModsPathEditable() {
        return steamModsPathEditable;
    }

    /**
     * Sets whether the Steam mods path is editable and saves config.
     * 
     * @param steamModsPathEditable editable flag
     */
    public void setSteamModsPathEditable(boolean steamModsPathEditable) {
        this.steamModsPathEditable = steamModsPathEditable;
        save();
    }

    /**
     * Returns whether the cache path is editable.
     * 
     * @return true if editable
     */
    public boolean isCachePathEditable() {
        return cachePathEditable;
    }

    /**
     * Sets whether the cache path is editable and saves config.
     * 
     * @param cachePathEditable editable flag
     */
    public void setCachePathEditable(boolean cachePathEditable) {
        this.cachePathEditable = cachePathEditable;
        save();
    }

    /**
     * Gets the enabled translation sources.
     * 
     * @return array of enabled sources
     */
    public String[] getEnabledSources() {
        return enabledSources;
    }

    /**
     * Sets the enabled translation sources and saves config.
     * Removes duplicates and preserves order.
     * 
     * @param enabledSources array of enabled sources
     */
    public void setEnabledSources(String[] enabledSources) {
        this.enabledSources = new LinkedHashSet<>(Arrays.asList(enabledSources)).toArray(new String[0]);
        save();
    }

    /**
     * Gets the disabled translation sources.
     * 
     * @return array of disabled sources
     */
    public String[] getDisabledSources() {
        return disabledSources;
    }

    /**
     * Sets the disabled translation sources and saves config.
     * Removes duplicates and preserves order.
     * 
     * @param disabledSources array of disabled sources
     */
    public void setDisabledSources(String[] disabledSources) {
        this.disabledSources = new LinkedHashSet<>(Arrays.asList(disabledSources)).toArray(new String[0]);
        save();
    }
}
