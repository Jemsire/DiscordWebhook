package com.jemsire.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jemsire.utils.DiscordWebhookSender;

import javax.annotation.Nonnull;
import java.awt.*;

public class OnPlayerDeathEvent extends DeathSystems.OnDeathSystem {
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    public void onComponentAdded(@Nonnull Ref ref, @Nonnull DeathComponent component, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
        DeathComponent deathComponent = (DeathComponent)store.getComponent(ref, DeathComponent.getComponentType());
        if (playerComponent != null && deathComponent != null) {
            String playerName = playerComponent.getDisplayName();
            String deathCause = deathComponent.getDeathMessage().getAnsiMessage().replace("You were", "was");

            DiscordWebhookSender.sendEmbed(
                    "☠ Player Died",
                    playerName + " " + deathCause,
                    "",
                    0xBDBDBD // green
            );
        }
    }
}
