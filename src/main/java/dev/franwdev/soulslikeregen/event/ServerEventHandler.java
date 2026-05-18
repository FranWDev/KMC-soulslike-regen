package dev.franwdev.soulslikeregen.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;

@Mod.EventBusSubscriber(modid = SoulslikeRegen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.reviveCaps();
        RegenCapProvider.get(oldPlayer).ifPresent(oldCap -> {
            RegenCapProvider.get(newPlayer).ifPresent(newCap -> {
                newCap.setCurrentFatigue(oldCap.getCurrentFatigue());
                newCap.setMaxCap(oldCap.getMaxCap());
                newCap.addFatigueSpent(oldCap.getTotalFatigueSpent());
                newCap.setCurrentLevel(oldCap.getCurrentLevel());
                newCap.setLastDamageTick(oldCap.getLastDamageTick());
                newCap.setBonusClaimed(oldCap.isBonusClaimed());
                newCap.setLastCampfireUseTick(oldCap.getLastCampfireUseTick());
                newCap.setLastBedUseTick(oldCap.getLastBedUseTick());
            });
        });
        oldPlayer.invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.level().isClientSide() && player.level().isDay()) {
                RegenCapProvider.get(player).ifPresent(cap -> {
                    long currentTime = player.level().getGameTime();
                    if (cap.getLastBedUseTick() < 0 || currentTime - cap.getLastBedUseTick() >= RegenConfig.BED_COOLDOWN_TICKS) {
                        cap.drainFatigue(RegenConfig.BED_REDUCTION);
                        cap.setLastBedUseTick(currentTime);
                        FeedbackHelper.sendBedRest(player, RegenConfig.BED_REDUCTION);
                    }
                });
            }
        }
    }
}
