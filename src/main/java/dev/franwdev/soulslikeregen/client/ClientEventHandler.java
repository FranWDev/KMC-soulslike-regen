package dev.franwdev.soulslikeregen.client;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SoulslikeRegen.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
            VanillaGuiLayers.ARMOR_LEVEL,
            ResourceLocation.fromNamespaceAndPath(SoulslikeRegen.MODID, "fatigue_hud"),
            FatigueHudOverlay.HUD
        );
    }
}
