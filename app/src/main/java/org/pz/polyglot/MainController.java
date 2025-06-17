package org.pz.polyglot;

import javafx.fxml.FXML;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Main controller for the Polyglot application.
 * Handles initialization and configuration of the main TreeTableView.
 */
public class MainController {
    /**
     * Model class representing a row in the tree table.
     */
    public static class ExampleRow {
        private final String value;

        public ExampleRow(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @FXML
    private TreeTableColumn<ExampleRow, String> keyColumn;
    @FXML
    private TreeTableColumn<ExampleRow, String> enColumn;
    @FXML
    private TreeTableView<ExampleRow> treeTableView;

    /**
     * Represents an icon and its associated action for use in a tree table cell.
     */
    public static class IconAction {
        private final String imagePath;
        private final String alertMessage;
        private final boolean clickable;

        public IconAction(String imagePath, String alertMessage, boolean clickable) {
            this.imagePath = imagePath;
            this.alertMessage = alertMessage;
            this.clickable = clickable;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getAlertMessage() {
            return alertMessage;
        }

        public boolean isClickable() {
            return clickable;
        }
    }

    /**
     * Initializes the TreeTableView and its columns with example data and custom
     * cell rendering.
     */
    @FXML
    private void initialize() {
        if (treeTableView != null) {
            // Set up columns
            if (keyColumn != null) {
                keyColumn
                        .setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getValue()));
                keyColumn.setCellFactory(col -> createIconTreeTableCell(List.of(
                        new IconAction("/fxml/icon.png", "First icon clicked!", true),
                        new IconAction("/fxml/icon.png", "Second icon clicked!", false))));
            }
            if (enColumn != null) {
                enColumn.setCellValueFactory(
                        param -> new SimpleStringProperty("EN: " + param.getValue().getValue().getValue()));
            }
            // Add 40 example rows
            TreeItem<ExampleRow> root = new TreeItem<>(new ExampleRow("Root"));
            root.setExpanded(true);
            IntStream.rangeClosed(1, 40).forEach(i -> {
                root.getChildren().add(new TreeItem<>(new ExampleRow("Item " + i)));
            });
            treeTableView.setRoot(root);
            treeTableView.setShowRoot(false);
        }
    }

    /**
     * Creates a TreeTableCell that displays a row's value and a set of icons, some
     * clickable, some not.
     * 
     * @param icons List of IconAction objects to display in the cell.
     * @return TreeTableCell for use in a TreeTableColumn.
     */
    private TreeTableCell<ExampleRow, String> createIconTreeTableCell(List<IconAction> icons) {
        return new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    HBox hbox = new HBox(5);
                    for (IconAction iconAction : icons) {
                        Image image = new Image(getClass().getResourceAsStream(iconAction.getImagePath()));
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(16);
                        imageView.setFitHeight(16);
                        imageView.setPreserveRatio(true);
                        if (iconAction.isClickable()) {
                            imageView.setCursor(Cursor.HAND);
                            imageView.setOnMouseClicked(event -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Icon Clicked");
                                alert.setHeaderText(null);
                                alert.setContentText(iconAction.getAlertMessage());
                                alert.showAndWait();
                            });
                            imageView.setOnMouseEntered(e -> imageView.setOpacity(0.7));
                            imageView.setOnMouseExited(e -> imageView.setOpacity(1.0));
                        } else {
                            imageView.setCursor(Cursor.DEFAULT);
                            imageView.setOnMouseClicked(null);
                            imageView.setOnMouseEntered(null);
                            imageView.setOnMouseExited(null);
                        }
                        hbox.getChildren().add(imageView);
                    }
                    setGraphic(hbox);
                }
            }
        };
    }
}
