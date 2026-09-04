package dev.franwdev.soulslikeregen.compat;

import dev.franwdev.soulslikeregen.api.event.FatigueResetEvent;
import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;
import dev.franwdev.soulslikeregen.feedback.ServerTranslationHelper;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.event.WaystoneActivatedEvent;
import net.blay09.mods.waystones.block.PortstoneBlock;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class WaystonesCompat {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("waystones");
    }

    public static void init() {
        // Subscribe to Balm's WaystoneActivatedEvent
        Balm.getEvents().onEvent(WaystoneActivatedEvent.class, event -> {
            if (event.getPlayer() instanceof ServerPlayer player) {
                applyWaystoneHeal(player);
            }
        });

        // Register right-click block interaction listener on NeoForge event bus
        NeoForge.EVENT_BUS.register(new WaystonesRightClickListener());
    }

    private static void applyWaystoneHeal(ServerPlayer player) {
        RegenCapProvider.get(player).ifPresent(cap -> {
            long currentTick = player.level().getGameTime();
            long lastUse = cap.getLastWaystoneUseTick();
            // 30 seconds = 600 ticks
            if (lastUse != -1L && currentTick - lastUse < 600L) {
                return; // Cooldown active
            }

            if (cap.getCurrentFatigue() > 0) {
                FatigueResetEvent event = new FatigueResetEvent(player, FatigueResetEvent.ResetSource.WAYSTONE);
                if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    return; // Canceled by another mod
                }

                cap.setCurrentFatigue(0.0f);
                cap.setLastWaystoneUseTick(currentTick);
                FeedbackHelper.sendWaystoneReset(player);
                FeedbackHelper.sendFullyRested(player, ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.source.waystone"));
            }
        });
    }

    private static class WaystonesRightClickListener {
        @SubscribeEvent
        public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
            if (event.getSide().isServer() && event.getEntity() instanceof ServerPlayer player) {
                if (event.getHand() == InteractionHand.MAIN_HAND) {
                    Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
                    if (block instanceof WaystoneBlockBase || block instanceof PortstoneBlock) {
                        applyWaystoneHeal(player);
                    }
                }
            }
        }
    }
}
