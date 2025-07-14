package org.pz.polyglot.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import org.pz.polyglot.Logger;

/**
 * Displays system memory usage and allows extension via hooks for additional
 * system information.
 * Updates every second and allows manual garbage collection via label click.
 */
public class SystemMonitor extends HBox {

    /**
     * List of hooks to provide additional system information for display.
     * Each hook is a supplier returning a string to be appended to the monitor
     * output.
     */
    private static final List<Supplier<String>> hooks = new ArrayList<>();

    /**
     * Label displaying current memory usage and hook outputs.
     */
    private final Label memoryLabel = new Label("Memory: -- MB");
    /**
     * Timeline for periodic memory usage updates.
     */
    private Timeline memoryUpdateTimeline;

    /**
     * Constructs a SystemMonitor and initializes UI and periodic memory updates.
     * Adds stylesheet, sets up label, and enables manual garbage collection on
     * click.
     */
    public SystemMonitor() {
        this.getStylesheets().add(getClass().getResource("/css/system-monitor.css").toExternalForm());
        getStyleClass().add("system-monitor");
        memoryLabel.getStyleClass().add("memory-label");
        getChildren().addAll(memoryLabel);
        memoryLabel.setOnMouseClicked(event -> {
            System.gc();
            Logger.info("Manual garbage collection triggered");
        });
        initializeMemoryMonitor();
    }

    /**
     * Initializes and starts the periodic memory usage update timeline.
     */
    private void initializeMemoryMonitor() {
        memoryUpdateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateMemoryUsage()));
        memoryUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        memoryUpdateTimeline.play();
        updateMemoryUsage();
    }

    /**
     * Updates the memory usage label and appends results from all registered hooks.
     * Silently ignores hook errors to ensure monitor stability.
     */
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long usedMemoryMB = usedMemory / (1024 * 1024);

        StringBuilder displayText = new StringBuilder();
        displayText.append(String.format("Memory: %d MB", usedMemoryMB));

        // Append results from all hooks
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
     * Registers a hook to be executed during each memory update cycle.
     * The hook should return a string to be appended to the system monitor display.
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
