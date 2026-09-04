package dev.franwdev.soulslikeregen.api.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Fired when a player's fatigue is about to be reset (e.g., by a Waystone).
 * Canceling this event prevents the fatigue from being reset.
 */
public class FatigueResetEvent extends PlayerEvent implements ICancellableEvent {

    private final ResetSource source;

    public FatigueResetEvent(Player player, ResetSource source) {
        super(player);
        this.source = source;
    }

    public ResetSource getSource() {
        return source;
    }

    public enum ResetSource {
        WAYSTONE,
        COMMAND
    }
}
