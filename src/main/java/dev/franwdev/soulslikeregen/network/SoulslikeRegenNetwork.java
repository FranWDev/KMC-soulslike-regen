package dev.franwdev.soulslikeregen.network;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class SoulslikeRegenNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SoulslikeRegen.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                FatigueDataPacket.class,
                FatigueDataPacket::encode,
                FatigueDataPacket::decode,
                FatigueDataPacket::handle
        );
    }

    public static void sendToClient(ServerPlayer player, FatigueDataPacket packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
