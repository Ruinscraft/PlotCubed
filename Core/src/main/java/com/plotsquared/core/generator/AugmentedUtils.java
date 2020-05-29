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
 *                  Copyright (C) 2020 IntellectualSites
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
package com.plotsquared.core.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.AreaBoundDelegateLocalBlockQueue;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.queue.LocationOffsetDelegateLocalBlockQueue;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import com.plotsquared.core.util.RegionUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class AugmentedUtils {

    private static boolean enabled = true;

    public static void bypass(boolean bypass, Runnable run) {
        enabled = bypass;
        run.run();
        enabled = true;
    }

    public static boolean generate(@Nullable Object chunkObject, @NotNull final String world,
        final int chunkX, final int chunkZ, LocalBlockQueue queue) {
        if (!enabled) {
            return false;
        }
        // The coordinates of the block on the
        // least positive corner of the chunk
        final int blockX = chunkX << 4;
        final int blockZ = chunkZ << 4;
        // Create a region that contains the
        // entire chunk
        CuboidRegion region = RegionUtil.createRegion(blockX, blockX + 15, blockZ, blockZ + 15);
        // Query for plot areas in the chunk
        Set<PlotArea> areas = PlotSquared.get().getPlotAreas(world, region);
        if (areas.isEmpty()) {
            return false;
        }
        boolean generationResult = false;
        for (final PlotArea area : areas) {
            // A normal plot world may not contain any clusters
            // and so there's no reason to continue searching
            if (area.getType() == PlotAreaType.NORMAL) {
                return false;
            }
            // This means that full vanilla generation is used
            // so we do not interfere
            if (area.getTerrain() == PlotAreaTerrainType.ALL) {
                continue;
            }
            IndependentPlotGenerator generator = area.getGenerator();
            // Mask
            if (queue == null) {
                queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
                queue.setChunkObject(chunkObject);
            }
            LocalBlockQueue primaryMask;
            // coordinates
            int relativeBottomX;
            int relativeBottomZ;
            int relativeTopX;
            int relativeTopZ;
            // Generation
            if (area.getType() == PlotAreaType.PARTIAL) {
                relativeBottomX = Math.max(0, area.getRegion().getMinimumPoint().getX() - blockX);
                relativeBottomZ = Math.max(0, area.getRegion().getMinimumPoint().getZ() - blockZ);
                relativeTopX = Math.min(15, area.getRegion().getMaximumPoint().getX() - blockX);
                relativeTopZ = Math.min(15, area.getRegion().getMaximumPoint().getZ() - blockZ);

                primaryMask = new AreaBoundDelegateLocalBlockQueue(area, queue);
            } else {
                relativeBottomX = relativeBottomZ = 0;
                relativeTopX = relativeTopZ = 15;
                primaryMask = queue;
            }

            LocalBlockQueue secondaryMask;
            BlockState air = BlockTypes.AIR.getDefaultState();
            if (area.getTerrain() == PlotAreaTerrainType.ROAD) {
                PlotManager manager = area.getPlotManager();
                final boolean[][] canPlace = new boolean[16][16];
                boolean has = false;
                for (int x = relativeBottomX; x <= relativeTopX; x++) {
                    for (int z = relativeBottomZ; z <= relativeTopZ; z++) {
                        int worldX = x + blockX;
                        int worldZ = z + blockZ;
                        boolean can = manager.getPlotId(worldX, 0, worldZ) == null;
                        if (can) {
                            for (int y = 1; y < 128; y++) {
                                queue.setBlock(worldX, y, worldZ, air);
                            }
                            canPlace[x][z] = true;
                            has = true;
                        }
                    }
                }
                if (!has) {
                    continue;
                }
                generationResult = true;
                secondaryMask = new LocationOffsetDelegateLocalBlockQueue(canPlace, blockX, blockZ,
                    primaryMask);
            } else {
                secondaryMask = primaryMask;
                for (int x = relativeBottomX; x <= relativeTopX; x++) {
                    for (int z = relativeBottomZ; z <= relativeTopZ; z++) {
                        for (int y = 1; y < 128; y++) {
                            queue.setBlock(blockX + x, y, blockZ + z, air);
                        }
                    }
                }
                generationResult = true;
            }
            primaryMask.setChunkObject(chunkObject);
            primaryMask.setForceSync(true);
            secondaryMask.setChunkObject(chunkObject);
            secondaryMask.setForceSync(true);

            ScopedLocalBlockQueue scoped =
                new ScopedLocalBlockQueue(secondaryMask, new Location(world, blockX, 0, blockZ),
                    new Location(world, blockX + 15, 255, blockZ + 15));
            generator.generateChunk(scoped, area);
            generator.populateChunk(scoped, area);
        }
        if (queue != null) {
            queue.setForceSync(true);
            queue.flush();
        }
        return generationResult;
    }

}
