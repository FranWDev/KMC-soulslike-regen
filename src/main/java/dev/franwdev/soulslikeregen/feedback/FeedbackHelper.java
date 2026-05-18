package dev.franwdev.soulslikeregen.feedback;

import dev.franwdev.soulslikeregen.capability.IRegenCap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class FeedbackHelper {

    private static final int BAR_LENGTH = 10;
    private static final boolean TEST_MODE = Boolean.getBoolean("soulslikeregen.testMode");

    // ── Action Bar ───────────────────────────────────────────────────────────

    public static void sendStatusActionBar(ServerPlayer player, IRegenCap cap) {
        if (cap.getCurrentFatigue() == 0 && !cap.isExhausted()) {
            return;
        }
        Component bar = buildStatusBar(cap);
        sendActionBar(player, bar);
    }

    public static void sendActionBar(ServerPlayer player, Component msg) {
        if (!canSend(player)) {
            return;
        }
        player.connection.send(new ClientboundSetActionBarTextPacket(msg));
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
                .withStyle(ChatFormatting.RED);
        }

        if (level > 0) {
            return Component.translatable("msg.soulslikeregen.actionbar.level", bar.toString(), curStr, maxStr, String.valueOf(level))
                .withStyle(ChatFormatting.AQUA);
        }

        return Component.translatable("msg.soulslikeregen.actionbar.normal", bar.toString(), curStr, maxStr)
            .withStyle(ChatFormatting.AQUA);
    }

    // ── Title ────────────────────────────────────────────────────────────────

    private static void sendTitle(ServerPlayer player, Component title, Component subtitle) {
        if (!canSend(player)) {
            return;
        }
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        if (subtitle != null) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    private static void sendChat(ServerPlayer player, Component msg) {
        if (!canSend(player)) {
            return;
        }
        player.sendSystemMessage(msg);
    }

    private static boolean canSend(ServerPlayer player) {
        if (TEST_MODE || player == null || player.connection == null) {
            return false;
        }
        Connection connection = player.connection.connection;
        return connection != null && connection.channel() != null;
    }

    // ── Specific event messages ──────────────────────────────────────────────

    public static void sendExhaustedFeedback(ServerPlayer player) {
        sendTitle(player,
            Component.translatable("msg.soulslikeregen.exhausted.title").withStyle(ChatFormatting.RED),
            Component.translatable("msg.soulslikeregen.exhausted.subtitle").withStyle(ChatFormatting.GRAY)
        );
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.exhausted.chat"), ChatFormatting.RED));
    }

    public static void sendEnteredNexus(ServerPlayer player) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.nexus.entered"), ChatFormatting.GREEN));
    }

    public static void sendLeftNexus(ServerPlayer player) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.nexus.left"), ChatFormatting.GRAY));
    }

    public static void sendInnWarmupStarted(ServerPlayer player, int warmupSeconds) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.inn.warmup", String.valueOf(warmupSeconds)), ChatFormatting.YELLOW));
    }

    public static void sendInnDrainStarted(ServerPlayer player) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.inn.drain"), ChatFormatting.GREEN));
    }

    public static void sendFullyRested(ServerPlayer player, Component source) {
        sendTitle(player,
            Component.translatable("msg.soulslikeregen.rest.full.title").withStyle(ChatFormatting.GREEN),
            null
        );
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.rest.full.chat", source), ChatFormatting.GREEN));
    }

    public static void sendLevelUp(ServerPlayer player, int newLevel, float newMaxCap, float increase) {
        int oldMaxCap = (int) Math.round(newMaxCap - increase);
        int maxCapInt = (int) Math.round(newMaxCap);
        
        sendTitle(player,
            Component.translatable("msg.soulslikeregen.level_up.title").withStyle(ChatFormatting.GOLD),
            Component.translatable("msg.soulslikeregen.level_up.subtitle", String.valueOf(oldMaxCap), String.valueOf(maxCapInt))
                .withStyle(ChatFormatting.YELLOW)
        );
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.level_up.chat", String.valueOf(newLevel), String.valueOf(maxCapInt)), ChatFormatting.GOLD));
    }

    public static void sendDayBonus(ServerPlayer player, float drained) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.bonus.day", String.valueOf((int)Math.round(drained))), ChatFormatting.AQUA));
    }

    public static void sendBedRest(ServerPlayer player, float drained) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.bonus.bed", String.valueOf((int)Math.round(drained))), ChatFormatting.GREEN));
    }

    public static void sendCampfireRest(ServerPlayer player, float drained) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.bonus.campfire", String.valueOf((int)Math.round(drained))), ChatFormatting.GREEN));
    }

    public static void sendWaystoneReset(ServerPlayer player) {
        sendChat(player, formatPrefix(Component.translatable("msg.soulslikeregen.bonus.waystone"), ChatFormatting.AQUA));
    }
    
    private static Component formatPrefix(MutableComponent component, ChatFormatting color) {
        return Component.literal("[SLRegen] ").withStyle(color).append(component.withStyle(color));
    }
}
