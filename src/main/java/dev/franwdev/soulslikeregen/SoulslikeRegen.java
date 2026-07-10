package dev.franwdev.soulslikeregen;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.command.SoulslikeRegenCommand;
import dev.franwdev.soulslikeregen.compat.FTBTeamsCompat;
import dev.franwdev.soulslikeregen.compat.WaystonesCompat;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.config.SoulslikeRegenClientConfig;
import dev.franwdev.soulslikeregen.client.ClientForgeEventHandler;
import dev.franwdev.soulslikeregen.event.DamageHandler;
import dev.franwdev.soulslikeregen.event.PlayerTickHandler;
import dev.franwdev.soulslikeregen.event.ServerEventHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(SoulslikeRegen.MODID)
public class SoulslikeRegen {

    public static final String MODID = "soulslikeregen";

    public SoulslikeRegen() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RegenConfig.SPEC, "soulslikeregen-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SoulslikeRegenClientConfig.SPEC, "soulslikeregen-client.toml");

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

        // Register capability events on mod bus
        modBus.addListener(RegenCapProvider::onRegisterCapabilities);
        
        // Register common setup for network initialization
        modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) -> {
            event.enqueueWork(dev.franwdev.soulslikeregen.network.SoulslikeRegenNetwork::init);
        });

        // Register game events on forge bus
        MinecraftForge.EVENT_BUS.register(new PlayerTickHandler());
        MinecraftForge.EVENT_BUS.register(new DamageHandler());
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
        MinecraftForge.EVENT_BUS.register(new RegenCapProvider());  // AttachCapabilitiesEvent

        // Register commands
        MinecraftForge.EVENT_BUS.addListener(SoulslikeRegenCommand::onRegisterCommands);

        // Register client commands only on physical client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ClientForgeEventHandler.class);
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
