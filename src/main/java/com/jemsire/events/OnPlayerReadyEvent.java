package com.jemsire.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.jemsire.utils.DiscordWebhookSender;

import java.util.HashMap;
import java.util.Map;

public class OnPlayerReadyEvent {

    //Event gets called on world change as well.
    //Disabled for now

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getDisplayName());
        placeholders.put("playerDisplayName", player.getDisplayName()); // Alias for clarity

        DiscordWebhookSender.sendEventMessage("PlayerReady", placeholders);
    }

}