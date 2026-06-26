package dev.franwdev.soulslikeregen.compat;

import net.blay09.mods.waystones.api.WaystoneActivatedEvent;
import net.blay09.mods.waystones.core.WarpMode;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;
import dev.franwdev.soulslikeregen.feedback.ServerTranslationHelper;
import dev.franwdev.soulslikeregen.api.event.FatigueResetEvent;

public class WaystonesCompat {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("waystones");
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new WaystonesListener());
    }

    private static void applyWaystoneHeal(ServerPlayer player) {
        RegenCapProvider.get(player).ifPresent(cap -> {
            long currentTick = player.level().getGameTime();
            long lastUse = cap.getLastWaystoneUseTick();
            // 3 minutes = 180 seconds = 3600 ticks
            if (lastUse != -1L && currentTick - lastUse < 3600L) {
                return; // Cooldown active
            }

            if (cap.getCurrentFatigue() > 0) {
                FatigueResetEvent event = new FatigueResetEvent(player, FatigueResetEvent.ResetSource.WAYSTONE);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    return; // Canceled by another mod
                }

                cap.setCurrentFatigue(0.0f);
                cap.setLastWaystoneUseTick(currentTick);
                FeedbackHelper.sendWaystoneReset(player);
                FeedbackHelper.sendFullyRested(player, ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.source.waystone"));
            }
        });
    }

    private static class WaystonesListener {
        @SubscribeEvent
        public void onWaystoneActivated(WaystoneActivatedEvent event) {
            if (event.getPlayer() instanceof ServerPlayer player) {
                applyWaystoneHeal(player);
            }
        }

        @SubscribeEvent
        public void onContainerOpen(PlayerContainerEvent.Open event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (event.getContainer() instanceof WaystoneSelectionMenu menu) {
                    WarpMode mode = menu.getWarpMode();
                    if (mode == WarpMode.WAYSTONE_TO_WAYSTONE ||
                        mode == WarpMode.SHARESTONE_TO_SHARESTONE ||
                        mode == WarpMode.PORTSTONE_TO_WAYSTONE) {
                        applyWaystoneHeal(player);
                    }
                }
            }
        }
    }
}
