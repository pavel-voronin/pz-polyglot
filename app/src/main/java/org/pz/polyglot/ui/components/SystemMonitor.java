package org.pz.polyglot.ui.components;

import org.pz.polyglot.ui.models.registries.TranslationVariantViewModelRegistry;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class SystemMonitor extends HBox {

    private final Label memoryLabel = new Label("Memory: -- MB");
    private Timeline memoryUpdateTimeline;

    public SystemMonitor() {
        this.getStylesheets().add(getClass().getResource("/css/system-monitor.css").toExternalForm());

        getStyleClass().add("system-monitor");

        memoryLabel.getStyleClass().add("memory-label");
        getChildren().addAll(memoryLabel);

        initializeMemoryMonitor();
    }

    private void initializeMemoryMonitor() {
        memoryUpdateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateMemoryUsage()));
        memoryUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        memoryUpdateTimeline.play();
        updateMemoryUsage();
    }

    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long usedMemoryMB = usedMemory / (1024 * 1024);

        memoryLabel.setText(
                String.format("Memory: %d MB (%d)", usedMemoryMB, TranslationVariantViewModelRegistry.getCacheSize()));
    }
}
