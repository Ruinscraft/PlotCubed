package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import lombok.Getter;
import lombok.Setter;

public class Location implements Cloneable, Comparable<Location> {

    private int x;
    private int y;
    private int z;
    @Getter @Setter private float yaw;
    @Getter @Setter private float pitch;
    @Getter @Setter private String world;
    @Getter private BlockVector3 blockVector3;

    public Location(String world, int x, int y, int z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public Location(String world, int x, int y, int z) {
        this(world, x, y, z, 0f, 0f);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public void setX(int x) {
        this.x = x;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public void setY(int y) {
        this.y = y;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public void setZ(int z) {
        this.z = z;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public void setBlockVector3(BlockVector3 blockVector3) {
        this.blockVector3 = blockVector3;
        this.x = blockVector3.getX();
        this.y = blockVector3.getY();
        this.z = blockVector3.getZ();
    }

    @Override
    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); //can't happen
        }
    }

    public PlotArea getPlotArea() {
        return PlotSquared.get().getPlotAreaAbs(this);
    }

    public Plot getOwnedPlot() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getOwnedPlot(this);
        } else {
            return null;
        }
    }

    public Plot getOwnedPlotAbs() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getOwnedPlotAbs(this);
        } else {
            return null;
        }
    }

    public boolean isPlotArea() {
        return getPlotArea() != null;
    }

    public boolean isPlotRoad() {
        PlotArea area = getPlotArea();
        return area != null && area.getPlotAbs(this) == null;
    }

    /**
     * Checks if anyone owns a plot at the current location.
     * @return true if the location is a road, not a plot area, or if the plot is unclaimed.
     */
    public boolean isUnownedPlotArea() {
        PlotArea area = getPlotArea();
        return area != null && area.getOwnedPlotAbs(this) == null;
    }

    public PlotManager getPlotManager() {
        PlotArea pa = getPlotArea();
        if (pa != null) {
            return pa.getPlotManager();
        } else {
            return null;
        }
    }

    public Plot getPlotAbs() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getPlotAbs(this);
        } else {
            return null;
        }
    }

    public Plot getPlot() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getPlot(this);
        } else {
            return null;
        }
    }

    public BlockVector2 getBlockVector2() {
        return BlockVector2.at(this.x >> 4, this.z >> 4);
    }

    public Location add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public double getEuclideanDistanceSquared(Location l2) {
        double x = getX() - l2.getX();
        double y = getY() - l2.getY();
        double z = getZ() - l2.getZ();
        return x * x + y * y + z * z;
    }

    public double getEuclideanDistance(Location l2) {
        return Math.sqrt(getEuclideanDistanceSquared(l2));
    }

    public boolean isInSphere(Location origin, int radius) {
        return getEuclideanDistanceSquared(origin) < radius * radius;
    }

    @Override public int hashCode() {
        return MathMan.pair((short) this.x, (short) this.z) * 17 + this.y;
    }

    public boolean isInAABB(Location min, Location max) {
        return this.x >= min.getX() && this.x <= max.getX() && this.y >= min.getY() && this.y <= max
            .getY() && this.z >= min.getX() && this.z < max.getZ();
    }

    public void lookTowards(int x, int y) {
        double l = this.x - x;
        double c = Math.sqrt(l * l + 0.0);
        if (Math.asin(0 / c) / Math.PI * 180 > 90) {
            setYaw((float) (180 - -Math.asin(l / c) / Math.PI * 180));
        } else {
            setYaw((float) (-Math.asin(l / c) / Math.PI * 180));
        }
    }

    public Location subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    @Override public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        Location l = (Location) o;
        return this.x == l.getX() && this.y == l.getY() && this.z == l.getZ() && this.world
            .equals(l.getWorld()) && this.yaw == l.getYaw() && this.pitch == l.getPitch();
    }

    @Override public int compareTo(Location o) {
        if (this.x == o.getX() && this.y == o.getY() || this.z == o.getZ()) {
            return 0;
        }
        if (this.x < o.getX() && this.y < o.getY() && this.z < o.getZ()) {
            return -1;
        }
        return 1;
    }

    @Override public String toString() {
        return "\"plotsquaredlocation\":{\"x\":" + this.x + ",\"y\":" + this.y + ",\"z\":" + this.z
            + ",\"yaw\":" + this.yaw + ",\"pitch\":" + this.pitch + ",\"world\":\"" + this.world
            + "\"}";
    }
}
