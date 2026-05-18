package dev.kmc.soulslikeregen.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.kmc.soulslikeregen.capability.RegenCapProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

public class SoulslikeRegenCommand {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("soulslikeregen").then(
                Commands.literal("status")
                    .executes(ctx -> executeStatus(ctx.getSource()))
            )
        );

        dispatcher.register(
            Commands.literal("slregen").then(
                Commands.literal("status")
                    .executes(ctx -> executeStatus(ctx.getSource()))
            )
        );
    }

    private static int executeStatus(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer player) {
            RegenCapProvider.get(player).ifPresent(cap -> {
                net.minecraft.network.chat.Component bar = dev.kmc.soulslikeregen.feedback.FeedbackHelper.buildStatusBar(cap);
                player.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(bar)
                );
            });
        } else {
            src.sendFailure(Component.literal("Only players can execute this command."));
        }
        return 1;
    }
}
