package com.jemsire.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jemsire.utils.DiscordWebhookSender;

import java.util.HashMap;
import java.util.Map;

public class OnPlayerDisconnectEvent {

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getUsername());
        placeholders.put("playerUsername", player.getUsername()); // Alias for clarity

        DiscordWebhookSender.sendEventMessage("PlayerDisconnect", placeholders);
    }

}