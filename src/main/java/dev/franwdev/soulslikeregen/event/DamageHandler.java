package dev.franwdev.soulslikeregen.event;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class DamageHandler {

    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RegenCapProvider.get(player).ifPresent(cap -> {
                cap.setLastDamageTick(player.level().getGameTime());

                // Increment buffer with net damage if it classified as environmental
                if (DamageClassifier.isEnvironmental(event.getSource())) {
                    cap.addEnvironmentalDamage(event.getNewDamage());
                }
            });
        }
    }
}

