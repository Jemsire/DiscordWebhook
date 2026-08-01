package com.jemsire.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jemsire.utils.DiscordWebhookSender;

import java.util.HashMap;
import java.util.Map;

public class OnPlayerReadyEvent {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Ref<EntityStore> entityRef = event.getPlayerRef();
        Store<EntityStore> store = entityRef.getStore();
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        String username = playerRef.getUsername();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", username);
        placeholders.put("playerDisplayName", username);

        DiscordWebhookSender.sendEventMessage("PlayerReady", placeholders);
    }

}