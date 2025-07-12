package org.pz.polyglot;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.pz.polyglot.models.translations.PZTranslationSession;
import org.pz.polyglot.models.translations.PZTranslationType;
import org.pz.polyglot.models.languages.PZLanguages;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Centralized Observable state manager for UI components.
 * Replaces callback-based communication with reactive Properties.
 */
public class State {
    private static State instance;

    // Observable properties
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);
    private final StringProperty selectedTranslationKey = new SimpleStringProperty();
    private final BooleanProperty rightPanelVisible = new SimpleBooleanProperty(false);
    private final StringProperty refreshKey = new SimpleStringProperty(); // For specific key refresh
    private final BooleanProperty saveAllTriggered = new SimpleBooleanProperty(false); // For save all events
    private final BooleanProperty tableRebuildRequired = new SimpleBooleanProperty(false); // For full table rebuild
    private final ObservableList<String> visibleLanguages = FXCollections.observableArrayList();
    private final StringProperty filterText = new SimpleStringProperty(""); // For global filter management
    private final EnumSet<PZTranslationType> selectedTypes = EnumSet.noneOf(PZTranslationType.class);
    private final BooleanProperty selectedTypesChanged = new SimpleBooleanProperty(false);
    private final BooleanProperty typesPanelVisible = new SimpleBooleanProperty(false); // For TypesPanel visibility
    private final BooleanProperty languagesPanelVisible = new SimpleBooleanProperty(false);

    private State() {
        // Initialize with current session state
        updateHasChangesFromSession();

        // Initialize visible languages from config
        initializeVisibleLanguagesFromConfig();

        initializeSelectedTypesFromConfig();
    }

    public static State getInstance() {
        if (instance == null) {
            instance = new State();
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

    public StringProperty filterTextProperty() {
        return filterText;
    }

    public BooleanProperty selectedTypesChangedProperty() {
        return selectedTypesChanged;
    }

    public BooleanProperty typesPanelVisibleProperty() {
        return typesPanelVisible;
    }

    public BooleanProperty languagesPanelVisibleProperty() {
        return languagesPanelVisible;
    }

    public Set<PZTranslationType> getSelectedTypes() {
        return EnumSet.copyOf(selectedTypes);
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

        // Save to configuration
        AppConfig config = AppConfig.getInstance();
        config.setPzLanguages(languages.toArray(new String[0]));
        config.save();
    }

    public String getFilterText() {
        return filterText.get();
    }

    public void setFilterText(String value) {
        filterText.set(value);
    }

    public void setSelectedTypes(Set<PZTranslationType> types) {
        selectedTypes.clear();
        selectedTypes.addAll(types);
        AppConfig config = AppConfig.getInstance();
        String[] typeNames = selectedTypes.stream().map(Enum::name).toArray(String[]::new);
        config.setPzTranslationTypes(typeNames);
        config.save();
        selectedTypesChanged.set(!selectedTypesChanged.get()); // Notify listeners once
    }

    public boolean isTypesPanelVisible() {
        return typesPanelVisible.get();
    }

    public void setTypesPanelVisible(boolean visible) {
        typesPanelVisible.set(visible);
    }

    public boolean isLanguagesPanelVisible() {
        return languagesPanelVisible.get();
    }

    public void setLanguagesPanelVisible(boolean visible) {
        languagesPanelVisible.set(visible);
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
                // Show all languages, but English first
                var allCodes = PZLanguages.getInstance().getAllLanguageCodes();
                var sortedCodes = allCodes.stream()
                        .sorted((a, b) -> {
                            if (a.equals("EN"))
                                return -1;
                            if (b.equals("EN"))
                                return 1;
                            return a.compareTo(b);
                        })
                        .toList();
                visibleLanguages.setAll(sortedCodes);
            }
        } catch (Exception e) {
            // If config loading fails, default to showing only EN
            visibleLanguages.setAll(Arrays.asList("EN"));
        }
    }

    private void initializeSelectedTypesFromConfig() {
        AppConfig config = AppConfig.getInstance();
        String[] cfgTypes = config.getPzTranslationTypes();
        selectedTypes.clear();
        for (String typeName : cfgTypes) {
            PZTranslationType.fromString(typeName).ifPresent(selectedTypes::add);
        }
    }
}
