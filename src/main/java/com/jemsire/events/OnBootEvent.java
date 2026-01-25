package com.jemsire.events;

import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.jemsire.utils.DiscordWebhookSender;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class OnBootEvent {
    public static void onBootEvent(BootEvent event) {
        Map<String, String> placeholders = new HashMap<>();
        
        // Get current time in system default timezone
        ZonedDateTime systemTime = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedTime = systemTime.format(formatter);
        placeholders.put("time", formattedTime);
        
        // Timezone-specific placeholders (e.g., {time-america/los_angeles}) are now
        // resolved dynamically by PlaceholderReplacer, which validates timezones
        // and defaults to UTC if invalid

        DiscordWebhookSender.sendEventMessage("Boot", placeholders);
    }
}
