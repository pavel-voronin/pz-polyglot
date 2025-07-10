package org.pz.polyglot.viewModels;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.translations.PZTranslationVariant;

import javafx.beans.property.*;

public class TranslationVariantViewModel {
    private final PZTranslationVariant variant;

    private final StringProperty originalText = new SimpleStringProperty();
    private final StringProperty editedText = new SimpleStringProperty();
    private final BooleanProperty changed = new SimpleBooleanProperty();

    public TranslationVariantViewModel(PZTranslationVariant variant) {
        this.variant = variant;

        originalText.set(variant.getOriginalText());
        editedText.set(variant.getEditedText());
        changed.set(variant.isChanged());

        originalText.addListener((obs, oldVal, newVal) -> variant.setOriginalText(newVal));
        editedText.addListener((obs, oldVal, newVal) -> {
            variant.setEditedText(newVal);
            changed.set(variant.isChanged());
        });
    }

    public StringProperty originalTextProperty() {
        return originalText;
    }

    public StringProperty editedTextProperty() {
        return editedText;
    }

    public BooleanProperty changedProperty() {
        return changed;
    }

    public void reset() {
        variant.reset();
        refresh();
    }

    public void markSaved() {
        variant.markSaved();
        refresh();
    }

    public void refresh() {
        originalText.set(variant.getOriginalText());
        editedText.set(variant.getEditedText());
        changed.set(variant.isChanged());
    }

    public PZTranslationVariant getVariant() {
        return variant;
    }

    public PZLanguage getLanguage() {
        return variant.getLanguage();
    }

    public String getTranslationKey() {
        return variant.getKey().getKey();
    }

    public String getSource() {
        return variant.getSource().getName();
    }

    public boolean isSourceEditable() {
        return variant.getSource().isEditable();
    }
}
