package dev.franwdev.soulslikeregen.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fired when a player's fatigue is about to be reset (e.g., by a Waystone).
 * Canceling this event prevents the fatigue from being reset.
 */
@Cancelable
public class FatigueResetEvent extends PlayerEvent {

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
