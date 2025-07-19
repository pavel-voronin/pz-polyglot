package org.pz.polyglot.components;

import java.io.IOException;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.Cursor;

import org.pz.polyglot.State;
import org.pz.polyglot.models.TranslationSession;
import org.pz.polyglot.models.translations.PZTranslationManager;
import org.pz.polyglot.viewModels.TranslationVariantViewModel;
import org.pz.polyglot.viewModels.registries.TranslationEntryViewModelRegistry;

/**
 * JavaFX component for displaying and editing a single translation variant.
 * Handles UI logic, bindings, and user interactions for a translation variant.
 */
public class TranslationVariantField extends VBox {

    /**
     * ViewModel representing the translation variant data and state.
     */
    private final TranslationVariantViewModel viewModel;

    // FXML-injected UI components
    /** Container for the label elements. */
    @FXML
    private HBox labelContainer;
    /** Container for language and type tags. */
    @FXML
    private HBox tagsContainer;
    /** Hyperlink for resetting the variant to its original value. */
    @FXML
    private Hyperlink resetLink;
    /** Hyperlink for saving the variant. */
    @FXML
    private Hyperlink saveLink;
    /** Hyperlink for deleting the variant. */
    @FXML
    private Hyperlink deleteLink;
    /** Container for the text area. */
    @FXML
    private StackPane textAreaContainer;
    /** Text area for editing the translation variant. */
    @FXML
    private TextArea textArea;
    /** Polygon used as the resize handle for the text area. */
    @FXML
    private Polygon resizeHandle;
    /** Rectangle area for mouse hit detection for resizing. */
    @FXML
    private Rectangle hitArea;

    /** Indicates if the text area has been manually resized by the user. */
    private boolean manuallyResized = false;

    /** Callback invoked when the variant changes. */
    private Consumer<String> onVariantChanged;
    /** Callback invoked when the state changes. */
    private Runnable onStateChanged;

    /**
     * Constructs a TranslationVariantField for the given ViewModel.
     * Loads FXML, applies styles, and sets up component logic.
     *
     * @param viewModel the ViewModel for this translation variant
     */
    public TranslationVariantField(TranslationVariantViewModel viewModel) {
        this.viewModel = viewModel;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TranslationVariantField.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();

            getStylesheets().add(getClass().getResource("/css/translation-variant-field.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load FXML for TranslationVariantField", e);
        }

        // Setup the component
        setupComponent();
    }

    /**
     * Initializes and configures the UI components, bindings, and event handlers.
     */
    private void setupComponent() {
        LanguageTag langTag = new LanguageTag(viewModel.getLanguage());
        TypeTag typeTag = new TypeTag(viewModel.getVariant().getType());
        tagsContainer.getChildren().addAll(langTag, typeTag);

        // Setup text area prompt text
        textArea.setPromptText("Enter translation for " + viewModel.getTranslationKey());

        // Bind text area to ViewModel
        textArea.textProperty().bindBidirectional(viewModel.editedTextProperty());

        // Set editable state based on source editability
        boolean isEditable = viewModel.isSourceEditable();
        textArea.setEditable(isEditable);

        // Add CSS class for non-editable fields
        if (!isEditable) {
            textArea.getStyleClass().add("locked");
        }

        // Setup visibility based on variant state and editability
        resetLink.visibleProperty().bind(viewModel.changedProperty());
        saveLink.visibleProperty().bind(viewModel.changedProperty());

        // Set deleteLink visibility based on source editability
        deleteLink.setVisible(isEditable);

        // Setup resize handle cursor
        resizeHandle.setCursor(Cursor.SE_RESIZE);
        hitArea.setCursor(Cursor.SE_RESIZE);

        // Setup event handlers
        setupEventHandlers();

        // Setup resize functionality
        setupResizeHandlers();
    }

    /**
     * Configures event handlers for UI actions and property changes.
     */
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

        deleteLink.setOnAction(e -> {
            if (!viewModel.isSourceEditable()) {
                return; // Do not allow deletion if not editable
            }

            var variant = viewModel.getVariant();
            var entry = variant.getKey();

            // First, remove the variant from the entry's variant list
            entry.getVariants().remove(variant);

            // Then remove from session and registry
            TranslationEntryViewModelRegistry.removeViewModel(entry);
            TranslationSession.getInstance().removeVariant(variant);
            PZTranslationManager.deleteVariant(variant);

            // Refresh the parent entry ViewModel
            var entryViewModel = org.pz.polyglot.viewModels.registries.TranslationEntryViewModelRegistry
                    .getViewModel(entry);
            if (entryViewModel != null) {
                entryViewModel.refresh();
            }

            // Remove this panel from its parent (reload UI)
            Platform.runLater(() -> {
                State.getInstance().requestTableRefresh();
                State.getInstance().setSelectedTranslationKey(null);
                Platform.runLater(() -> {
                    State.getInstance().setSelectedTranslationKey(viewModel.getTranslationKey());
                });
            });
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
            resizeTextArea(newText);
        });

        // Trigger initial resize after binding
        resizeTextArea(textArea.getText());
    }

    /**
     * Dynamically resizes the text area based on its content, unless manually
     * resized.
     * Uses a pixel-based estimation for line wrapping and height calculation.
     *
     * @param text the current text in the text area
     */
    private void resizeTextArea(String text) {
        // Prevent auto-resize if user has manually resized the text area
        if (manuallyResized) {
            return;
        }

        Platform.runLater(() -> {
            if (text == null || text.isEmpty()) {
                textArea.setPrefHeight(28);
                textArea.setMaxHeight(28);
            } else {
                // Estimate height based on line breaks and wrapped lines
                int lineBreaks = text.split("\n", -1).length;
                double charWidth = 7.5;
                double availableWidth = 410;
                int charsPerLine = (int) (availableWidth / charWidth);

                int wrappedLines = 0;
                String[] textLines = text.split("\n", -1);
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

    /**
     * Sets up mouse event handlers for manual resizing of the text area.
     * Allows the user to drag the resize handle to adjust the text area size.
     */
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
            manuallyResized = true;

            dragAnchor[0] = event.getSceneX();
            dragAnchor[1] = event.getSceneY();
            event.consume();
        });
    }

    /**
     * Forces a UI update for the variant buttons by triggering a text area change
     * event.
     * This is a workaround to refresh button states when needed.
     */
    public void updateVariantButtons() {
        Platform.runLater(() -> {
            String currentText = textArea.getText();
            textArea.setText(currentText + " ");
            textArea.setText(currentText);
        });
    }

    /**
     * Sets the callback to be invoked when the variant changes.
     *
     * @param callback the callback accepting the translation key
     */
    public void setOnVariantChanged(Consumer<String> callback) {
        this.onVariantChanged = callback;
    }

    /**
     * Sets the callback to be invoked when the state changes.
     *
     * @param callback the callback to run on state change
     */
    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }
}
