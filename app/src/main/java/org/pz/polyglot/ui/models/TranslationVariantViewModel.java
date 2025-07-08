package org.pz.polyglot.ui.models;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.translations.PZTranslationVariant;

import javafx.beans.property.*;

public class TranslationVariantViewModel {
    private final PZTranslationVariant variant;

    private final StringProperty originalText = new SimpleStringProperty();
    private final StringProperty editedText = new SimpleStringProperty();
    private final BooleanProperty changed = new SimpleBooleanProperty();
    private final StringProperty sourceLabelText = new SimpleStringProperty();

    public TranslationVariantViewModel(PZTranslationVariant variant) {
        this.variant = variant;

        originalText.set(variant.getOriginalText());
        editedText.set(variant.getEditedText());
        changed.set(variant.isChanged());

        // TODO: remove in favor of using grouping and separate components
        {
            String sourceName = variant.getFile().getSource().getName();
            String detectedCharsetName = variant.getUsedCharset().name();
            Boolean charsetChanged = !detectedCharsetName.equals(variant.getSupposedCharset().name());

            sourceLabelText.set("(" + sourceName + ", " + detectedCharsetName + (charsetChanged ? " *" : "") + ")");
        }

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

    public StringProperty sourceLabelTextProperty() {
        return sourceLabelText;
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
        return variant.getFile().getLanguage();
    }

    public String getTranslationKey() {
        return variant.getKey().getKey();
    }
}
