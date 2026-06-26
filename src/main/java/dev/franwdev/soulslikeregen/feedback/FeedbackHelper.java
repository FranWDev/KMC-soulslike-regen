package dev.franwdev.soulslikeregen.feedback;

import dev.franwdev.soulslikeregen.capability.IRegenCap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

public class FeedbackHelper {

    private static final int BAR_LENGTH = 10;
    private static final boolean TEST_MODE = Boolean.getBoolean("soulslikeregen.testMode");

    // ── Action Bar ───────────────────────────────────────────────────────────

    public static void sendStatusActionBar(ServerPlayer player, IRegenCap cap) {
        if (cap.getCurrentFatigue() == 0 && !cap.isExhausted()) {
            return;
        }
        Component bar = buildStatusBar(player, cap);
        sendActionBar(player, bar);
    }

    public static void sendActionBar(ServerPlayer player, Component msg) {
        if (!canSend(player)) {
            return;
        }
        player.connection.send(new ClientboundSetActionBarTextPacket(msg));
    }

    public static Component buildStatusBar(ServerPlayer player, IRegenCap cap) {
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
            return ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.actionbar.exhausted", bar.toString(), curStr, maxStr)
                .withStyle(ChatFormatting.RED);
        }

        if (level > 0) {
            return ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.actionbar.level", bar.toString(), curStr, maxStr, String.valueOf(level))
                .withStyle(ChatFormatting.AQUA);
        }

        return ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.actionbar.normal", bar.toString(), curStr, maxStr)
            .withStyle(ChatFormatting.AQUA);
    }

    // ── Chat/System Messages ──────────────────────────────────────────────────

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

    public static void sendEnteredNexus(ServerPlayer player) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.nexus.entered"), ChatFormatting.GREEN));
    }

    public static void sendLeftNexus(ServerPlayer player) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.nexus.left"), ChatFormatting.GRAY));
    }

    public static void sendInnWarmupStarted(ServerPlayer player, int warmupSeconds) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.inn.warmup", String.valueOf(warmupSeconds)), ChatFormatting.YELLOW));
    }

    public static void sendInnDrainStarted(ServerPlayer player) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.inn.drain"), ChatFormatting.GREEN));
    }

    public static void sendFullyRested(ServerPlayer player, Component source) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.rest.full.chat", source), ChatFormatting.GREEN));
    }

    public static void sendLevelUp(ServerPlayer player, int newLevel, float newMaxCap, float increase) {
        int maxCapInt = (int) Math.round(newMaxCap);
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.level_up.chat", String.valueOf(newLevel), String.valueOf(maxCapInt)), ChatFormatting.GOLD));
    }

    public static void sendDayBonus(ServerPlayer player, float drained) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.bonus.day", String.valueOf((int)Math.round(drained))), ChatFormatting.AQUA));
    }

    public static void sendBedRest(ServerPlayer player, float drained) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.bonus.bed", String.valueOf((int)Math.round(drained))), ChatFormatting.GREEN));
    }

    public static void sendCampfireRest(ServerPlayer player, float drained) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.bonus.campfire", String.valueOf((int)Math.round(drained))), ChatFormatting.GREEN));
    }

    public static void sendWaystoneReset(ServerPlayer player) {
        sendChat(player, formatPrefix(ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.bonus.waystone"), ChatFormatting.AQUA));
    }
    
    private static Component formatPrefix(MutableComponent component, ChatFormatting color) {
        return Component.literal("[SLRegen] ").withStyle(color).append(component.withStyle(color));
    }
}
