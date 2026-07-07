package dev.franwdev.soulslikeregen.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

import dev.franwdev.soulslikeregen.api.event.FatigueResetEvent;
import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.compat.FTBTeamsCompat;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.data.InnData;
import dev.franwdev.soulslikeregen.data.InnEntry;
import dev.franwdev.soulslikeregen.data.NexusData;
import dev.franwdev.soulslikeregen.data.NexusEntry;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;
import dev.franwdev.soulslikeregen.feedback.ServerTranslationHelper;

public class SoulslikeRegenCommand {

    private static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> slRoot = buildCommandTree("soulslikeregen");
        LiteralArgumentBuilder<CommandSourceStack> slAlias = buildCommandTree("slregen");

        dispatcher.register(slRoot);
        dispatcher.register(slAlias);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommandTree(String name) {
        return literal(name)
            .then(literal("status")
                .executes(ctx -> executeStatus(ctx.getSource())))
            
            // --- Nexus Commands (Admin: Permission level 2) ---
            .then(literal("setTeamNexus")
                .requires(src -> src.hasPermission(2))
                .then(argument("coords", getVec3Argument())
                    .then(argument("radius", DoubleArgumentType.doubleArg(0.1))
                        .then(argument("teamName", StringArgumentType.string())
                            .suggests((ctx, builder) -> {
                                FTBTeamsCompat.getAllPartyTeamNames()
                                    .forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> executeSetNexus(
                                ctx.getSource(),
                                getVec3Coord(ctx, "coords"),
                                DoubleArgumentType.getDouble(ctx, "radius"),
                                StringArgumentType.getString(ctx, "teamName")
                             ))
                        )
                    )
                )
            )
            .then(literal("editTeamNexus")
                .requires(src -> src.hasPermission(2))
                .then(argument("id", IntegerArgumentType.integer(1))
                    .then(literal("radius")
                        .then(argument("newRadius", DoubleArgumentType.doubleArg(0.1))
                            .executes(ctx -> executeEditNexusRadius(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                DoubleArgumentType.getDouble(ctx, "newRadius")
                            ))
                        )
                    )
                    .then(literal("coords")
                        .then(argument("coords", getVec3Argument())
                            .executes(ctx -> executeEditNexusCoords(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                getVec3Coord(ctx, "coords")
                            ))
                        )
                    )
                )
            )
            .then(literal("removeTeamNexus")
                .requires(src -> src.hasPermission(2))
                .then(argument("id", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeRemoveNexus(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))
                )
            )
            .then(literal("listNexus")
                .executes(ctx -> executeListNexus(ctx.getSource(), 1))
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeListNexus(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                )
            )

            // --- Inn Commands (Admin: Permission level 2) ---
            .then(literal("setInn")
                .requires(src -> src.hasPermission(2))
                .then(argument("coords", getVec3Argument())
                    .then(argument("radius", DoubleArgumentType.doubleArg(0.1))
                        .executes(ctx -> executeSetInn(
                            ctx.getSource(),
                            getVec3Coord(ctx, "coords"),
                            DoubleArgumentType.getDouble(ctx, "radius")
                        ))
                    )
                )
            )
            .then(literal("editInn")
                .requires(src -> src.hasPermission(2))
                .then(argument("id", IntegerArgumentType.integer(1))
                    .then(literal("radius")
                        .then(argument("newRadius", DoubleArgumentType.doubleArg(0.1))
                            .executes(ctx -> executeEditInnRadius(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                DoubleArgumentType.getDouble(ctx, "newRadius")
                            ))
                        )
                    )
                    .then(literal("coords")
                        .then(argument("coords", getVec3Argument())
                            .executes(ctx -> executeEditInnCoords(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                getVec3Coord(ctx, "coords")
                            ))
                        )
                    )
                )
            )
            .then(literal("removeInn")
                .requires(src -> src.hasPermission(2))
                .then(argument("id", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeRemoveInn(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))
                )
            )
            .then(literal("listInns")
                .executes(ctx -> executeListInns(ctx.getSource(), 1))
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeListInns(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                )
            )
            
            // --- Player Stat Commands (Admin: Permission level 2) ---
            .then(literal("player")
                .requires(src -> src.hasPermission(2))
                .then(argument("playerName", StringArgumentType.string())
                    .suggests((ctx, builder) -> {
                        ctx.getSource().getServer().getPlayerList().getPlayers()
                            .forEach(p -> builder.suggest(p.getName().getString()));
                        return builder.buildFuture();
                    })
                    // Player Fatigue Commands
                    .then(literal("fatigue")
                        .then(literal("get")
                            .executes(ctx -> executePlayerFatigueGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                        .then(literal("set")
                            .then(argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> executePlayerFatigueSet(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                        .then(literal("add")
                            .then(argument("amount", DoubleArgumentType.doubleArg(0.1))
                                .executes(ctx -> executePlayerFatigueAdd(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                        .then(literal("drain")
                            .then(argument("amount", DoubleArgumentType.doubleArg(0.1))
                                .executes(ctx -> executePlayerFatigueDrain(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                    )
                    // Player Capacity Commands
                    .then(literal("capacity")
                        .then(literal("get")
                            .executes(ctx -> executePlayerCapacityGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                        .then(literal("set")
                            .then(argument("amount", DoubleArgumentType.doubleArg(1))
                                .executes(ctx -> executePlayerCapacitySet(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                        .then(literal("reset")
                            .executes(ctx -> executePlayerCapacityReset(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                    )
                    // Player Level Commands
                    .then(literal("level")
                        .then(literal("get")
                            .executes(ctx -> executePlayerLevelGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                        .then(literal("set")
                            .then(argument("level", IntegerArgumentType.integer(0))
                                .executes(ctx -> executePlayerLevelSet(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    IntegerArgumentType.getInteger(ctx, "level")
                                ))
                            )
                        )
                        .then(literal("up")
                            .executes(ctx -> executePlayerLevelUp(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName"),
                                1
                            ))
                            .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> executePlayerLevelUp(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    IntegerArgumentType.getInteger(ctx, "amount")
                                ))
                            )
                        )
                        .then(literal("reset")
                            .executes(ctx -> executePlayerLevelReset(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                    )
                    // Player TotalFatigue Command
                    .then(literal("totalfatigue")
                        .then(literal("get")
                            .executes(ctx -> executePlayerTotalFatigueGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                    )
                    // Player Cooldown Commands
                    .then(literal("cooldown")
                        .then(literal("daybonus")
                            .then(literal("reset")
                                .executes(ctx -> executePlayerCooldownDayBonusReset(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName")
                                ))
                            )
                        )
                        .then(literal("innwarmup")
                            .then(literal("reset")
                                .executes(ctx -> executePlayerCooldownInnWarmupReset(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName")
                                ))
                            )
                        )
                    )
                    // Player Status Command
                    .then(literal("status")
                        .executes(ctx -> executePlayerStatus(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "playerName")
                        ))
                    )
                    // Player Reset Command (HARD RESET)
                    .then(literal("reset")
                        .executes(ctx -> executePlayerReset(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "playerName")
                        ))
                    )
                )
            )
            
            // --- ActionBar Toggle Commands (no permission requirement) ---
            .then(literal("bar")
                .then(literal("on")
                    .executes(ctx -> executeBarEnable(ctx.getSource()))
                )
                .then(literal("off")
                    .executes(ctx -> executeBarDisable(ctx.getSource()))
                )
                .then(literal("status")
                    .executes(ctx -> executeBarStatus(ctx.getSource()))
                )
            );
    }

    private static int executeStatus(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer player) {
            RegenCapProvider.get(player).ifPresent(cap -> {
                Component bar = FeedbackHelper.buildStatusBar(player, cap);
                player.connection.send(new ClientboundSetActionBarTextPacket(bar));
            });
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.only_player"));
        }
        return 1;
    }

    private static int executeSetNexus(CommandSourceStack src, Vec3 coords, double radius, String teamName) {
        ServerLevel level = src.getLevel();
        
        UUID teamId = FTBTeamsCompat.getTeamIdByName(teamName);
        if (FTBTeamsCompat.isLoaded() && teamId == null) {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.nexus.team_not_found", teamName));
            return 0;
        }
        if (teamId == null) {
            teamId = new UUID(0L, 0L);
        }

        NexusData data = NexusData.get(level);
        NexusEntry entry = data.addNexus(coords.x, coords.y, coords.z, radius, level.dimension(), teamId, teamName);

        src.sendSuccess(() -> translatable(src,
            "msg.soulslikeregen.command.nexus.set",
            String.valueOf(entry.id()),
            teamName,
            String.valueOf((int) coords.x),
            String.valueOf((int) coords.y),
            String.valueOf((int) coords.z),
            String.format("%.1f", radius)
        ), true);

        return 1;
    }

    private static int executeEditNexusRadius(CommandSourceStack src, int id, double newRadius) {
        ServerLevel level = src.getLevel();
        NexusData data = NexusData.get(level);
        if (data.updateNexusRadius(id, newRadius)) {
            src.sendSuccess(() -> translatable(src,
                "msg.soulslikeregen.command.nexus.edit_radius",
                String.valueOf(id),
                String.format("%.1f", newRadius)
            ), true);
            return 1;
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.nexus.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeEditNexusCoords(CommandSourceStack src, int id, Vec3 coords) {
        ServerLevel level = src.getLevel();
        NexusData data = NexusData.get(level);
        if (data.updateNexusCoords(id, coords.x, coords.y, coords.z)) {
            src.sendSuccess(() -> translatable(src,
                "msg.soulslikeregen.command.nexus.edit_coords",
                String.valueOf(id),
                String.valueOf((int) coords.x),
                String.valueOf((int) coords.y),
                String.valueOf((int) coords.z)
            ), true);
            return 1;
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.nexus.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeRemoveNexus(CommandSourceStack src, int id) {
        ServerLevel level = src.getLevel();
        NexusData data = NexusData.get(level);
        if (data.removeNexus(id)) {
            src.sendSuccess(() -> translatable(src, "msg.soulslikeregen.command.nexus.remove", String.valueOf(id)), true);
            return 1;
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.nexus.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeListNexus(CommandSourceStack src, int page) {
        ServerLevel level = src.getLevel();
        NexusData data = NexusData.get(level);
        Collection<NexusEntry> all = data.getAllNexuses();
        
        if (all.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No Team Nexuses configured."), false);
            return 1;
        }

        int pageSize = 5;
        int total = all.size();
        int maxPage = (int) Math.ceil((double) total / pageSize);
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        final int finalPage = page;
        final int finalMaxPage = maxPage;
        src.sendSuccess(() -> translatable(src, "msg.soulslikeregen.command.nexus.list_title", String.valueOf(finalPage), String.valueOf(finalMaxPage)), false);

        List<NexusEntry> list = new ArrayList<>(all);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            NexusEntry entry = list.get(i);
            src.sendSuccess(() -> translatable(src,
                "msg.soulslikeregen.command.nexus.list_item",
                String.valueOf(entry.id()),
                entry.teamName(),
                String.valueOf((int) entry.x()),
                String.valueOf((int) entry.y()),
                String.valueOf((int) entry.z()),
                String.format("%.1f", entry.radius()),
                entry.dimension().location().getPath()
            ), false);
        }

        return 1;
    }

    private static int executeSetInn(CommandSourceStack src, Vec3 coords, double radius) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        InnEntry entry = data.addInn(coords.x, coords.y, coords.z, radius, level.dimension());

        src.sendSuccess(() -> translatable(src,
            "msg.soulslikeregen.command.inn.set",
            String.valueOf(entry.id()),
            String.valueOf((int) coords.x),
            String.valueOf((int) coords.y),
            String.valueOf((int) coords.z),
            String.format("%.1f", radius)
        ), true);

        return 1;
    }

    private static int executeEditInnRadius(CommandSourceStack src, int id, double newRadius) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        if (data.updateInnRadius(id, newRadius)) {
            src.sendSuccess(() -> translatable(src,
                "msg.soulslikeregen.command.inn.edit_radius",
                String.valueOf(id),
                String.format("%.1f", newRadius)
            ), true);
            return 1;
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.inn.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeEditInnCoords(CommandSourceStack src, int id, Vec3 coords) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        if (data.updateInnCoords(id, coords.x, coords.y, coords.z)) {
            src.sendSuccess(() -> translatable(src,
                "msg.soulslikeregen.command.inn.edit_coords",
                String.valueOf(id),
                String.valueOf((int) coords.x),
                String.valueOf((int) coords.y),
                String.valueOf((int) coords.z)
            ), true);
            return 1;
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.inn.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeRemoveInn(CommandSourceStack src, int id) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        if (data.removeInn(id)) {
            src.sendSuccess(() -> translatable(src, "msg.soulslikeregen.command.inn.remove", String.valueOf(id)), true);
            return 1;
        } else {
            src.sendFailure(translatable(src, "msg.soulslikeregen.command.inn.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeListInns(CommandSourceStack src, int page) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        Collection<InnEntry> all = data.getAllInns();
        
        if (all.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No Inns configured."), false);
            return 1;
        }

        int pageSize = 5;
        int total = all.size();
        int maxPage = (int) Math.ceil((double) total / pageSize);
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        final int finalPage = page;
        final int finalMaxPage = maxPage;
        src.sendSuccess(() -> translatable(src, "msg.soulslikeregen.command.inn.list_title", String.valueOf(finalPage), String.valueOf(finalMaxPage)), false);

        List<InnEntry> list = new ArrayList<>(all);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            InnEntry entry = list.get(i);
            src.sendSuccess(() -> translatable(src,
                "msg.soulslikeregen.command.inn.list_item",
                String.valueOf(entry.id()),
                String.valueOf((int) entry.x()),
                String.valueOf((int) entry.y()),
                String.valueOf((int) entry.z()),
                String.format("%.1f", entry.radius()),
                entry.dimension().location().getPath()
            ), false);
        }

        return 1;
    }

    // ========== PLAYER STAT COMMANDS ==========

    private static int executePlayerFatigueGet(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float fatigue = cap.getCurrentFatigue();
            float maxCap = cap.getMaxCap();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] %s's fatigue: %.1f / %.1f", playerName, fatigue, maxCap
            )), false);
        });
        return 1;
    }

    private static int executePlayerFatigueSet(CommandSourceStack src, String playerName, float amount) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float maxCap = cap.getMaxCap();
            float clamped = Math.max(0, Math.min(amount, maxCap));
            cap.setCurrentFatigue(clamped);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Set %s's fatigue to %.1f / %.1f", playerName, clamped, maxCap
            )), false);
        });
        return 1;
    }

    private static int executePlayerFatigueAdd(CommandSourceStack src, String playerName, float amount) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float before = cap.getCurrentFatigue();
            float maxCap = cap.getMaxCap();
            float added = cap.addFatigue(amount);
            float after = cap.getCurrentFatigue();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Added %.1f fatigue to %s (%.1f → %.1f / %.1f)", added, playerName, before, after, maxCap
            )), false);
        });
        return 1;
    }

    private static int executePlayerFatigueDrain(CommandSourceStack src, String playerName, float amount) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float before = cap.getCurrentFatigue();
            float drained = cap.drainFatigue(amount);
            float after = cap.getCurrentFatigue();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Drained %.1f fatigue from %s (%.1f → %.1f)", drained, playerName, before, after
            )), false);
        });
        return 1;
    }

    private static int executePlayerCapacityGet(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float maxCap = cap.getMaxCap();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] %s's max capacity: %.1f", playerName, maxCap
            )), false);
        });
        return 1;
    }

    private static int executePlayerCapacitySet(CommandSourceStack src, String playerName, float amount) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float before = cap.getMaxCap();
            cap.setMaxCap(amount);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Set %s's max capacity from %.1f to %.1f", playerName, before, amount
            )), false);
        });
        return 1;
    }

    private static int executePlayerCapacityReset(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float before = cap.getMaxCap();
            cap.setMaxCap(RegenConfig.BASE_MAX_CAP);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Reset %s's max capacity from %.1f to BASE (%.1f)", playerName, before, RegenConfig.BASE_MAX_CAP
            )), false);
        });
        return 1;
    }

    private static int executePlayerLevelGet(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            int level = cap.getCurrentLevel();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] %s's level: %d", playerName, level
            )), false);
        });
        return 1;
    }

    private static int executePlayerLevelSet(CommandSourceStack src, String playerName, int level) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            int before = cap.getCurrentLevel();
            cap.setCurrentLevel(Math.max(0, level));
            int maxCap = (int) cap.getMaxCap();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Set %s's level from %d to %d (max capacity: %.1f)", playerName, before, level, cap.getMaxCap()
            )), false);
        });
        return 1;
    }

    private static int executePlayerLevelUp(CommandSourceStack src, String playerName, int amount) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            int before = cap.getCurrentLevel();
            int after = before + Math.max(1, amount);
            cap.setCurrentLevel(after);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Leveled up %s from level %d to %d (max capacity: %.1f)", playerName, before, after, cap.getMaxCap()
            )), false);
        });
        return 1;
    }

    private static int executePlayerLevelReset(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            int before = cap.getCurrentLevel();
            cap.setCurrentLevel(0);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Reset %s's level from %d to 0", playerName, before
            )), false);
        });
        return 1;
    }

    private static int executePlayerTotalFatigueGet(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            float totalFatigue = cap.getTotalFatigueSpent();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] %s's total fatigue spent: %.1f", playerName, totalFatigue
            )), false);
        });
        return 1;
    }

    private static int executePlayerCooldownDayBonusReset(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            cap.setBonusClaimed(false);
            cap.setLastDamageTick(-1L);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Reset %s's day bonus cooldown", playerName
            )), false);
        });
        return 1;
    }

    private static int executePlayerCooldownInnWarmupReset(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            cap.setInnWarmupTicks(0);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Reset %s's inn warmup timer", playerName
            )), false);
        });
        return 1;
    }

    private static int executePlayerStatus(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        RegenCapProvider.get(player).ifPresent(cap -> {
            src.sendSuccess(() -> Component.literal(String.format(
                "\n[SLRegen] === %s's Status ===\nFatigue: %.1f / %.1f\nMax Capacity: %.1f\nLevel: %d\nTotal Fatigue Spent: %.1f\nExhausted: %s",
                playerName,
                cap.getCurrentFatigue(),
                cap.getMaxCap(),
                cap.getMaxCap(),
                cap.getCurrentLevel(),
                cap.getTotalFatigueSpent(),
                cap.isExhausted() ? "YES" : "NO"
            )), false);
        });
        return 1;
    }

    private static int executePlayerReset(CommandSourceStack src, String playerName) {
        ServerPlayer player = findPlayer(src, playerName);
        if (player == null) return 0;

        FatigueResetEvent event = new FatigueResetEvent(player, FatigueResetEvent.ResetSource.COMMAND);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            src.sendFailure(Component.literal("[SLRegen] Reset was canceled by another mod."));
            return 0;
        }

        RegenCapProvider.get(player).ifPresent(cap -> {
            cap.setCurrentFatigue(0.0f);
            cap.setMaxCap(RegenConfig.BASE_MAX_CAP);
            cap.setCurrentLevel(0);
            // Note: totalFatigueSpent is NOT reset — it's cumulative progression tracking
            // If you want to reset that too, uncomment the next line:
            // cap.addFatigueSpent(-cap.getTotalFatigueSpent());
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] HARD RESET %s: fatigue=0, capacity=BASE (%.1f), level=0", 
                playerName, RegenConfig.BASE_MAX_CAP
            )), false);
        });
        return 1;
    }

    // ========== ACTIONBAR TOGGLE COMMANDS ==========

    private static int executeBarEnable(CommandSourceStack src) {
        ServerPlayer player = (ServerPlayer) src.getEntity();
        if (player == null) {
            src.sendFailure(Component.literal("[SLRegen] This command only works for players."));
            return 0;
        }

        RegenCapProvider.get(player).ifPresent(cap -> {
            cap.setActionBarEnabled(true);
            src.sendSuccess(() -> Component.literal("[SLRegen] Status bar ENABLED - you will see your regenerative capacity on the action bar."), false);
        });
        return 1;
    }

    private static int executeBarDisable(CommandSourceStack src) {
        ServerPlayer player = (ServerPlayer) src.getEntity();
        if (player == null) {
            src.sendFailure(Component.literal("[SLRegen] This command only works for players."));
            return 0;
        }

        RegenCapProvider.get(player).ifPresent(cap -> {
            cap.setActionBarEnabled(false);
            src.sendSuccess(() -> Component.literal("[SLRegen] Status bar DISABLED."), false);
        });
        return 1;
    }

    private static int executeBarStatus(CommandSourceStack src) {
        ServerPlayer player = (ServerPlayer) src.getEntity();
        if (player == null) {
            src.sendFailure(Component.literal("[SLRegen] This command only works for players."));
            return 0;
        }

        RegenCapProvider.get(player).ifPresent(cap -> {
            boolean enabled = cap.isActionBarEnabled();
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Status bar is currently: %s", enabled ? "ENABLED" : "DISABLED"
            )), false);
        });
        return 1;
    }

    // ========== HELPER METHODS ==========

    private static Component translatable(CommandSourceStack src, String key, Object... args) {
        ServerPlayer player = src.getEntity() instanceof ServerPlayer p ? p : null;
        return ServerTranslationHelper.getComponent(player, key, args);
    }

    private static ServerPlayer findPlayer(CommandSourceStack src, String playerName) {
        ServerPlayer player = src.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            src.sendFailure(Component.literal(String.format("[SLRegen] Player '%s' not found or not online.", playerName)));
            return null;
        }
        return player;
    }

    @SuppressWarnings("unchecked")
    private static ArgumentType<Object> getVec3Argument() {
        try {
            Class<?> clazz = Class.forName("net.minecraft.commands.arguments.coordinates.Vec3Argument");
            for (Method method : clazz.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 0 &&
                    ArgumentType.class.isAssignableFrom(method.getReturnType())) {
                    return (ArgumentType<Object>) method.invoke(null);
                }
            }
            throw new NoSuchMethodException("No static factory method returning ArgumentType found on Vec3Argument");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Vec3Argument via signature reflection", e);
        }
    }

    private static Vec3 getVec3Coord(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.commands.arguments.coordinates.Vec3Argument");
            for (Method method : clazz.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 2 &&
                    method.getParameterTypes()[0].equals(CommandContext.class) &&
                    method.getParameterTypes()[1].equals(String.class) &&
                    Vec3.class.isAssignableFrom(method.getReturnType())) {
                    return (Vec3) method.invoke(null, ctx, name);
                }
            }
            throw new NoSuchMethodException("No static retriever method returning Vec3 found on Vec3Argument");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Vec3 via signature reflection", e);
        }
    }
}
