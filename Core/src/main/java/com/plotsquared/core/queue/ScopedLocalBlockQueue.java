/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2021 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.queue;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.util.task.RunnableVal3;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

public class ScopedLocalBlockQueue extends DelegateLocalBlockQueue {
    private final int minX;
    private final int minY;
    private final int minZ;

    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private final int dx;
    private final int dy;
    private final int dz;

    public ScopedLocalBlockQueue(LocalBlockQueue parent, Location min, Location max) {
        super(parent);
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();

        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();

        this.dx = maxX - minX;
        this.dy = maxY - minY;
        this.dz = maxZ - minZ;
    }

    @Override public boolean setBiome(int x, int z, BiomeType biome) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBiome(x + minX, z + minZ, biome);
    }

    public void fillBiome(BiomeType biome) {
        for (int x = 0; x <= dx; x++) {
            for (int z = 0; z < dz; z++) {
                setBiome(x, z, biome);
            }
        }
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        return x >= 0 && x <= dx && y >= 0 && y <= dy && z >= 0 && z <= dz && super
            .setBlock(x + minX, y + minY, z + minZ, id);
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        return x >= 0 && x <= dx && y >= 0 && y <= dy && z >= 0 && z <= dz && super
            .setBlock(x + minX, y + minY, z + minZ, id);
    }

    @Override public boolean setBlock(int x, int y, int z, Pattern pattern) {
        return x >= 0 && x <= dx && y >= 0 && y <= dy && z >= 0 && z <= dz && super
            .setBlock(x + minX, y + minY, z + minZ, pattern);
    }

    public Location getMin() {
        return new Location(getWorld(), minX, minY, minZ);
    }

    public Location getMax() {
        return new Location(getWorld(), maxX, maxY, maxZ);
    }

    /**
     * Run a task for each x,z value corresponding to the plot at that location<br>
     * - Plot: The plot at the x,z (may be null)<br>
     * - Location: The location in the chunk (y = 0)<br>
     * - PlotChunk: Reference to this chunk object<br>
     *
     * @param task
     */
    public void mapByType2D(RunnableVal3<Plot, Integer, Integer> task) {
        int bx = minX;
        int bz = minZ;
        PlotArea area = PlotSquared.get().getPlotArea(getWorld(), null);
        Location location = new Location(getWorld(), bx, 0, bz);
        if (area != null) {
            PlotManager manager = area.getPlotManager();
            for (int x = 0; x < 16; x++) {
                location.setX(bx + x);
                for (int z = 0; z < 16; z++) {
                    location.setZ(bz + z);
                    task.run(area.getPlotAbs(location), x, z);
                }
            }
        } else {
            for (int x = 0; x < 16; x++) {
                location.setX(bx + x);
                for (int z = 0; z < 16; z++) {
                    location.setZ(bz + z);
                    task.run(location.getPlotAbs(), x, z);
                }
            }
        }
    }
}
