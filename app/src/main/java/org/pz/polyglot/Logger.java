package org.pz.polyglot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class Logger {
    public enum Level {
        ERROR, WARNING, INFO, DEBUG
    }

    private static Set<Level> enabledLevels = Set.of(Level.ERROR, Level.WARNING, Level.INFO);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static String lastMessage = null;
    private static Level lastLevel = null;
    private static int repeatCount = 1;
    private static String originalLine = null;

    public static void enable(Level... levels) {
        enabledLevels = Set.of(levels);
    }

    public static void enableAll() {
        enabledLevels = Set.of(Level.ERROR, Level.WARNING, Level.INFO, Level.DEBUG);
    }

    public static void enableProductionMode() {
        enabledLevels = Set.of(Level.ERROR, Level.WARNING);
    }

    private static void log(Level level, String message, Throwable throwable) {
        if (!enabledLevels.contains(level)) {
            return;
        }

        String timestamp = LocalTime.now().format(TIME_FORMAT);
        String currentLine = timestamp + " [" + level + "] " + message;

        if (throwable != null) {
            finishPreviousGroup();
            System.out.println(currentLine);
            throwable.printStackTrace();
            resetTracking();
            return;
        }

        if (lastMessage != null && lastMessage.equals(message) && lastLevel == level) {
            repeatCount++;
            System.out.print("\r" + originalLine + " x" + repeatCount);
            System.out.flush();
        } else {
            finishPreviousGroup();

            System.out.print(currentLine);
            System.out.flush();

            lastMessage = message;
            lastLevel = level;
            repeatCount = 1;
            originalLine = currentLine;
        }
    }

    private static void finishPreviousGroup() {
        if (lastMessage != null) {
            System.out.println();
        }
    }

    private static void resetTracking() {
        lastMessage = null;
        lastLevel = null;
        repeatCount = 1;
        originalLine = null;
    }

    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    public static void warning(String message) {
        log(Level.WARNING, message, null);
    }

    public static void warning(String message, Throwable throwable) {
        log(Level.WARNING, message, throwable);
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    public static void debug(String message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }

    public static void flush() {
        finishPreviousGroup();
        resetTracking();
    }
}
