package dev.franwdev.soulslikeregen.capability;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import dev.franwdev.soulslikeregen.SoulslikeRegen;

public class RegenCapProvider {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SoulslikeRegen.MODID);

    public static final Supplier<AttachmentType<RegenCap>> REGEN_CAP =
        ATTACHMENT_TYPES.register(
            "regen_cap",
            () -> AttachmentType.serializable(RegenCap::new).copyOnDeath().build()
        );

    // ── Static helper ─────────────────────────────────────────────────────────

    /**
     * Retrieve the capability / attachment from a player.
     * Never store the result across ticks — always re-fetch.
     */
    public static Optional<IRegenCap> get(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(player.getData(REGEN_CAP));
    }
}
