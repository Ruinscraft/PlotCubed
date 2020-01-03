package com.github.intellectualsites.plotsquared.nukkit.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.BlockVector2;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;

public class NukkitChunkManager extends ChunkManager {
    public NukkitChunkManager() {
        PlotSquared.debug("Not implemented: NukkitChunkManager");
    }

    @Override public int[] countEntities(Plot plot) {
        return new int[0];
    }

    @Override public boolean loadChunk(String world, BlockVector2 loc, boolean force) {
        return true;
    }

    @Override public void unloadChunk(String world, BlockVector2 loc, boolean save, boolean safe) {

    }

    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos, Runnable whenDone) {
        return false;
    }

    @Override public boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment,
        Runnable whenDone) {
        return false;
    }

    @Override public void clearAllEntities(Location pos1, Location pos2) {

    }

    @Override public void swap(Location bot1, Location top1, Location bot2, Location top2,
        Runnable whenDone) {
        whenDone.run();
    }
}
