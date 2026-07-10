package dev.franwdev.soulslikeregen.network;

import java.util.function.Supplier;
import dev.franwdev.soulslikeregen.client.FatigueClientData;
import dev.franwdev.soulslikeregen.client.FatigueClientData.RecoveryType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class FatigueDataPacket {
    private final float currentFatigue;
    private final float maxCap;
    private final boolean exhausted;
    private final RecoveryType recoveryType;

    public FatigueDataPacket(float currentFatigue, float maxCap, boolean exhausted, RecoveryType recoveryType) {
        this.currentFatigue = currentFatigue;
        this.maxCap = maxCap;
        this.exhausted = exhausted;
        this.recoveryType = recoveryType;
    }

    public static void encode(FatigueDataPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.currentFatigue);
        buf.writeFloat(msg.maxCap);
        buf.writeBoolean(msg.exhausted);
        buf.writeEnum(msg.recoveryType);
    }

    public static FatigueDataPacket decode(FriendlyByteBuf buf) {
        return new FatigueDataPacket(
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readEnum(RecoveryType.class)
        );
    }

    public static void handle(FatigueDataPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FatigueClientData.update(
                msg.currentFatigue,
                msg.maxCap,
                msg.exhausted,
                msg.recoveryType
            ));
        });
        ctx.setPacketHandled(true);
    }
}
