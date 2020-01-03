package com.github.intellectualsites.plotsquared.plot.object;

public class BlockLoc {

    private final int x;
    private final int y;
    private final int z;

    private final float yaw;
    private final float pitch;

    public BlockLoc(int x, int y, int z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public BlockLoc(int x, int y, int z) {
        this(x, y, z, 0f, 0f);
    }

    public static BlockLoc fromString(String string) {
        String[] parts = string.split(",");

        float yaw;
        float pitch;
        if (parts.length == 5) {
            yaw = Float.parseFloat(parts[3]);
            pitch = Float.parseFloat(parts[4]);
        } else {
            return new BlockLoc(0, 0, 0);
        }
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);

        return new BlockLoc(x, y, z, yaw, pitch);
    }

    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.getX();
        result = prime * result + this.getY();
        result = prime * result + this.getZ();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return this.getX() == 0 && this.getY() == 0 && this.getZ() == 0;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlockLoc other = (BlockLoc) obj;
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other
            .getZ();
    }

    @Override public String toString() {
        if (this.getX() == 0 && this.getY() == 0 && this.getZ() == 0) {
            return "";
        }
        return this.getX() + "," + this.getY() + ',' + this.getZ() + ',' + this.getYaw()
            + ',' + this.getPitch();

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
