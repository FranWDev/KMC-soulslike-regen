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
}
