package dev.kmc.soulslikeregen.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

public class SoulslikeRegenCommand {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Main command: /soulslikeregen status (available to all players, permission level 0)
        dispatcher.register(
            Commands.literal("soulslikeregen").then(
                Commands.literal("status")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        if (src.getEntity() instanceof ServerPlayer player) {
                            // Send simulated action bar feedback for Phase 1
                            player.connection.send(
                                new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                                    Component.literal("Regen: [██░░░░░░░░ 8/40] (Placeholder - Phase 1)")
                                        .withStyle(net.minecraft.ChatFormatting.AQUA)
                                )
                            );
                        } else {
                            src.sendFailure(Component.literal("Only players can execute this command."));
                        }
                        return 1;
                    })
            )
        );

        // Alias command: /slregen status (available to all players)
        dispatcher.register(
            Commands.literal("slregen").then(
                Commands.literal("status")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        if (src.getEntity() instanceof ServerPlayer player) {
                            player.connection.send(
                                new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                                    Component.literal("Regen: [██░░░░░░░░ 8/40] (Placeholder - Phase 1)")
                                        .withStyle(net.minecraft.ChatFormatting.AQUA)
                                )
                            );
                        } else {
                            src.sendFailure(Component.literal("Only players can execute this command."));
                        }
                        return 1;
                    })
            )
        );
    }
}
