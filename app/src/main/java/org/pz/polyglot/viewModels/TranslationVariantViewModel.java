package org.pz.polyglot.viewModels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.translations.PZTranslationVariant;

/**
 * ViewModel for a translation variant, providing properties for UI binding and
 * domain logic.
 */
public class TranslationVariantViewModel {
    private final PZTranslationVariant variant;

    /**
     * Property representing the original text of the translation variant.
     */
    private final StringProperty originalText = new SimpleStringProperty();
    /**
     * Property representing the edited text of the translation variant.
     */
    private final StringProperty editedText = new SimpleStringProperty();
    /**
     * Property indicating whether the translation variant has been changed.
     */
    private final BooleanProperty changed = new SimpleBooleanProperty();

    /**
     * Constructs a ViewModel for the given translation variant.
     * 
     * @param variant the translation variant to wrap
     */
    public TranslationVariantViewModel(PZTranslationVariant variant) {
        this.variant = variant;

        originalText.set(variant.getOriginalText());
        editedText.set(variant.getEditedText());
        changed.set(variant.isChanged());

        // Update domain model when properties change
        originalText.addListener((obs, oldVal, newVal) -> variant.setOriginalText(newVal));
        editedText.addListener((obs, oldVal, newVal) -> {
            variant.setEditedText(newVal);
            changed.set(variant.isChanged());
        });
    }

    /**
     * Returns the property for the original text.
     * 
     * @return original text property
     */
    public StringProperty originalTextProperty() {
        return originalText;
    }

    /**
     * Returns the property for the edited text.
     * 
     * @return edited text property
     */
    public StringProperty editedTextProperty() {
        return editedText;
    }

    /**
     * Returns the property indicating if the variant has changed.
     * 
     * @return changed property
     */
    public BooleanProperty changedProperty() {
        return changed;
    }

    /**
     * Resets the translation variant to its original state.
     */
    public void reset() {
        variant.reset();
        refresh();
    }

    /**
     * Marks the translation variant as saved.
     */
    public void markSaved() {
        variant.markSaved();
        refresh();
    }

    /**
     * Refreshes the properties from the underlying domain model.
     */
    public void refresh() {
        originalText.set(variant.getOriginalText());
        editedText.set(variant.getEditedText());
        changed.set(variant.isChanged());
    }

    /**
     * Returns the underlying translation variant.
     * 
     * @return the translation variant
     */
    public PZTranslationVariant getVariant() {
        return variant;
    }

    /**
     * Returns the language of the translation variant.
     * 
     * @return the language
     */
    public PZLanguage getLanguage() {
        return variant.getLanguage();
    }

    /**
     * Returns the translation key for this variant.
     * 
     * @return translation key
     */
    public String getTranslationKey() {
        return variant.getKey().getKey();
    }

    /**
     * Returns the name of the source for this variant.
     * 
     * @return source name
     */
    public String getSource() {
        return variant.getSource().getName();
    }

    /**
     * Indicates whether the source of this variant is editable.
     * 
     * @return true if editable, false otherwise
     */
    public boolean isSourceEditable() {
        return variant.getSource().isEditable();
    }
}
