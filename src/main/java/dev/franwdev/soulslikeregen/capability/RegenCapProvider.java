package dev.franwdev.soulslikeregen.capability;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RegenCapProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<IRegenCap> REGEN_CAP =
        CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation KEY =
        new ResourceLocation(SoulslikeRegen.MODID, "regen_cap");

    private final RegenCap instance = new RegenCap();
    private final LazyOptional<IRegenCap> optional = LazyOptional.of(() -> instance);

    // ── Capability registration (mod bus) ────────────────────────────────────

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IRegenCap.class);
    }

    // ── Capability attachment (forge bus) ────────────────────────────────────

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(KEY, new RegenCapProvider());
        }
    }

    // ── ICapabilitySerializable ──────────────────────────────────────────────

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return REGEN_CAP.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        instance.deserializeNBT(tag);
    }

    // ── Static helper ─────────────────────────────────────────────────────────

    /**
     * Retrieve the capability from a player.
     * Never store the result across ticks — always re-fetch.
     */
    @Nonnull
    public static LazyOptional<IRegenCap> get(Player player) {
        return player.getCapability(REGEN_CAP);
    }
}
