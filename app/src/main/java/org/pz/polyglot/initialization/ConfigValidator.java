package org.pz.polyglot.initialization;

import org.pz.polyglot.Config;
import org.pz.polyglot.Logger;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.models.translations.PZTranslationType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates and synchronizes configuration with domain models.
 * Ensures config contains all necessary data with proper defaults.
 */
public class ConfigValidator {
    private final Config config;

    public ConfigValidator(Config config) {
        this.config = config;
    }

    /**
     * Validates and updates sources in configuration.
     * Ensures all discovered sources are properly categorized as enabled/disabled.
     */
    public void validateAndUpdateSources() {
        Logger.info("Validating sources configuration");

        try {
            Set<String> discoveredSources = PZSources.getInstance().getSources().stream()
                    .map(source -> source.getName())
                    .collect(java.util.stream.Collectors.toSet());
            Set<String> enabledSources = new HashSet<>(Arrays.asList(config.getEnabledSources()));
            Set<String> disabledSources = new HashSet<>(Arrays.asList(config.getDisabledSources()));

            // Find sources that are not in either enabled or disabled
            Set<String> newSources = new HashSet<>(discoveredSources);
            newSources.removeAll(enabledSources);
            newSources.removeAll(disabledSources);

            if (!newSources.isEmpty()) {
                Logger.info("Found " + newSources.size() + " new sources, adding to enabled: " + newSources);
                enabledSources.addAll(newSources);
                config.setEnabledSources(enabledSources.toArray(new String[0]));
            }

            // Remove sources that no longer exist
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
     * Validates and updates languages in configuration.
     * Ensures all supported languages are available if config is empty.
     */
    public void validateAndUpdateLanguages() {
        Logger.info("Validating languages configuration");

        try {
            String[] configLanguages = config.getPzLanguages();

            // If no languages configured, set all available languages with EN first
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
     * Validates and updates translation types in configuration.
     * Ensures all supported translation types are available if config is empty.
     */
    public void validateAndUpdateTranslationTypes() {
        Logger.info("Validating translation types configuration");

        try {
            String[] configTypes = config.getPzTranslationTypes();

            // If no types configured, set all available types
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
