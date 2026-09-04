package dev.franwdev.soulslikeregen.network;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import dev.franwdev.soulslikeregen.client.FatigueClientData;
import dev.franwdev.soulslikeregen.client.FatigueClientData.RecoveryType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FatigueDataPayload(
    float currentFatigue,
    float maxCap,
    boolean exhausted,
    RecoveryType recoveryType
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FatigueDataPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeRegen.MODID, "fatigue_data"));

    public static final StreamCodec<ByteBuf, FatigueDataPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        FatigueDataPayload::currentFatigue,
        ByteBufCodecs.FLOAT,
        FatigueDataPayload::maxCap,
        ByteBufCodecs.BOOL,
        FatigueDataPayload::exhausted,
        ByteBufCodecs.idMapper(i -> RecoveryType.values()[i], RecoveryType::ordinal),
        FatigueDataPayload::recoveryType,
        FatigueDataPayload::new
    );

    @Override
    public CustomPacketPayload.Type<FatigueDataPayload> type() {
        return TYPE;
    }

    public static void handle(FatigueDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            FatigueClientData.update(
                payload.currentFatigue(),
                payload.maxCap(),
                payload.exhausted(),
                payload.recoveryType()
            );
        });
    }
}
