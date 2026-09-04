package dev.franwdev.soulslikeregen;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.client.ClientForgeEventHandler;
import dev.franwdev.soulslikeregen.command.SoulslikeRegenCommand;
import dev.franwdev.soulslikeregen.compat.FTBTeamsCompat;
import dev.franwdev.soulslikeregen.compat.WaystonesCompat;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.config.SoulslikeRegenClientConfig;
import dev.franwdev.soulslikeregen.event.DamageHandler;
import dev.franwdev.soulslikeregen.event.PlayerTickHandler;
import dev.franwdev.soulslikeregen.event.ServerEventHandler;
import dev.franwdev.soulslikeregen.network.SoulslikeRegenNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SoulslikeRegen.MODID)
public class SoulslikeRegen {

    public static final String MODID = "soulslikeregen";

    public SoulslikeRegen(IEventBus modBus, ModContainer modContainer) {
        // Register configs
        modContainer.registerConfig(ModConfig.Type.COMMON, RegenConfig.SPEC, "soulslikeregen-common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, SoulslikeRegenClientConfig.SPEC, "soulslikeregen-client.toml");

        // Bake config values on loading and reloading events
        modBus.addListener((ModConfigEvent.Loading event) -> {
            if (event.getConfig().getSpec() == RegenConfig.SPEC) {
                RegenConfig.bake();
            }
        });
        modBus.addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == RegenConfig.SPEC) {
                RegenConfig.bake();
            }
        });

        // Register attachment types on mod bus
        RegenCapProvider.ATTACHMENT_TYPES.register(modBus);

        // Register network payloads on mod bus
        modBus.addListener(SoulslikeRegenNetwork::onRegisterPayloadHandlers);

        // Register game events on NeoForge bus
        NeoForge.EVENT_BUS.register(PlayerTickHandler.class);
        NeoForge.EVENT_BUS.register(DamageHandler.class);
        NeoForge.EVENT_BUS.register(ServerEventHandler.class);

        // Register commands on NeoForge bus
        NeoForge.EVENT_BUS.addListener(SoulslikeRegenCommand::onRegisterCommands);

        // Register client commands only on physical client
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(ClientForgeEventHandler.class);
        }

        // Optional integrations — guard with isLoaded checks
        if (FTBTeamsCompat.isLoaded()) {
            FTBTeamsCompat.init();
        }
        if (WaystonesCompat.isLoaded()) {
            WaystonesCompat.init();
        }
    }
}
