package dev.franwdev.soulslikeregen.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

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
                newCap.setLastWaystoneUseTick(oldCap.getLastWaystoneUseTick());
                newCap.setActionBarEnabled(oldCap.isActionBarEnabled());
            });
        });
        oldPlayer.invalidateCaps();
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
