package com.jemsire.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import com.jemsire.plugin.DiscordWebhook;

import java.util.logging.Level;

/**
 * Structured logging utility for DiscordWebhook plugin
 * Provides consistent logging with proper error handling
 */
public class Logger {
    private static HytaleLogger logger;

    /**
     * Gets the logger instance, initializing if necessary
     */
    private static HytaleLogger getLogger() {
        if (logger == null) {
            DiscordWebhook plugin = DiscordWebhook.get();
            if (plugin != null) {
                logger = plugin.getLogger();
            }
        }
        return logger;
    }

    /**
     * Logs a message at the specified level
     * @param message The message to log
     * @param level The log level
     */
    public static void log(String message, Level level) {
        HytaleLogger loggerInstance = getLogger();
        if (loggerInstance != null) {
            loggerInstance.at(level).log(message);
        } else {
            // Fallback to system out if logger not available
            System.out.println("[" + level + "] " + message);
        }
    }

    /**
     * Logs an info message
     */
    public static void info(String message) {
        log(message, Level.INFO);
    }

    /**
     * Logs a warning message
     */
    public static void warning(String message) {
        log(message, Level.WARNING);
    }

    /**
     * Logs a severe/error message
     */
    public static void severe(String message) {
        log(message, Level.SEVERE);
    }

    /**
     * Logs a severe/error message with exception
     */
    public static void severe(String message, Throwable throwable) {
        HytaleLogger loggerInstance = getLogger();
        if (loggerInstance != null) {
            loggerInstance.at(Level.SEVERE).log(message + ": " + throwable.getMessage());
            throwable.printStackTrace();
        } else {
            System.err.println("[SEVERE] " + message);
            throwable.printStackTrace();
        }
    }
}
