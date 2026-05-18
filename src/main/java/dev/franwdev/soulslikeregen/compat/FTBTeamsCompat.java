package dev.franwdev.soulslikeregen.compat;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

public class FTBTeamsCompat {

    private static final String MOD_ID = "ftbteams";
    private static final boolean LOADED = ModList.get().isLoaded(MOD_ID);
    private static final boolean TEST_MODE = Boolean.getBoolean("soulslikeregen.testMode");
    private static final String TEST_STUB_CLASS = "dev.franwdev.soulslikeregen.gametest.TestDataStub";

    public static boolean isLoaded() {
        return LOADED || TEST_MODE;
    }

    public static void init() {
    }

    private static Object getApi() throws Exception {
        Class<?> apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
        Method apiMethod = apiClass.getMethod("api");
        return apiMethod.invoke(null);
    }

    public static UUID getPlayerTeamId(ServerPlayer player) {
        if (TEST_MODE) {
            Optional<UUID> testTeam = invokeTestUuid("getTeamId", UUID.class, player.getUUID());
            return testTeam.orElse(null);
        }
        if (!LOADED) return null;
        try {
            Object api = getApi();
            if (api == null) return null;
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            Method getTeamForPlayer = manager.getClass().getMethod("getTeamForPlayer", ServerPlayer.class);
            Optional<?> teamOpt = (Optional<?>) getTeamForPlayer.invoke(manager, player);
            if (teamOpt.isPresent()) {
                Object team = teamOpt.get();
                boolean isParty = (boolean) team.getClass().getMethod("isPartyTeam").invoke(team);
                if (isParty) {
                    Method getTeamId = team.getClass().getMethod("getTeamId");
                    return (UUID) getTeamId.invoke(team);
                }
            }
        } catch (Throwable e) {
            // Guard
        }
        return null;
    }

    public static UUID getTeamIdByName(String name) {
        if (TEST_MODE) {
            Optional<UUID> testTeam = invokeTestUuid("getTeamIdByName", String.class, name);
            return testTeam.orElse(null);
        }
        if (!LOADED) return null;
        try {
            Object api = getApi();
            if (api == null) return null;
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            Collection<?> teams = (Collection<?>) manager.getClass().getMethod("getTeams").invoke(manager);
            for (Object team : teams) {
                boolean isParty = (boolean) team.getClass().getMethod("isPartyTeam").invoke(team);
                if (!isParty) continue;
                
                String shortName = (String) team.getClass().getMethod("getShortName").invoke(team);
                net.minecraft.network.chat.Component displayName = (net.minecraft.network.chat.Component) team.getClass().getMethod("getName").invoke(team);
                String fullName = displayName.getString();
                
                if (shortName.equalsIgnoreCase(name) || fullName.equalsIgnoreCase(name)) {
                    Method getTeamId = team.getClass().getMethod("getTeamId");
                    return (UUID) getTeamId.invoke(team);
                }
            }
        } catch (Throwable e) {
            // Guard
        }
        return null;
    }

    public static String getTeamName(UUID teamId) {
        if (teamId == null) {
            return "Unknown";
        }
        if (TEST_MODE) {
            Optional<String> testName = invokeTestString("getTeamName", UUID.class, teamId);
            return testName.orElse("Unknown");
        }
        if (!LOADED) {
            return "Unknown";
        }
        try {
            Object api = getApi();
            if (api == null) return "Unknown";
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            Method getTeamByID = manager.getClass().getMethod("getTeamByID", UUID.class);
            Optional<?> teamOpt = (Optional<?>) getTeamByID.invoke(manager, teamId);
            if (teamOpt.isPresent()) {
                Object team = teamOpt.get();
                net.minecraft.network.chat.Component displayName = (net.minecraft.network.chat.Component) team.getClass().getMethod("getName").invoke(team);
                return displayName.getString();
            }
        } catch (Throwable e) {
            // Guard
        }
        return "Unknown";
    }

    public static boolean arePlayersAllies(ServerPlayer player1, ServerPlayer player2) {
        if (TEST_MODE) {
            UUID teamId1 = getPlayerTeamId(player1);
            UUID teamId2 = getPlayerTeamId(player2);
            return teamId1 != null && teamId1.equals(teamId2);
        }
        if (!LOADED) return false;
        try {
            UUID teamId1 = getPlayerTeamId(player1);
            UUID teamId2 = getPlayerTeamId(player2);
            return teamId1 != null && teamId1.equals(teamId2);
        } catch (Throwable e) {
            // Guard
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Optional<UUID> invokeTestUuid(String methodName, Class<?> paramType, Object arg) {
        try {
            Class<?> stubClass = Class.forName(TEST_STUB_CLASS);
            Method method = stubClass.getMethod(methodName, paramType);
            Object result = method.invoke(null, arg);
            if (result instanceof Optional<?>) {
                return (Optional<UUID>) result;
            }
        } catch (Throwable e) {
            // Guard
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static Optional<String> invokeTestString(String methodName, Class<?> paramType, Object arg) {
        try {
            Class<?> stubClass = Class.forName(TEST_STUB_CLASS);
            Method method = stubClass.getMethod(methodName, paramType);
            Object result = method.invoke(null, arg);
            if (result instanceof Optional<?>) {
                return (Optional<String>) result;
            }
        } catch (Throwable e) {
            // Guard
        }
        return Optional.empty();
    }
}
