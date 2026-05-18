package dev.kmc.soulslikeregen.feedback;

import dev.kmc.soulslikeregen.capability.IRegenCap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class FeedbackHelper {

    private static final int BAR_LENGTH = 10;

    // ── Action Bar ───────────────────────────────────────────────────────────

    public static void sendStatusActionBar(ServerPlayer player, IRegenCap cap) {
        if (cap.getCurrentFatigue() == 0 && !cap.isExhausted()) {
            return;
        }
        Component bar = buildStatusBar(cap);
        sendActionBar(player, bar);
    }

    public static void sendActionBar(ServerPlayer player, Component msg) {
        player.connection.send(
            new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(msg)
        );
    }

    public static Component buildStatusBar(IRegenCap cap) {
        float current = cap.getCurrentFatigue();
        float max     = cap.getMaxCap();
        int level     = cap.getCurrentLevel();

        int filled = (int) Math.round((current / max) * BAR_LENGTH);
        filled = Math.min(filled, BAR_LENGTH);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < BAR_LENGTH; i++) {
            bar.append(i < filled ? '█' : '░');
        }

        String curStr = String.valueOf((int)Math.round(current));
        String maxStr = String.valueOf((int)Math.round(max));

        if (cap.isExhausted()) {
            return Component.translatable("msg.soulslikeregen.actionbar.exhausted", bar.toString(), curStr, maxStr)
                .withStyle(net.minecraft.ChatFormatting.RED);
        }

        if (level > 0) {
            return Component.translatable("msg.soulslikeregen.actionbar.level", bar.toString(), curStr, maxStr, String.valueOf(level))
                .withStyle(net.minecraft.ChatFormatting.AQUA);
        }

        return Component.translatable("msg.soulslikeregen.actionbar.normal", bar.toString(), curStr, maxStr)
            .withStyle(net.minecraft.ChatFormatting.AQUA);
    }

    // ── Title ────────────────────────────────────────────────────────────────

    private static void sendTitle(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        if (subtitle != null) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    // ── Specific event messages ──────────────────────────────────────────────

    public static void sendExhaustedFeedback(ServerPlayer player) {
        sendTitle(player,
            Component.translatable("msg.soulslikeregen.exhausted.title").withStyle(net.minecraft.ChatFormatting.RED),
            Component.translatable("msg.soulslikeregen.exhausted.subtitle").withStyle(net.minecraft.ChatFormatting.GRAY)
        );
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.exhausted.chat"), net.minecraft.ChatFormatting.RED));
    }

    public static void sendEnteredNexus(ServerPlayer player) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.nexus.entered"), net.minecraft.ChatFormatting.GREEN));
    }

    public static void sendLeftNexus(ServerPlayer player) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.nexus.left"), net.minecraft.ChatFormatting.GRAY));
    }

    public static void sendInnWarmupStarted(ServerPlayer player, int warmupSeconds) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.inn.warmup", String.valueOf(warmupSeconds)), net.minecraft.ChatFormatting.YELLOW));
    }

    public static void sendInnDrainStarted(ServerPlayer player) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.inn.drain"), net.minecraft.ChatFormatting.GREEN));
    }

    public static void sendFullyRested(ServerPlayer player, Component source) {
        sendTitle(player,
            Component.translatable("msg.soulslikeregen.rest.full.title").withStyle(net.minecraft.ChatFormatting.GREEN),
            null
        );
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.rest.full.chat", source), net.minecraft.ChatFormatting.GREEN));
    }

    public static void sendLevelUp(ServerPlayer player, int newLevel, float newMaxCap, float increase) {
        int oldMaxCap = (int) Math.round(newMaxCap - increase);
        int maxCapInt = (int) Math.round(newMaxCap);
        
        sendTitle(player,
            Component.translatable("msg.soulslikeregen.level_up.title").withStyle(net.minecraft.ChatFormatting.GOLD),
            Component.translatable("msg.soulslikeregen.level_up.subtitle", String.valueOf(oldMaxCap), String.valueOf(maxCapInt))
                .withStyle(net.minecraft.ChatFormatting.YELLOW)
        );
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.level_up.chat", String.valueOf(newLevel), String.valueOf(maxCapInt)), net.minecraft.ChatFormatting.GOLD));
    }

    public static void sendDayBonus(ServerPlayer player, float drained) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.bonus.day", String.valueOf((int)Math.round(drained))), net.minecraft.ChatFormatting.AQUA));
    }

    public static void sendBedRest(ServerPlayer player, float drained) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.bonus.bed", String.valueOf((int)Math.round(drained))), net.minecraft.ChatFormatting.GREEN));
    }

    public static void sendCampfireRest(ServerPlayer player, float drained) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.bonus.campfire", String.valueOf((int)Math.round(drained))), net.minecraft.ChatFormatting.GREEN));
    }

    public static void sendWaystoneReset(ServerPlayer player) {
        player.sendSystemMessage(formatPrefix(Component.translatable("msg.soulslikeregen.bonus.waystone"), net.minecraft.ChatFormatting.AQUA));
    }
    
    private static Component formatPrefix(MutableComponent component, net.minecraft.ChatFormatting color) {
        return Component.literal("[SLRegen] ").withStyle(color).append(component.withStyle(color));
    }
}
