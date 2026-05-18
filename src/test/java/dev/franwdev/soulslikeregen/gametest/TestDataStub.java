package dev.franwdev.soulslikeregen.gametest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Offline stub for FTBTeamsCompat when testing without FTB Teams installed.
 * Maintains an in-memory player→team mapping for test scenarios.
 */
public class TestDataStub {

    private static final Map<UUID, UUID> playerTeams = new HashMap<>();
    private static final Map<UUID, String> teamNames = new HashMap<>();

    /**
     * Reset all stub data.
     */
    public static void reset() {
        playerTeams.clear();
        teamNames.clear();
    }

    /**
     * Set a player's team assignment.
     */
    public static void setPlayerTeam(UUID playerId, UUID teamId, String teamName) {
        playerTeams.put(playerId, teamId);
        teamNames.put(teamId, teamName);
    }

    /**
     * Get a player's team ID.
     */
    public static Optional<UUID> getTeamId(UUID playerId) {
        return Optional.ofNullable(playerTeams.get(playerId));
    }

    /**
     * Get a team's name by ID.
     */
    public static Optional<String> getTeamName(UUID teamId) {
        return Optional.ofNullable(teamNames.get(teamId));
    }

    /**
     * Find a team ID by name (case-insensitive).
     */
    public static Optional<UUID> getTeamIdByName(String name) {
        for (Map.Entry<UUID, String> entry : teamNames.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(name)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * Remove a player from any team.
     */
    public static void removePlayer(UUID playerId) {
        playerTeams.remove(playerId);
    }
}
