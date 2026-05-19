package dev.franwdev.soulslikeregen.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.compat.FTBTeamsCompat;
import dev.franwdev.soulslikeregen.data.InnData;
import dev.franwdev.soulslikeregen.data.InnEntry;
import dev.franwdev.soulslikeregen.data.NexusData;
import dev.franwdev.soulslikeregen.data.NexusEntry;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;

public class SoulslikeRegenCommand {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> slRoot = buildCommandTree("soulslikeregen");
        LiteralArgumentBuilder<CommandSourceStack> slAlias = buildCommandTree("slregen");

        dispatcher.register(slRoot);
        dispatcher.register(slAlias);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommandTree(String name) {
        return Commands.literal(name)
            .then(Commands.literal("status")
                .executes(ctx -> executeStatus(ctx.getSource())))
            
            // --- Nexus Commands (Admin: Permission level 2) ---
            .then(Commands.literal("setTeamNexus")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("coords", Vec3Argument.vec3())
                    .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.1))
                        .then(Commands.argument("teamName", StringArgumentType.string())
                            .executes(ctx -> executeSetNexus(
                                ctx.getSource(),
                                Vec3Argument.getVec3(ctx, "coords"),
                                DoubleArgumentType.getDouble(ctx, "radius"),
                                StringArgumentType.getString(ctx, "teamName")
                            ))
                        )
                    )
                )
            )
            .then(Commands.literal("editTeamNexus")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .then(Commands.literal("radius")
                        .then(Commands.argument("newRadius", DoubleArgumentType.doubleArg(0.1))
                            .executes(ctx -> executeEditNexusRadius(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                DoubleArgumentType.getDouble(ctx, "newRadius")
                            ))
                        )
                    )
                    .then(Commands.literal("coords")
                        .then(Commands.argument("coords", Vec3Argument.vec3())
                            .executes(ctx -> executeEditNexusCoords(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                Vec3Argument.getVec3(ctx, "coords")
                            ))
                        )
                    )
                )
            )
            .then(Commands.literal("removeTeamNexus")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeRemoveNexus(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))
                )
            )
            .then(Commands.literal("listNexus")
                .executes(ctx -> executeListNexus(ctx.getSource(), 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeListNexus(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                )
            )

            // --- Inn Commands (Admin: Permission level 2) ---
            .then(Commands.literal("setInn")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("coords", Vec3Argument.vec3())
                    .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.1))
                        .executes(ctx -> executeSetInn(
                            ctx.getSource(),
                            Vec3Argument.getVec3(ctx, "coords"),
                            DoubleArgumentType.getDouble(ctx, "radius")
                        ))
                    )
                )
            )
            .then(Commands.literal("editInn")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .then(Commands.literal("radius")
                        .then(Commands.argument("newRadius", DoubleArgumentType.doubleArg(0.1))
                            .executes(ctx -> executeEditInnRadius(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                DoubleArgumentType.getDouble(ctx, "newRadius")
                            ))
                        )
                    )
                    .then(Commands.literal("coords")
                        .then(Commands.argument("coords", Vec3Argument.vec3())
                            .executes(ctx -> executeEditInnCoords(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "id"),
                                Vec3Argument.getVec3(ctx, "coords")
                            ))
                        )
                    )
                )
            )
            .then(Commands.literal("removeInn")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeRemoveInn(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))
                )
            )
            .then(Commands.literal("listInns")
                .executes(ctx -> executeListInns(ctx.getSource(), 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> executeListInns(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                )
            )
            
            // --- Player Stat Commands (Admin: Permission level 2) ---
            .then(Commands.literal("player")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("playerName", StringArgumentType.string())
                    // Player Fatigue Commands
                    .then(Commands.literal("fatigue")
                        .then(Commands.literal("get")
                            .executes(ctx -> executePlayerFatigueGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                        .then(Commands.literal("set")
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> executePlayerFatigueSet(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                        .then(Commands.literal("add")
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                                .executes(ctx -> executePlayerFatigueAdd(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                        .then(Commands.literal("drain")
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                                .executes(ctx -> executePlayerFatigueDrain(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                    )
                    // Player Capacity Commands
                    .then(Commands.literal("capacity")
                        .then(Commands.literal("get")
                            .executes(ctx -> executePlayerCapacityGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                        .then(Commands.literal("set")
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                                .executes(ctx -> executePlayerCapacitySet(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    (float) DoubleArgumentType.getDouble(ctx, "amount")
                                ))
                            )
                        )
                        .then(Commands.literal("reset")
                            .executes(ctx -> executePlayerCapacityReset(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                    )
                    // Player Level Commands
                    .then(Commands.literal("level")
                        .then(Commands.literal("get")
                            .executes(ctx -> executePlayerLevelGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                        .then(Commands.literal("set")
                            .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(ctx -> executePlayerLevelSet(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    IntegerArgumentType.getInteger(ctx, "level")
                                ))
                            )
                        )
                        .then(Commands.literal("up")
                            .executes(ctx -> executePlayerLevelUp(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName"),
                                1
                            ))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> executePlayerLevelUp(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName"),
                                    IntegerArgumentType.getInteger(ctx, "amount")
                                ))
                            )
                        )
                        .then(Commands.literal("reset")
                            .executes(ctx -> executePlayerLevelReset(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                    )
                    // Player TotalFatigue Command
                    .then(Commands.literal("totalfatigue")
                        .then(Commands.literal("get")
                            .executes(ctx -> executePlayerTotalFatigueGet(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "playerName")
                            ))
                        )
                    )
                    // Player Cooldown Commands
                    .then(Commands.literal("cooldown")
                        .then(Commands.literal("daybonus")
                            .then(Commands.literal("reset")
                                .executes(ctx -> executePlayerCooldownDayBonusReset(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName")
                                ))
                            )
                        )
                        .then(Commands.literal("innwarmup")
                            .then(Commands.literal("reset")
                                .executes(ctx -> executePlayerCooldownInnWarmupReset(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "playerName")
                                ))
                            )
                        )
                    )
                    // Player Status Command
                    .then(Commands.literal("status")
                        .executes(ctx -> executePlayerStatus(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "playerName")
                        ))
                    )
                    // Player Reset Command (HARD RESET)
                    .then(Commands.literal("reset")
                        .executes(ctx -> executePlayerReset(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "playerName")
                        ))
                    )
                )
            )
            
            // --- ActionBar Toggle Commands (no permission requirement) ---
            .then(Commands.literal("bar")
                .then(Commands.literal("on")
                    .executes(ctx -> executeBarEnable(ctx.getSource()))
                )
                .then(Commands.literal("off")
                    .executes(ctx -> executeBarDisable(ctx.getSource()))
                )
                .then(Commands.literal("status")
                    .executes(ctx -> executeBarStatus(ctx.getSource()))
                )
            );
    }

    private static int executeStatus(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer player) {
            RegenCapProvider.get(player).ifPresent(cap -> {
                Component bar = FeedbackHelper.buildStatusBar(cap);
                player.connection.send(new ClientboundSetActionBarTextPacket(bar));
            });
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.only_player"));
        }
        return 1;
    }

    private static int executeSetNexus(CommandSourceStack src, Vec3 coords, double radius, String teamName) {
        ServerLevel level = src.getLevel();
        
        UUID teamId = FTBTeamsCompat.getTeamIdByName(teamName);
        if (FTBTeamsCompat.isLoaded() && teamId == null) {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.nexus.team_not_found", teamName));
            return 0;
        }
        if (teamId == null) {
            teamId = new UUID(0L, 0L);
        }

        NexusData data = NexusData.get(level);
        NexusEntry entry = data.addNexus(coords.x, coords.y, coords.z, radius, level.dimension(), teamId, teamName);

        src.sendSuccess(() -> Component.translatable(
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
            src.sendSuccess(() -> Component.translatable(
                "msg.soulslikeregen.command.nexus.edit_radius",
                String.valueOf(id),
                String.format("%.1f", newRadius)
            ), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.nexus.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeEditNexusCoords(CommandSourceStack src, int id, Vec3 coords) {
        ServerLevel level = src.getLevel();
        NexusData data = NexusData.get(level);
        if (data.updateNexusCoords(id, coords.x, coords.y, coords.z)) {
            src.sendSuccess(() -> Component.translatable(
                "msg.soulslikeregen.command.nexus.edit_coords",
                String.valueOf(id),
                String.valueOf((int) coords.x),
                String.valueOf((int) coords.y),
                String.valueOf((int) coords.z)
            ), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.nexus.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeRemoveNexus(CommandSourceStack src, int id) {
        ServerLevel level = src.getLevel();
        NexusData data = NexusData.get(level);
        if (data.removeNexus(id)) {
            src.sendSuccess(() -> Component.translatable("msg.soulslikeregen.command.nexus.remove", String.valueOf(id)), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.nexus.not_found", String.valueOf(id)));
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
        src.sendSuccess(() -> Component.translatable("msg.soulslikeregen.command.nexus.list_title", String.valueOf(finalPage), String.valueOf(finalMaxPage)), false);

        List<NexusEntry> list = new ArrayList<>(all);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            NexusEntry entry = list.get(i);
            src.sendSuccess(() -> Component.translatable(
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

        src.sendSuccess(() -> Component.translatable(
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
            src.sendSuccess(() -> Component.translatable(
                "msg.soulslikeregen.command.inn.edit_radius",
                String.valueOf(id),
                String.format("%.1f", newRadius)
            ), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.inn.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeEditInnCoords(CommandSourceStack src, int id, Vec3 coords) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        if (data.updateInnCoords(id, coords.x, coords.y, coords.z)) {
            src.sendSuccess(() -> Component.translatable(
                "msg.soulslikeregen.command.inn.edit_coords",
                String.valueOf(id),
                String.valueOf((int) coords.x),
                String.valueOf((int) coords.y),
                String.valueOf((int) coords.z)
            ), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.inn.not_found", String.valueOf(id)));
            return 0;
        }
    }

    private static int executeRemoveInn(CommandSourceStack src, int id) {
        ServerLevel level = src.getLevel();
        InnData data = InnData.get(level);
        if (data.removeInn(id)) {
            src.sendSuccess(() -> Component.translatable("msg.soulslikeregen.command.inn.remove", String.valueOf(id)), true);
            return 1;
        } else {
            src.sendFailure(Component.translatable("msg.soulslikeregen.command.inn.not_found", String.valueOf(id)));
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
        src.sendSuccess(() -> Component.translatable("msg.soulslikeregen.command.inn.list_title", String.valueOf(finalPage), String.valueOf(finalMaxPage)), false);

        List<InnEntry> list = new ArrayList<>(all);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            InnEntry entry = list.get(i);
            src.sendSuccess(() -> Component.translatable(
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
            cap.setMaxCap(dev.franwdev.soulslikeregen.config.RegenConfig.BASE_MAX_CAP);
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] Reset %s's max capacity from %.1f to BASE (%.1f)", playerName, before, dev.franwdev.soulslikeregen.config.RegenConfig.BASE_MAX_CAP
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

        RegenCapProvider.get(player).ifPresent(cap -> {
            cap.setCurrentFatigue(0.0f);
            cap.setMaxCap(dev.franwdev.soulslikeregen.config.RegenConfig.BASE_MAX_CAP);
            cap.setCurrentLevel(0);
            // Note: totalFatigueSpent is NOT reset — it's cumulative progression tracking
            // If you want to reset that too, uncomment the next line:
            // cap.addFatigueSpent(-cap.getTotalFatigueSpent());
            src.sendSuccess(() -> Component.literal(String.format(
                "[SLRegen] HARD RESET %s: fatigue=0, capacity=BASE (%.1f), level=0", 
                playerName, dev.franwdev.soulslikeregen.config.RegenConfig.BASE_MAX_CAP
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

    private static ServerPlayer findPlayer(CommandSourceStack src, String playerName) {
        ServerPlayer player = src.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            src.sendFailure(Component.literal(String.format("[SLRegen] Player '%s' not found or not online.", playerName)));
            return null;
        }
        return player;
    }
}
