package org.pz.polyglot.ui.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SystemMonitor extends HBox {

    private static final List<Supplier<String>> hooks = new ArrayList<>();

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

        StringBuilder displayText = new StringBuilder();
        displayText.append(String.format("Memory: %d MB", usedMemoryMB));

        // Execute all hooks and append their results
        for (Supplier<String> hook : hooks) {
            try {
                String hookResult = hook.get();
                if (hookResult != null && !hookResult.trim().isEmpty()) {
                    displayText.append(" | ").append(hookResult.trim());
                }
            } catch (Exception e) {
                // Silently ignore hook execution errors to prevent system monitor from failing
            }
        }

        memoryLabel.setText(displayText.toString());
    }

    /**
     * Adds a hook that will be executed during each memory update cycle.
     * The hook should return a string that will be appended to the system monitor
     * display.
     * 
     * @param hook a supplier that returns text to display, or null/empty string if
     *             nothing to display
     */
    public static void addHook(Supplier<String> hook) {
        if (hook != null) {
            hooks.add(hook);
        }
    }
}
