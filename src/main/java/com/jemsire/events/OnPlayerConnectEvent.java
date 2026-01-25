package com.jemsire.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jemsire.utils.DiscordWebhookSender;

import java.util.HashMap;
import java.util.Map;

public class OnPlayerConnectEvent {

    public static void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        String username = playerRef.getUsername();
        String uuid = String.valueOf(playerRef.getUuid());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", username);
        placeholders.put("playerUuid", uuid);

        DiscordWebhookSender.sendEventMessage("PlayerConnect", placeholders);
    }

}