package org.pz.polyglot.components;

import java.io.IOException;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

/**
 * UI component for displaying the application version.
 * Reads the version from the resource file and shows it in a label.
 */
public class Version extends HBox {

    /**
     * Label displaying the version string.
     */
    private final Label versionLabel;

    /**
     * Constructs the Version component and initializes the label.
     */
    public Version() {
        setAlignment(Pos.CENTER_RIGHT);
        setSpacing(0);
        setStyle("-fx-padding: 0 8 0 8;");
        versionLabel = new Label();
        versionLabel.setFont(Font.font("System", 12));
        getChildren().add(versionLabel);
        setVersionText();
    }

    /**
     * Loads the version string from the resource file and updates the label.
     * If the file is missing or an error occurs, sets a fallback message.
     */
    private void setVersionText() {
        try (var is = getClass().getResourceAsStream("/version.txt")) {
            if (is != null) {
                var version = new String(is.readAllBytes());
                versionLabel.setText("Version: " + version.strip());
            } else {
                versionLabel.setText("Version: unknown");
            }
        } catch (IOException e) {
            versionLabel.setText("Version: error");
        }
    }
}
