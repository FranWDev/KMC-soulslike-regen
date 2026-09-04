package dev.franwdev.soulslikeregen.event;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        // Note: Data Attachments configured with copyOnDeath() automatically persist
        // across deaths. We keep explicit sync for dimension travel / edge cases.
        RegenCapProvider.get(oldPlayer).ifPresent(oldCap -> {
            RegenCapProvider.get(newPlayer).ifPresent(newCap -> {
                newCap.setCurrentFatigue(oldCap.getCurrentFatigue());
                newCap.setMaxCap(oldCap.getMaxCap());
                newCap.setTotalFatigueSpent(oldCap.getTotalFatigueSpent());
                newCap.setCurrentLevel(oldCap.getCurrentLevel());
                newCap.setLastDamageTick(oldCap.getLastDamageTick());
                newCap.setBonusClaimed(oldCap.isBonusClaimed());
                newCap.setLastCampfireUseTick(oldCap.getLastCampfireUseTick());
                newCap.setLastBedUseTick(oldCap.getLastBedUseTick());
                newCap.setLastWaystoneUseTick(oldCap.getLastWaystoneUseTick());
                newCap.setActionBarEnabled(oldCap.isActionBarEnabled());
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.level().isClientSide() && !event.wakeImmediately()) {
                RegenCapProvider.get(player).ifPresent(cap -> {
                    ServerLevel level = player.serverLevel();
                    long currentDay = Math.max(0L, level.getDayTime()) / 24000L;
                    long lastBedDay = cap.getLastBedUseTick() < 0 ? -1L : Math.max(0L, cap.getLastBedUseTick()) / 24000L;

                    // Bed Rest (percentage reduction of current fatigue)
                    if (cap.getLastBedUseTick() < 0 || currentDay > lastBedDay) {
                        float drained = cap.getCurrentFatigue() * RegenConfig.BED_REDUCTION_PERCENT;
                        cap.drainFatigue(drained);
                        cap.setLastBedUseTick(level.getDayTime());
                        FeedbackHelper.sendBedRest(player, drained);
                    }
                });
            }
        }
    }
}
