package org.pz.polyglot.components;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

import java.io.IOException;

public class Version extends HBox {
    private final Label versionLabel;

    public Version() {
        setAlignment(Pos.CENTER_RIGHT);
        setSpacing(0);
        setStyle("-fx-padding: 0 8 0 8;");
        versionLabel = new Label();
        versionLabel.setFont(Font.font("System", 12));
        getChildren().add(versionLabel);
        setVersionText();
    }

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
