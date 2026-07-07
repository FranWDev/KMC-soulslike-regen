package dev.franwdev.soulslikeregen.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import dev.franwdev.soulslikeregen.capability.IRegenCap;
import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.data.InnData;
import dev.franwdev.soulslikeregen.data.NexusData;
import dev.franwdev.soulslikeregen.event.PlayerTickHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.event.TickEvent;

/**
 * Shared test utilities for GameTest suite.
 * Provides zone registration, player creation, and assertion helpers.
 */
public class TestHelpers {

    public static final BlockPos ZONE_POS = new BlockPos(0, 64, 0);
    public static final BlockPos ALT_ZONE_POS = new BlockPos(100, 64, 100);
    public static final BlockPos FAR_POS = new BlockPos(1000, 64, 1000);

    /**
     * Creates a mock ServerPlayer with the given UUID and name.
     */
    public static ServerPlayer makePlayer(GameTestHelper helper, UUID uuid, String name) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        GameProfile profile = new GameProfile(uuid, name);
        ServerPlayer serverPlayer = new ServerPlayer(level.getServer(), level, profile) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        serverPlayer.connection = new TestNetHandler(level.getServer(), serverPlayer);
        level.addNewPlayer(serverPlayer);
        return serverPlayer;
    }

    private static final class TestNetHandler extends ServerGamePacketListenerImpl {
        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        private TestNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player);
        }

        @Override
        public void send(Packet<?> packet) {
            // No-op for GameTest.
        }

        @Override
        public void send(Packet<?> packet, PacketSendListener sendListener) {
            // No-op for GameTest.
        }

        @Override
        public void disconnect(Component message) {
            // No-op for GameTest.
        }
    }

    /**
     * Registers a Nexus zone in the test world.
     */
    public static void registerNexus(GameTestHelper helper, BlockPos pos, float radius, 
                                     UUID teamId, String teamName) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        NexusData nexusData = NexusData.get(level);
        nexusData.addNexus(pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension(), teamId, teamName);
    }

    /**
     * Registers an Inn zone in the test world.
     */
    public static void registerInn(GameTestHelper helper, BlockPos pos, float radius) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        InnData innData = InnData.get(level);
        innData.addInn("test_inn", pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension());
    }

    /**
     * Gets the capability for a player, throwing if not attached.
     */
    public static IRegenCap getCap(ServerPlayer player) {
        return RegenCapProvider.get(player)
            .orElseThrow(() -> new AssertionError("Player has no RegenCap!"));
    }

    /**
     * Simulates server ticks for a player and runs the tick handler.
     */
    public static void tickPlayer(ServerPlayer player, int ticks) {
        for (int i = 0; i < ticks; i++) {
            player.tickCount++;
            PlayerTickHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
        }
    }

    /**
     * Simulates player damage (updates lastDamageTick).
     */
    public static void simulateDamage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        IRegenCap cap = getCap(player);
        cap.setLastDamageTick(level.getGameTime());
    }

    /**
     * Simulates player healing by a specific amount.
     */
    public static void simulateHealing(ServerPlayer player, float health) {
        float newHealth = Math.min(player.getMaxHealth(), player.getHealth() + health);
        player.setHealth(newHealth);
    }

    /**
     * Assertion: fatigue is within expected range.
     */
    public static void assertFatigueNear(GameTestHelper helper, IRegenCap cap, 
                                         float expected, float tolerance) {
        float actual = cap.getCurrentFatigue();
        if (Math.abs(actual - expected) > tolerance) {
            helper.fail("Expected fatigue ~" + expected + " ±" + tolerance + 
                       ", got " + actual);
        }
    }

    /**
     * Assertion: maxCap is at specific level.
     */
    public static void assertMaxCap(GameTestHelper helper, IRegenCap cap, float expected) {
        if (Math.abs(cap.getMaxCap() - expected) > 0.01f) {
            helper.fail("Expected maxCap=" + expected + ", got " + cap.getMaxCap());
        }
    }

    /**
     * Assertion: player is in Nexus (drain active).
     */
    public static void assertNexoActive(GameTestHelper helper, IRegenCap cap) {
        if (!cap.isNexoDrainActive()) {
            helper.fail("Expected Nexo drain to be active, but it is not");
        }
    }

    /**
     * Assertion: player is NOT in Nexus (drain inactive).
     */
    public static void assertNexoInactive(GameTestHelper helper, IRegenCap cap) {
        if (cap.isNexoDrainActive()) {
            helper.fail("Expected Nexo drain to be inactive, but it is active");
        }
    }

    /**
     * Assertion: Inn drain is active.
     */
    public static void assertInnActive(GameTestHelper helper, IRegenCap cap) {
        if (!cap.isInnDrainActive()) {
            helper.fail("Expected Inn drain to be active, but it is not");
        }
    }

    /**
     * Assertion: Inn drain is inactive.
     */
    public static void assertInnInactive(GameTestHelper helper, IRegenCap cap) {
        if (cap.isInnDrainActive()) {
            helper.fail("Expected Inn drain to be inactive, but it is active");
        }
    }

    /**
     * Assertion: value is true.
     */
    public static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    /**
     * Assertion: value is false.
     */
    public static void assertFalse(GameTestHelper helper, boolean condition, String message) {
        if (condition) {
            helper.fail(message);
        }
    }

    /**
     * Assertion: two floats are equal within tolerance.
     */
    public static void assertEquals(GameTestHelper helper, float expected, float actual, float tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            helper.fail(message + "; expected " + expected + ", got " + actual);
        }
    }
}
