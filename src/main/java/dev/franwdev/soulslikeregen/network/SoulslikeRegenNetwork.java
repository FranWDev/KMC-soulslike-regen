package dev.franwdev.soulslikeregen.network;

import dev.franwdev.soulslikeregen.client.FatigueClientData.RecoveryType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class SoulslikeRegenNetwork {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
            FatigueDataPayload.TYPE,
            FatigueDataPayload.STREAM_CODEC,
            FatigueDataPayload::handle
        );
    }

    public static void sendToClient(ServerPlayer player, FatigueDataPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToClient(ServerPlayer player, FatigueDataPacket packet) {
        sendToClient(player, packet.toPayload());
    }

    public static void sendToClient(ServerPlayer player, float currentFatigue, float maxCap, boolean exhausted, RecoveryType recoveryType) {
        sendToClient(player, new FatigueDataPayload(currentFatigue, maxCap, exhausted, recoveryType));
    }
}
