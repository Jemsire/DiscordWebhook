package com.jemsire.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import com.jemsire.plugin.DiscordWebhook;

import java.util.logging.Level;

public class Logger {

    static HytaleLogger logger = DiscordWebhook.get().getLogger();

    public static void log(String message, Level level){
        logger.at(level).log(message);
    }

}
