package org.pz.polyglot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import org.pz.polyglot.models.translations.PZTranslationType;
import org.pz.polyglot.models.TranslationSession;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.WorkMode;

/**
 * Centralized Observable state manager for UI components.
 * <p>
 * Singleton pattern is used to ensure a single state instance across the
 * application.
 */
public class State {
    /**
     * Singleton instance of State.
     */
    private static State instance;

    /** Indicates if there are unsaved changes in the current session. */
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);
    /** Currently selected translation key in the UI. */
    private final StringProperty selectedTranslationKey = new SimpleStringProperty();
    /** Controls visibility of the right panel in the UI. */
    private final BooleanProperty rightPanelVisible = new SimpleBooleanProperty(false);
    /** Used to trigger refresh for a specific translation key. */
    private final StringProperty refreshKey = new SimpleStringProperty();
    /** Used to trigger save all event for reactive components. */
    private final BooleanProperty saveAllTriggered = new SimpleBooleanProperty(false);
    /** Indicates if a full table rebuild is required. */
    private final BooleanProperty tableRebuildRequired = new SimpleBooleanProperty(false);
    /** List of languages currently visible in the UI. */
    private final ObservableList<String> visibleLanguages = FXCollections.observableArrayList();
    /** List of languages currently filtered in the UI. */
    private final ObservableList<String> filteredLanguages = FXCollections.observableArrayList();
    /** Global filter text for language management. */
    private final StringProperty filterText = new SimpleStringProperty("");
    /** Set of selected translation types. */
    private final EnumSet<PZTranslationType> selectedTypes = EnumSet.noneOf(PZTranslationType.class);
    /** Indicates if selected types have changed. */
    private final BooleanProperty selectedTypesChanged = new SimpleBooleanProperty(false);
    /** Controls visibility of the TypesPanel. */
    private final BooleanProperty typesPanelVisible = new SimpleBooleanProperty(false);
    /** Controls visibility of the LanguagesPanel. */
    private final BooleanProperty languagesPanelVisible = new SimpleBooleanProperty(false);
    /** Set of enabled translation sources. */
    private final ObservableSet<String> enabledSources = FXCollections.observableSet(new HashSet<>());
    /** Set of disabled translation sources. */
    private final ObservableSet<String> disabledSources = FXCollections.observableSet(new HashSet<>());
    /** Indicates if enabled sources have changed. */
    private final BooleanProperty enabledSourcesChanged = new SimpleBooleanProperty(false);
    /** Controls visibility of the SourcesPanel. */
    private final BooleanProperty sourcesPanelVisible = new SimpleBooleanProperty(false);
    /** Current work mode (Discovery, Focus, etc.). */
    private final ObjectProperty<WorkMode> currentWorkMode = new SimpleObjectProperty<>(WorkMode.DISCOVERY);

    /**
     * Private constructor for singleton pattern. Initializes state from
     * configuration and session.
     */
    private State() {
        updateHasChangesFromSession();
        initializeVisibleLanguagesFromConfig();
        initializeSelectedTypesFromConfig();
        initializeSourcesFromConfig();
    }

    /**
     * Returns the singleton instance of State.
     * 
     * @return State instance
     */
    public static State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }

    // Properties accessors
    /**
     * Property for observing changes in session state.
     */
    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    /**
     * Property for observing the selected translation key.
     */
    public StringProperty selectedTranslationKeyProperty() {
        return selectedTranslationKey;
    }

    /**
     * Property for observing right panel visibility.
     */
    public BooleanProperty rightPanelVisibleProperty() {
        return rightPanelVisible;
    }

    /**
     * Property for observing refresh key changes.
     */
    public StringProperty refreshKeyProperty() {
        return refreshKey;
    }

    /**
     * Property for observing save all events.
     */
    public BooleanProperty saveAllTriggeredProperty() {
        return saveAllTriggered;
    }

    /**
     * Property for observing table rebuild requests.
     */
    public BooleanProperty tableRebuildRequiredProperty() {
        return tableRebuildRequired;
    }

    /**
     * Gets the list of currently visible languages.
     */
    public ObservableList<String> getVisibleLanguages() {
        return visibleLanguages;
    }

    /**
     * Gets the list of currently filtered languages.
     */
    public ObservableList<String> getFilteredLanguages() {
        return filteredLanguages;
    }

    /**
     * Property for observing global filter text changes.
     */
    public StringProperty filterTextProperty() {
        return filterText;
    }

    /**
     * Property for observing changes in selected translation types.
     */
    public BooleanProperty selectedTypesChangedProperty() {
        return selectedTypesChanged;
    }

    /**
     * Property for observing TypesPanel visibility changes.
     */
    public BooleanProperty typesPanelVisibleProperty() {
        return typesPanelVisible;
    }

    /**
     * Property for observing LanguagesPanel visibility changes.
     */
    public BooleanProperty languagesPanelVisibleProperty() {
        return languagesPanelVisible;
    }

    /**
     * Property for observing changes in enabled sources.
     */
    public BooleanProperty enabledSourcesChangedProperty() {
        return enabledSourcesChanged;
    }

    /**
     * Property for observing SourcesPanel visibility changes.
     */
    public BooleanProperty sourcesPanelVisibleProperty() {
        return sourcesPanelVisible;
    }

    /**
     * Property for observing work mode changes.
     */
    public ObjectProperty<WorkMode> currentWorkModeProperty() {
        return currentWorkMode;
    }

    /**
     * Gets the set of enabled translation sources.
     */
    public ObservableSet<String> getEnabledSources() {
        return enabledSources;
    }

    /**
     * Gets the set of disabled translation sources.
     */
    public ObservableSet<String> getDisabledSources() {
        return disabledSources;
    }

    /**
     * Gets the set of selected translation types.
     */
    public Set<PZTranslationType> getSelectedTypes() {
        return EnumSet.copyOf(selectedTypes);
    }

    // Convenience methods
    /**
     * Sets the hasChanges property.
     * 
     * @param value true if there are unsaved changes
     */
    public void setHasChanges(boolean value) {
        hasChanges.set(value);
    }

    /**
     * Returns whether there are unsaved changes.
     */
    public boolean getHasChanges() {
        return hasChanges.get();
    }

    /**
     * Sets the selected translation key.
     * 
     * @param key translation key to select
     */
    public void setSelectedTranslationKey(String key) {
        if (!Objects.equals(selectedTranslationKey.get(), key)) {
            selectedTranslationKey.set(key);
        }
    }

    /**
     * Gets the currently selected translation key.
     */
    public String getSelectedTranslationKey() {
        return selectedTranslationKey.get();
    }

    /**
     * Sets the visibility of the right panel.
     * 
     * @param visible true to show, false to hide
     */
    public void setRightPanelVisible(boolean visible) {
        rightPanelVisible.set(visible);
    }

    /**
     * Returns whether the right panel is visible.
     */
    public boolean isRightPanelVisible() {
        return rightPanelVisible.get();
    }

    /**
     * Triggers a refresh for a specific translation key.
     * 
     * @param key translation key to refresh
     */
    public void triggerRefreshForKey(String key) {
        refreshKey.set(key);
    }

    /**
     * Updates the list of visible languages and persists to configuration.
     * Also ensures filteredLanguages only contains visible languages.
     * 
     * @param languages list of language codes to set as visible
     */
    public void updateVisibleLanguages(List<String> languages) {
        var modifiableVisible = FXCollections.observableArrayList(languages);
        visibleLanguages.setAll(modifiableVisible);
        Config.getInstance().setPzLanguages(languages.toArray(new String[0]));
        // Ensure filteredLanguages only contains visible languages
        var filtered = filteredLanguages.stream().filter(modifiableVisible::contains).toList();
        filteredLanguages.setAll(FXCollections.observableArrayList(filtered));
    }

    /**
     * Updates the list of filtered languages, restricting to those present in
     * visibleLanguages.
     * 
     * @param languages list of language codes to filter
     */
    public void updateFilteredLanguages(List<String> languages) {
        var modifiableFiltered = FXCollections
                .observableArrayList(languages.stream().filter(visibleLanguages::contains).toList());
        filteredLanguages.setAll(modifiableFiltered);
    }

    /**
     * Gets the current filter text.
     */
    public String getFilterText() {
        return filterText.get();
    }

    /**
     * Sets the filter text.
     * 
     * @param value filter string
     */
    public void setFilterText(String value) {
        filterText.set(value);
    }

    /**
     * Sets the selected translation types and persists to configuration.
     * Notifies listeners of the change.
     * 
     * @param types set of translation types to select
     */
    public void setSelectedTypes(Set<PZTranslationType> types) {
        selectedTypes.clear();
        selectedTypes.addAll(types);
        String[] typeNames = selectedTypes.stream().map(Enum::name).toArray(String[]::new);
        Config.getInstance().setPzTranslationTypes(typeNames);
        selectedTypesChanged.set(!selectedTypesChanged.get());
    }

    /**
     * Returns whether the TypesPanel is visible.
     */
    public boolean isTypesPanelVisible() {
        return typesPanelVisible.get();
    }

    /**
     * Sets the visibility of the TypesPanel.
     * 
     * @param visible true to show, false to hide
     */
    public void setTypesPanelVisible(boolean visible) {
        typesPanelVisible.set(visible);
    }

    /**
     * Returns whether the LanguagesPanel is visible.
     */
    public boolean isLanguagesPanelVisible() {
        return languagesPanelVisible.get();
    }

    /**
     * Sets the visibility of the LanguagesPanel.
     * 
     * @param visible true to show, false to hide
     */
    public void setLanguagesPanelVisible(boolean visible) {
        languagesPanelVisible.set(visible);
    }

    /**
     * Gets all known translation sources (enabled and disabled).
     */
    public Set<String> getAllKnownSources() {
        Set<String> allSources = new HashSet<>();
        allSources.addAll(enabledSources);
        allSources.addAll(disabledSources);
        return allSources;
    }

    /**
     * Sets the enabled translation sources and updates configuration.
     * Disabled sources are recalculated accordingly.
     * Notifies listeners of the change.
     * 
     * @param sources set of sources to enable
     */
    public void setEnabledSources(Set<String> sources) {
        // Calculate all known sources BEFORE mutating enabledSources
        Set<String> allSources = new HashSet<>(enabledSources);
        allSources.addAll(disabledSources);
        enabledSources.clear();
        enabledSources.addAll(sources);
        disabledSources.clear();
        disabledSources.addAll(allSources);
        disabledSources.removeAll(sources);
        Config.getInstance().setEnabledSources(enabledSources.toArray(new String[0]));
        Config.getInstance().setDisabledSources(disabledSources.toArray(new String[0]));
        enabledSourcesChanged.set(!enabledSourcesChanged.get());
    }

    /**
     * Adds a new translation source if not already known, and enables it.
     * Updates configuration and notifies listeners.
     * 
     * @param sourceName name of the new source
     */
    public void addNewSource(String sourceName) {
        if (!getAllKnownSources().contains(sourceName)) {
            enabledSources.add(sourceName);
            Config.getInstance().setEnabledSources(enabledSources.toArray(new String[0]));
            Config.getInstance().setDisabledSources(disabledSources.toArray(new String[0]));
            enabledSourcesChanged.set(!enabledSourcesChanged.get());
        }
    }

    /**
     * Returns whether the SourcesPanel is visible.
     */
    public boolean isSourcesPanelVisible() {
        return sourcesPanelVisible.get();
    }

    /**
     * Sets the visibility of the SourcesPanel.
     * 
     * @param visible true to show, false to hide
     */
    public void setSourcesPanelVisible(boolean visible) {
        sourcesPanelVisible.set(visible);
    }

    /**
     * Returns the current work mode.
     */
    public WorkMode getCurrentWorkMode() {
        return currentWorkMode.get();
    }

    /**
     * Sets the current work mode.
     * 
     * @param mode the work mode to set
     */
    public void setCurrentWorkMode(WorkMode mode) {
        currentWorkMode.set(mode);
    }

    /**
     * Updates hasChanges property based on current TranslationSession state.
     * True if there are any translation variants present.
     */
    public void updateHasChangesFromSession() {
        TranslationSession session = TranslationSession.getInstance();
        boolean sessionHasChanges = !session.getVariants().isEmpty();
        setHasChanges(sessionHasChanges);
    }

    /**
     * Closes the right panel and clears the selected translation key.
     */
    public void closeRightPanel() {
        setRightPanelVisible(false);
        setSelectedTranslationKey(null);
    }

    /**
     * Triggers save all event for reactive components by toggling the property.
     */
    public void triggerSaveAllEvent() {
        saveAllTriggered.set(!saveAllTriggered.get());
    }

    /**
     * Requests a table refresh for all entries by setting refreshKey to empty
     * string.
     */
    public void requestTableRefresh() {
        refreshKey.set("");
    }

    /**
     * Requests a complete table rebuild (for adding new entries) by toggling the
     * property.
     */
    public void requestTableRebuild() {
        tableRebuildRequired.set(!tableRebuildRequired.get());
    }

    /**
     * Initializes visible languages from configuration.
     * If no languages are configured, defaults to showing only English (EN).
     * English is prioritized first in the list.
     */
    private void initializeVisibleLanguagesFromConfig() {
        try {
            String[] cfgLangs = Config.getInstance().getPzLanguages();
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

    /**
     * Initializes selected translation types from configuration.
     * Only valid types are added to the set.
     */
    private void initializeSelectedTypesFromConfig() {
        String[] cfgTypes = Config.getInstance().getPzTranslationTypes();
        selectedTypes.clear();
        for (String typeName : cfgTypes) {
            PZTranslationType.fromString(typeName).ifPresent(selectedTypes::add);
        }
    }

    /**
     * Initializes enabled and disabled translation sources from configuration.
     */
    private void initializeSourcesFromConfig() {
        enabledSources.clear();
        disabledSources.clear();
        String[] enabled = Config.getInstance().getEnabledSources();
        String[] disabled = Config.getInstance().getDisabledSources();
        if (enabled != null) {
            enabledSources.addAll(Arrays.asList(enabled));
        }
        if (disabled != null) {
            disabledSources.addAll(Arrays.asList(disabled));
        }
    }
}
