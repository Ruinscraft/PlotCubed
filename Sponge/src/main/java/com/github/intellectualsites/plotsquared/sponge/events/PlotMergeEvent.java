package com.github.intellectualsites.plotsquared.sponge.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

import java.util.ArrayList;

public class PlotMergeEvent extends AbstractEvent implements Cancellable {
    private final ArrayList<PlotId> plots;
    private boolean cancelled;
    private Plot plot;
    private World world;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotMergeEvent(final World world, final Plot plot, final ArrayList<PlotId> plots) {
        this.plots = plots;
    }

    /**
     * Get the plots being added;
     *
     * @return Plot
     */
    public ArrayList<PlotId> getPlots() {
        return plots;
    }

    /**
     * Get the main plot
     *
     * @return Plot
     */
    public Plot getPlot() {
        return plot;
    }

    public World getWorld() {
        return world;
    }

    @Override public boolean isCancelled() {
        return cancelled;
    }

    @Override public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    @Override public Cause getCause() {
        return null;
    }
}
