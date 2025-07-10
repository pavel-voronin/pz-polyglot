package org.pz.polyglot.models.translations;

import java.util.Optional;

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
     * Returns an Optional containing the enum constant of this type with the
     * specified name,
     * or an empty Optional if no such constant exists.
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