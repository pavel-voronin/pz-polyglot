package org.pz.polyglot.models.translations;

import java.util.Optional;

/**
 * Represents the various translation types used in Project Zomboid localization
 * files.
 * Each enum constant corresponds to a specific domain or category of
 * translation data.
 */
public enum PZTranslationType {
    Attributes,
    BodyParts,
    Challenge,
    ContextMenu,
    DynamicRadio,
    Entity,
    EvolvedRecipeName,
    Farming,
    Fluids,
    GameSound,
    IG_UI,
    ItemName,
    Items,
    MakeUp,
    Moodles,
    Moveables,
    MultiStageBuild,
    Print_Media,
    Print_Text,
    RadioData,
    Recipes,
    Recorded_Media,
    Sandbox,
    Stash,
    SurvivalGuide,
    SurvivorNames,
    Tooltip,
    UI;

    /**
     * Attempts to resolve a {@link PZTranslationType} from the given string.
     * <p>
     * The comparison is case-sensitive and matches the enum constant name exactly.
     *
     * @param name the name of the enum constant to resolve
     * @return an {@link Optional} containing the matching
     *         {@code PZTranslationType}, or empty if not found
     */
    public static Optional<PZTranslationType> fromString(String name) {
        for (PZTranslationType type : values()) {
            if (type.name().equals(name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}