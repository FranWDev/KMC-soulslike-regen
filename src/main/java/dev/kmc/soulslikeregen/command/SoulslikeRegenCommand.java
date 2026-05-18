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
                float fatigue = cap.getCurrentFatigue();
                float max = cap.getMaxCap();
                
                // Construct a simple progress bar
                int totalBars = 10;
                int filledBars = Math.round((fatigue / max) * totalBars);
                filledBars = Math.min(totalBars, Math.max(0, filledBars));
                
                StringBuilder bar = new StringBuilder();
                for (int i = 0; i < totalBars; i++) {
                    bar.append(i < filledBars ? "█" : "░");
                }
                
                String text = String.format("Regen: [%s %.1f/%.1f]", bar.toString(), fatigue, max);
                
                player.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                        Component.literal(text).withStyle(net.minecraft.ChatFormatting.AQUA)
                    )
                );
            });
        } else {
            src.sendFailure(Component.literal("Only players can execute this command."));
        }
        return 1;
    }
}
