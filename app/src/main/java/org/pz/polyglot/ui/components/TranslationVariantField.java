package org.pz.polyglot.ui.components;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.Cursor;
import org.pz.polyglot.pz.translations.PZTranslationVariant;
import org.pz.polyglot.pz.translations.PZTranslationEntry;
import org.pz.polyglot.pz.translations.PZTranslationManager;
import org.pz.polyglot.pz.languages.PZLanguage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Component for displaying and editing a single translation variant.
 */
public class TranslationVariantField extends VBox {

    private final PZTranslationVariant variant;

    // FXML components
    @FXML
    private VBox root;
    @FXML
    private HBox labelContainer;
    @FXML
    private VBox languageTagContainer;
    @FXML
    private Label sourceLabel;
    @FXML
    private Hyperlink resetLink;
    @FXML
    private Hyperlink saveLink;
    @FXML
    private StackPane textAreaContainer;
    @FXML
    private TextArea textArea;
    @FXML
    private Polygon resizeHandle;
    @FXML
    private javafx.scene.shape.Rectangle hitArea;

    // Track which text areas have been manually resized
    private final Set<TextArea> manuallyResizedTextAreas = new HashSet<>();

    // Callbacks
    private Consumer<String> onVariantChanged;
    private Runnable onStateChanged;

    public TranslationVariantField(PZTranslationVariant variant, PZTranslationEntry entry,
            PZLanguage language, String translationKey, String fieldKey) {
        this.variant = variant;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TranslationVariantField.fxml"));
            loader.setController(this);
            VBox fxmlRoot = loader.load();

            // Load CSS
            try {
                String cssPath = getClass().getResource("/css/translation-variant-field.css").toExternalForm();
                fxmlRoot.getStylesheets().add(cssPath);
                fxmlRoot.getStyleClass().add("translation-variant-field");
            } catch (Exception cssEx) {
                System.err.println("Warning: Could not load CSS file: " + cssEx.getMessage());
            }

            // Copy children from FXML root to this component
            getChildren().addAll(fxmlRoot.getChildren());

            // Copy properties from FXML root
            setSpacing(fxmlRoot.getSpacing());

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load FXML for TranslationVariantField", e);
        }

        // Setup the component
        setupComponent(variant, entry, language, translationKey, fieldKey);
    }

    private void setupComponent(PZTranslationVariant variant, PZTranslationEntry entry,
            PZLanguage language, String translationKey, String fieldKey) {

        // Create and add language tag
        LanguageTag langTag = new LanguageTag(language);
        languageTagContainer.getChildren().add(langTag);

        // Setup source label
        String detectedCharsetName = variant.getUsedCharset() != null
                ? variant.getUsedCharset().name()
                : "Unknown";
        String supposedCharsetName = variant.getSupposedCharset() != null
                ? variant.getSupposedCharset().name()
                : "Unknown";

        String sourceName = variant.getFile().getSource().getName();
        sourceLabel.setText("(" + sourceName + ", " + detectedCharsetName
                + (detectedCharsetName.equals(supposedCharsetName) ? "" : " *") + ")");

        // Setup text area
        textArea.setPromptText("Enter translation for " + fieldKey);
        textArea.setText(variant.getEditedText());

        // Setup visibility based on variant state
        resetLink.setVisible(variant.isChanged());
        saveLink.setVisible(variant.isChanged());

        // Setup resize handle cursor
        resizeHandle.setCursor(Cursor.SE_RESIZE);
        hitArea.setCursor(Cursor.SE_RESIZE);

        // Setup event handlers
        setupEventHandlers(translationKey);

        // Setup resize functionality
        setupResizeHandlers();
    }

    private void setupEventHandlers(String translationKey) {
        // Set up reset functionality
        resetLink.setOnAction(e -> {
            variant.reset();
            textArea.setText(variant.getEditedText());
            resetLink.setVisible(false);
            saveLink.setVisible(false);
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (onVariantChanged != null) {
                onVariantChanged.accept(translationKey);
            }
        });

        // Set up save functionality
        saveLink.setOnAction(e -> {
            // Get the current text from the text area
            String currentText = textArea.getText();
            // Update the variant with the current text
            variant.setEditedText(currentText);
            // Save the variant to file
            PZTranslationManager.saveVariant(variant);
            // Hide the save link after successful save
            saveLink.setVisible(false);
            // Reset button should still be visible if text differs from original
            resetLink.setVisible(false);
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (onVariantChanged != null) {
                onVariantChanged.accept(translationKey);
            }
        });

        // Track text changes and show/hide reset and save buttons
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.equals(variant.getOriginalText())) {
                variant.setEditedText(newText);
                resetLink.setVisible(true);
                saveLink.setVisible(true);
            } else if (newText != null && newText.equals(variant.getOriginalText())) {
                variant.reset();
                resetLink.setVisible(false);
                saveLink.setVisible(false);
            }
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (onVariantChanged != null) {
                onVariantChanged.accept(translationKey);
            }
        });

        // Smart auto-resize using pixel-based calculation
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            // Only auto-resize if not manually resized
            if (!manuallyResizedTextAreas.contains(textArea)) {
                Platform.runLater(() -> {
                    if (newText == null || newText.isEmpty()) {
                        textArea.setPrefHeight(28);
                        textArea.setMaxHeight(28);
                    } else {
                        // Calculate height based on text content
                        int lineBreaks = newText.split("\n", -1).length;

                        // Estimate wrapped lines based on character count and width
                        double charWidth = 7.5;
                        double availableWidth = 410;
                        int charsPerLine = (int) (availableWidth / charWidth);

                        int wrappedLines = 0;
                        String[] textLines = newText.split("\n", -1);
                        for (String line : textLines) {
                            if (line.length() > charsPerLine) {
                                wrappedLines += (line.length() / charsPerLine);
                            }
                        }

                        int totalLines = Math.max(1, lineBreaks + wrappedLines);
                        int newHeight = Math.max(24, totalLines * 17 + 10);
                        textArea.setPrefHeight(newHeight);
                        textArea.setMaxHeight(newHeight);
                    }
                });
            }
        });
    }

    private void setupResizeHandlers() {
        final double[] dragAnchor = new double[2];

        hitArea.setOnMousePressed(event -> {
            dragAnchor[0] = event.getSceneX();
            dragAnchor[1] = event.getSceneY();
            event.consume();
        });

        hitArea.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - dragAnchor[0];
            double deltaY = event.getSceneY() - dragAnchor[1];

            double newWidth = Math.max(textArea.getMinWidth(), textArea.getPrefWidth() + deltaX);
            double newHeight = Math.max(textArea.getMinHeight(), textArea.getPrefHeight() + deltaY);

            // Only resize the TextArea, let container adjust automatically
            textArea.setPrefWidth(newWidth);
            textArea.setPrefHeight(newHeight);
            textArea.setMaxHeight(newHeight);

            // Mark this text area as manually resized
            manuallyResizedTextAreas.add(textArea);

            dragAnchor[0] = event.getSceneX();
            dragAnchor[1] = event.getSceneY();
            event.consume();
        });
    }

    /**
     * Updates the state of the variant buttons based on current changes.
     */
    public void updateVariantButtons() {
        Platform.runLater(() -> {
            String currentText = textArea.getText();
            textArea.setText(currentText + " ");
            textArea.setText(currentText);
        });
    }

    /**
     * Sets the callback for when variant changes occur.
     */
    public void setOnVariantChanged(Consumer<String> callback) {
        this.onVariantChanged = callback;
    }

    /**
     * Sets the callback for when the state changes.
     */
    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    /**
     * Gets the text area for this variant field.
     */
    public TextArea getTextArea() {
        return textArea;
    }

    /**
     * Gets the variant associated with this field.
     */
    public PZTranslationVariant getVariant() {
        return variant;
    }
}
