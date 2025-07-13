package org.pz.polyglot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.translations.PZTranslationType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static Config instance;

    @JsonProperty("gamePath")
    private String gamePath;
    @JsonProperty("steamModsPath")
    private String steamModsPath;
    @JsonProperty("cachePath")
    private String cachePath;
    @JsonProperty("pzLanguages")
    private String[] pzLanguages = new String[0];
    @JsonProperty("pzTranslationTypes")
    private String[] pzTranslationTypes = new String[0];
    @JsonProperty("enabledSources")
    private String[] enabledSources = new String[0];
    @JsonProperty("disabledSources")
    private String[] disabledSources = new String[0];
    @JsonProperty("gamePathEditable")
    private boolean gamePathEditable = false;
    @JsonProperty("steamModsPathEditable")
    private boolean steamModsPathEditable = false;
    @JsonProperty("cachePathEditable")
    private boolean cachePathEditable = true;

    private volatile Instant lastSaveTime = Instant.EPOCH;
    private volatile ScheduledFuture<?> scheduledSave = null;
    private static final Duration SAVE_THROTTLE = Duration.ofSeconds(1);
    private volatile boolean autoSaveEnabled = false;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread t = Executors.defaultThreadFactory().newThread(runnable);
        t.setDaemon(true);
        return t;
    });

    public static Config getInstance() {
        if (instance == null) {
            instance = loadOrCreate();
        }
        return instance;
    }

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
     * Throttled save method that saves the config file only if enough time has
     * passed since the last save. If called too frequently, it schedules a save
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
            if (scheduledSave != null && !scheduledSave.isDone()) {
                return;
            }
            long delay = SAVE_THROTTLE.minus(sinceLast).toMillis();
            scheduledSave = scheduler.schedule(this::store, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Immediately saves the config file
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

    private static Path getConfigFilePath() {
        return Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
    }

    // Config values related methods

    public String getGamePath() {
        return gamePath;
    }

    public void setGamePath(String path) {
        this.gamePath = path;
        save();
    }

    public String getSteamModsPath() {
        return steamModsPath;
    }

    public void setSteamModsPath(String path) {
        this.steamModsPath = path;
        save();
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String path) {
        this.cachePath = path;
        save();
    }

    public String[] getPzLanguages() {
        return pzLanguages;
    }

    public void setPzLanguages(String[] pzLanguages) {
        this.pzLanguages = new LinkedHashSet<>(Arrays.asList(pzLanguages)).toArray(new String[0]);
        save();
    }

    public String[] getPzTranslationTypes() {
        return pzTranslationTypes;
    }

    public void setPzTranslationTypes(String[] pzTranslationTypes) {
        this.pzTranslationTypes = new LinkedHashSet<>(Arrays.asList(pzTranslationTypes)).toArray(new String[0]);
        save();
    }

    public boolean isGamePathEditable() {
        return gamePathEditable;
    }

    public void setGamePathEditable(boolean gamePathEditable) {
        this.gamePathEditable = gamePathEditable;
        save();
    }

    public boolean isSteamModsPathEditable() {
        return steamModsPathEditable;
    }

    public void setSteamModsPathEditable(boolean steamModsPathEditable) {
        this.steamModsPathEditable = steamModsPathEditable;
        save();
    }

    public boolean isCachePathEditable() {
        return cachePathEditable;
    }

    public void setCachePathEditable(boolean cachePathEditable) {
        this.cachePathEditable = cachePathEditable;
        save();
    }

    public String[] getEnabledSources() {
        return enabledSources;
    }

    public void setEnabledSources(String[] enabledSources) {
        this.enabledSources = new LinkedHashSet<>(Arrays.asList(enabledSources)).toArray(new String[0]);
        save();
    }

    public String[] getDisabledSources() {
        return disabledSources;
    }

    public void setDisabledSources(String[] disabledSources) {
        this.disabledSources = new LinkedHashSet<>(Arrays.asList(disabledSources)).toArray(new String[0]);
        save();
    }
}
