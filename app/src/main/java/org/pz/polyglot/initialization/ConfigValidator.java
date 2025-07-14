package org.pz.polyglot.initialization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pz.polyglot.Config;
import org.pz.polyglot.Logger;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.models.translations.PZTranslationType;

/**
 * Responsible for validating and synchronizing configuration with domain
 * models.
 * Ensures the configuration contains all required data and applies proper
 * defaults when necessary.
 */
public class ConfigValidator {
    /**
     * The configuration instance to validate and update.
     */
    private final Config config;

    /**
     * Constructs a ConfigValidator for the provided configuration.
     * 
     * @param config the configuration to validate and update
     */
    public ConfigValidator(Config config) {
        this.config = config;
    }

    /**
     * Validates and updates the sources in the configuration.
     * <p>
     * Ensures all discovered sources are categorized as enabled or disabled. Adds
     * new sources to enabled,
     * and removes obsolete sources from both enabled and disabled lists.
     */
    public void validateAndUpdateSources() {
        Logger.info("Validating sources configuration");

        try {
            // Discover all sources from the domain model
            Set<String> discoveredSources = PZSources.getInstance().getSources().stream()
                    .map(source -> source.getName())
                    .collect(java.util.stream.Collectors.toSet());
            Set<String> enabledSources = new HashSet<>(Arrays.asList(config.getEnabledSources()));
            Set<String> disabledSources = new HashSet<>(Arrays.asList(config.getDisabledSources()));

            // Identify sources not present in either enabled or disabled lists
            Set<String> newSources = new HashSet<>(discoveredSources);
            newSources.removeAll(enabledSources);
            newSources.removeAll(disabledSources);

            if (!newSources.isEmpty()) {
                Logger.info("Found " + newSources.size() + " new sources, adding to enabled: " + newSources);
                enabledSources.addAll(newSources);
                config.setEnabledSources(enabledSources.toArray(new String[0]));
            }

            // Remove sources that no longer exist in the domain model
            Set<String> obsoleteEnabled = new HashSet<>(enabledSources);
            obsoleteEnabled.removeAll(discoveredSources);

            Set<String> obsoleteDisabled = new HashSet<>(disabledSources);
            obsoleteDisabled.removeAll(discoveredSources);

            if (!obsoleteEnabled.isEmpty() || !obsoleteDisabled.isEmpty()) {
                Logger.info(
                        "Removing obsolete sources. Enabled: " + obsoleteEnabled + ", Disabled: " + obsoleteDisabled);

                enabledSources.removeAll(obsoleteEnabled);
                disabledSources.removeAll(obsoleteDisabled);

                config.setEnabledSources(enabledSources.toArray(new String[0]));
                config.setDisabledSources(disabledSources.toArray(new String[0]));
            }

        } catch (Exception e) {
            Logger.error("Failed to validate sources: " + e.getMessage());
        }
    }

    /**
     * Validates and updates the languages in the configuration.
     * <p>
     * If no languages are configured, sets all available languages with English
     * (EN) as the first entry.
     */
    public void validateAndUpdateLanguages() {
        Logger.info("Validating languages configuration");

        try {
            String[] configLanguages = config.getPzLanguages();

            // If no languages are configured, set all available languages with EN first
            if (configLanguages == null || configLanguages.length == 0) {
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

                config.setPzLanguages(sortedCodes.toArray(new String[0]));
                Logger.info("Set default languages configuration with " + sortedCodes.size() + " languages");
            }

        } catch (Exception e) {
            Logger.error("Failed to validate languages: " + e.getMessage());
        }
    }

    /**
     * Validates and updates the translation types in the configuration.
     * <p>
     * If no translation types are configured, sets all available types from the
     * domain model.
     */
    public void validateAndUpdateTranslationTypes() {
        Logger.info("Validating translation types configuration");

        try {
            String[] configTypes = config.getPzTranslationTypes();

            // If no types are configured, set all available types
            if (configTypes == null || configTypes.length == 0) {
                String[] allTypes = Arrays.stream(PZTranslationType.values())
                        .map(Enum::name)
                        .toArray(String[]::new);

                config.setPzTranslationTypes(allTypes);
                Logger.info("Set default translation types configuration with " + allTypes.length + " types");
            }

        } catch (Exception e) {
            Logger.error("Failed to validate translation types: " + e.getMessage());
        }
    }
}
