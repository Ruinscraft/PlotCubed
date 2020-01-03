// PlotCubed start
package com.github.intellectualsites.plotsquared.plot.object;

public class PlotWarp {

    public final String name;
    public final BlockLoc blockLoc;

    public PlotWarp(String name, BlockLoc blockLoc) {
        this.name = name;
        this.blockLoc = blockLoc;
    }

    public Location getLocation(Plot plot) {
        int x;
        int z;
        int y;
        float yaw;
        float pitch;

        Location bottom = plot.getBottomAbs();

        x = bottom.getX() + blockLoc.getX();
        z = bottom.getZ() + blockLoc.getZ();
        y = blockLoc.getY();
        yaw = blockLoc.getYaw();
        pitch = blockLoc.getPitch();

        return new Location(plot.getWorldName(), x, y, z, yaw, pitch);
    }
}
// PlotCubed end