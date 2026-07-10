package dev.franwdev.soulslikeregen.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientForgeEventHandler {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("slclient")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("hud")
                    .executes(ctx -> {
                        // Open HudPositionScreen on render thread
                        Minecraft.getInstance().tell(() -> {
                            Minecraft.getInstance().setScreen(new HudPositionScreen());
                        });
                        return 1;
                    })
                )
        );
    }
}
