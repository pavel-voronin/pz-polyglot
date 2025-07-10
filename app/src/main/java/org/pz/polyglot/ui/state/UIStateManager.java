package org.pz.polyglot.ui.state;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.pz.polyglot.config.AppConfig;
import org.pz.polyglot.pz.translations.PZTranslationSession;

import java.util.Arrays;
import java.util.List;

/**
 * Centralized Observable state manager for UI components.
 * Replaces callback-based communication with reactive Properties.
 */
public class UIStateManager {
    private static UIStateManager instance;

    // Observable properties
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);
    private final StringProperty selectedTranslationKey = new SimpleStringProperty();
    private final BooleanProperty rightPanelVisible = new SimpleBooleanProperty(false);
    private final StringProperty refreshKey = new SimpleStringProperty(); // For specific key refresh
    private final BooleanProperty saveAllTriggered = new SimpleBooleanProperty(false); // For save all events
    private final BooleanProperty tableRebuildRequired = new SimpleBooleanProperty(false); // For full table rebuild
    private final ObservableList<String> visibleLanguages = FXCollections.observableArrayList();

    private UIStateManager() {
        // Initialize with current session state
        updateHasChangesFromSession();

        // Initialize visible languages from config
        initializeVisibleLanguagesFromConfig();
    }

    public static UIStateManager getInstance() {
        if (instance == null) {
            instance = new UIStateManager();
        }
        return instance;
    }

    // Properties accessors
    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    public StringProperty selectedTranslationKeyProperty() {
        return selectedTranslationKey;
    }

    public BooleanProperty rightPanelVisibleProperty() {
        return rightPanelVisible;
    }

    public StringProperty refreshKeyProperty() {
        return refreshKey;
    }

    public BooleanProperty saveAllTriggeredProperty() {
        return saveAllTriggered;
    }

    public BooleanProperty tableRebuildRequiredProperty() {
        return tableRebuildRequired;
    }

    public ObservableList<String> getVisibleLanguages() {
        return visibleLanguages;
    }

    // Convenience methods
    public void setHasChanges(boolean value) {
        hasChanges.set(value);
    }

    public boolean getHasChanges() {
        return hasChanges.get();
    }

    public void setSelectedTranslationKey(String key) {
        selectedTranslationKey.set(key);
    }

    public String getSelectedTranslationKey() {
        return selectedTranslationKey.get();
    }

    public void setRightPanelVisible(boolean visible) {
        rightPanelVisible.set(visible);
    }

    public boolean isRightPanelVisible() {
        return rightPanelVisible.get();
    }

    public void triggerRefreshForKey(String key) {
        refreshKey.set(key);
    }

    public void updateVisibleLanguages(List<String> languages) {
        visibleLanguages.setAll(languages);
    }

    /**
     * Updates hasChanges property based on current PZTranslationSession state.
     */
    public void updateHasChangesFromSession() {
        PZTranslationSession session = PZTranslationSession.getInstance();
        boolean sessionHasChanges = !session.getVariants().isEmpty();
        setHasChanges(sessionHasChanges);
    }

    /**
     * Called when right panel should be closed and selection cleared.
     */
    public void closeRightPanel() {
        setRightPanelVisible(false);
        setSelectedTranslationKey(null);
    }

    /**
     * Triggers save all event for reactive components.
     */
    public void triggerSaveAllEvent() {
        saveAllTriggered.set(!saveAllTriggered.get()); // Toggle to trigger listeners
    }

    /**
     * Requests a table refresh for all entries.
     */
    public void requestTableRefresh() {
        // Trigger refresh by setting refreshKey to empty string (means refresh all)
        refreshKey.set("");
    }

    /**
     * Requests a complete table rebuild (for adding new entries).
     */
    public void requestTableRebuild() {
        tableRebuildRequired.set(!tableRebuildRequired.get()); // Toggle to trigger listeners
    }

    /**
     * Initializes visible languages from configuration.
     * If no languages are configured, defaults to showing only EN.
     */
    private void initializeVisibleLanguagesFromConfig() {
        try {
            AppConfig config = AppConfig.getInstance();
            String[] cfgLangs = config.getPzLanguages();
            if (cfgLangs != null && cfgLangs.length > 0) {
                visibleLanguages.setAll(Arrays.asList(cfgLangs));
            } else {
                // Default to showing only EN if no languages are configured
                // FIXME #18
                visibleLanguages.setAll(Arrays.asList("EN"));
            }
        } catch (Exception e) {
            // If config loading fails, default to showing only EN
            visibleLanguages.setAll(Arrays.asList("EN"));
        }
    }
}
