package dev.franwdev.soulslikeregen.compat;

import net.blay09.mods.waystones.api.WaystoneActivatedEvent;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.blay09.mods.waystones.block.PortstoneBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
            // 30 seconds = 600 ticks
            if (lastUse != -1L && currentTick - lastUse < 600L) {
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
