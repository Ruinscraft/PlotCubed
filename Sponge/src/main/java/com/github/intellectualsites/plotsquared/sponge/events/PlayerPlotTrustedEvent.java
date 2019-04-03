package com.github.intellectualsites.plotsquared.sponge.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class PlayerPlotTrustedEvent extends PlotEvent {

    private final Player initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotTrustedEvent: Called when a plot trusted user is added/removed
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was added/removed from the trusted list
     * @param added     true of the player was added, false if the player was removed
     */
    public PlayerPlotTrustedEvent(final Player initiator, final Plot plot, final UUID player,
        final boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    /**
     * If a user was added
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return added;
    }

    /**
     * The player added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return player;
    }

    /**
     * The player initiating the action
     *
     * @return Player
     */
    public Player getInitiator() {
        return initiator;
    }
}
