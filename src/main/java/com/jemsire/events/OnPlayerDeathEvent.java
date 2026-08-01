package com.jemsire.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jemsire.utils.DiscordWebhookSender;
import com.jemsire.utils.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class OnPlayerDeathEvent extends DeathSystems.OnDeathSystem {
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) return;

            String playerName = playerRef.getUsername();
            String deathCause = component.getDeathMessage().getAnsiMessage().replace("You were", "was");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            placeholders.put("playerDisplayName", playerName);
            placeholders.put("deathCause", deathCause);
            placeholders.put("deathMessage", deathCause);

            try {
                String rawDeathMessage = component.getDeathMessage().getAnsiMessage();
                placeholders.put("deathMessageRaw", rawDeathMessage);
            } catch (Exception e) {
                Logger.severe("Raw message not available", e);
            }

            DiscordWebhookSender.sendEventMessage("PlayerDeath", placeholders);
        } catch (Exception e){
            Logger.severe("Error in onPlayerDeathEvent", e);
        }
    }
}
