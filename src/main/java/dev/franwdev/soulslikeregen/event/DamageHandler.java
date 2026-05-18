package dev.franwdev.soulslikeregen.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import dev.franwdev.soulslikeregen.capability.RegenCapProvider;

@Mod.EventBusSubscriber(modid = SoulslikeRegen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageHandler {

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RegenCapProvider.get(player).ifPresent(cap -> {
                cap.setLastDamageTick(player.level().getGameTime());
            });
        }
    }
}
