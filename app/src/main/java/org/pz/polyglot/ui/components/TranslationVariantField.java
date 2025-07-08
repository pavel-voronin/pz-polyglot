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
import org.pz.polyglot.pz.translations.PZTranslationManager;
import org.pz.polyglot.ui.models.TranslationVariantViewModel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Component for displaying and editing a single translation variant.
 */
public class TranslationVariantField extends VBox {

    private final TranslationVariantViewModel viewModel;

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

    public TranslationVariantField(TranslationVariantViewModel viewModel) {
        this.viewModel = viewModel;

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
        setupComponent();
    }

    private void setupComponent() {
        LanguageTag langTag = new LanguageTag(viewModel.getLanguage());
        languageTagContainer.getChildren().add(langTag);

        // Setup source label
        sourceLabel.textProperty().bind(viewModel.sourceLabelTextProperty());

        // Setup text area prompt text
        textArea.setPromptText("Enter translation for " + viewModel.getTranslationKey());

        // Bind text area to ViewModel
        textArea.textProperty().bindBidirectional(viewModel.editedTextProperty());

        // Setup visibility based on variant state
        resetLink.visibleProperty().bind(viewModel.changedProperty());
        saveLink.visibleProperty().bind(viewModel.changedProperty());

        // Setup resize handle cursor
        resizeHandle.setCursor(Cursor.SE_RESIZE);
        hitArea.setCursor(Cursor.SE_RESIZE);

        // Setup event handlers
        setupEventHandlers();

        // Setup resize functionality
        setupResizeHandlers();
    }

    private void setupEventHandlers() {
        // Set up reset functionality
        resetLink.setOnAction(e -> {
            viewModel.reset();
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (onVariantChanged != null) {
                onVariantChanged.accept(viewModel.getTranslationKey());
                onVariantChanged.accept(""); // Temporary placeholder
            }
        });

        // Set up save functionality
        saveLink.setOnAction(e -> {
            // Save the variant to file
            PZTranslationManager.saveVariant(viewModel.getVariant());
            viewModel.markSaved();
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (onVariantChanged != null) {
                onVariantChanged.accept(viewModel.getTranslationKey());
                onVariantChanged.accept(""); // Temporary placeholder
            }
        });

        // Listen to ViewModel property changes for callbacks
        viewModel.changedProperty().addListener((obs, oldVal, newVal) -> {
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (onVariantChanged != null) {
                onVariantChanged.accept(viewModel.getTranslationKey());
                onVariantChanged.accept(""); // Temporary placeholder
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
}
