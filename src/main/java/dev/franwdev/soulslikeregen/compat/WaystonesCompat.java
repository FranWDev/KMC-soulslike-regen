package dev.franwdev.soulslikeregen.compat;

import net.blay09.mods.waystones.api.WaystoneActivatedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;

public class WaystonesCompat {
    public static boolean isLoaded() {
        return ModList.get().isLoaded("waystones");
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new WaystonesListener());
    }

    private static class WaystonesListener {
        @SubscribeEvent
        public void onWaystoneActivated(WaystoneActivatedEvent event) {
            Player eventPlayer = event.getPlayer();
            if (eventPlayer instanceof ServerPlayer player) {
                RegenCapProvider.get(player).ifPresent(cap -> {
                    cap.setCurrentFatigue(0.0f);
                    FeedbackHelper.sendWaystoneReset(player);
                    FeedbackHelper.sendFullyRested(player, Component.translatable("msg.soulslikeregen.source.waystone"));
                });
            }
        }
    }
}
