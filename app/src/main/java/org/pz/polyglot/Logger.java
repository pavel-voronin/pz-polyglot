package org.pz.polyglot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Logger utility for application-wide logging with support for log levels and
 * message grouping.
 * <p>
 * Supports repeated message grouping and production/debug modes. Thread safety
 * is not guaranteed.
 */
public class Logger {

    /**
     * Log levels supported by the logger.
     */
    public enum Level {
        ERROR, WARNING, INFO, DEBUG
    }

    /**
     * Currently enabled log levels.
     */
    private static Set<Level> enabledLevels = Set.of(Level.ERROR, Level.WARNING, Level.INFO);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Tracks last logged message for grouping repeated logs
    private static String lastMessage = null;
    private static Level lastLevel = null;
    private static int repeatCount = 1;
    private static String originalLine = null;

    /**
     * Enables specified log levels.
     * 
     * @param levels log levels to enable
     */
    public static void enable(Level... levels) {
        enabledLevels = Set.of(levels);
    }

    /**
     * Enables all log levels (including DEBUG).
     */
    public static void enableAll() {
        enabledLevels = Set.of(Level.ERROR, Level.WARNING, Level.INFO, Level.DEBUG);
    }

    /**
     * Enables only production log levels (ERROR, WARNING).
     */
    public static void enableProductionMode() {
        enabledLevels = Set.of(Level.ERROR, Level.WARNING);
    }

    /**
     * Logs a message at the specified level, optionally with a throwable.
     * Groups repeated messages for better readability.
     * 
     * @param level     log level
     * @param message   log message
     * @param throwable optional exception to log
     */
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

        // Group repeated messages for compact output
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

    /**
     * Prints a newline if a message group was active.
     */
    private static void finishPreviousGroup() {
        if (lastMessage != null) {
            System.out.println();
        }
    }

    /**
     * Resets message grouping state.
     */
    private static void resetTracking() {
        lastMessage = null;
        lastLevel = null;
        repeatCount = 1;
        originalLine = null;
    }

    /**
     * Logs an error message.
     * 
     * @param message error message
     */
    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    /**
     * Logs an error message with exception.
     * 
     * @param message   error message
     * @param throwable exception to log
     */
    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    /**
     * Logs a warning message.
     * 
     * @param message warning message
     */
    public static void warning(String message) {
        log(Level.WARNING, message, null);
    }

    /**
     * Logs a warning message with exception.
     * 
     * @param message   warning message
     * @param throwable exception to log
     */
    public static void warning(String message, Throwable throwable) {
        log(Level.WARNING, message, throwable);
    }

    /**
     * Logs an info message.
     * 
     * @param message info message
     */
    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    /**
     * Logs an info message with exception.
     * 
     * @param message   info message
     * @param throwable exception to log
     */
    public static void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    /**
     * Logs a debug message.
     * 
     * @param message debug message
     */
    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    /**
     * Logs a debug message with exception.
     * 
     * @param message   debug message
     * @param throwable exception to log
     */
    public static void debug(String message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }

    /**
     * Flushes any active message group and resets logger state.
     */
    public static void flush() {
        finishPreviousGroup();
        resetTracking();
    }
}
