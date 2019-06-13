package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.object.StringPlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.block.DelegateLocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AugmentedUtils {

    private static boolean enabled = true;

    public static void bypass(boolean bypass, Runnable run) {
        enabled = bypass;
        run.run();
        enabled = true;
    }

    public static boolean generate(@NotNull final String world, final int chunkX, final int chunkZ,
        LocalBlockQueue queue) {
        if (!enabled) {
            return false;
        }

        final int blockX = chunkX << 4;
        final int blockZ = chunkZ << 4;
        RegionWrapper region = new RegionWrapper(blockX, blockX + 15, blockZ, blockZ + 15);
        Set<PlotArea> areas = PlotSquared.get().getPlotAreas(world, region);
        if (areas.isEmpty()) {
            return false;
        }
        boolean toReturn = false;
        for (final PlotArea area : areas) {
            if (area.TYPE == 0) {
                return false;
            }
            if (area.TERRAIN == 3) {
                continue;
            }
            IndependentPlotGenerator generator = area.getGenerator();
            if (generator == null) {
                continue;
            }
            // Mask
            if (queue == null) {
                queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
            }
            LocalBlockQueue primaryMask;
            // coordinates
            int bxx;
            int bzz;
            int txx;
            int tzz;
            // gen
            if (area.TYPE == 2) {
                bxx = Math.max(0, area.getRegion().minX - blockX);
                bzz = Math.max(0, area.getRegion().minZ - blockZ);
                txx = Math.min(15, area.getRegion().maxX - blockX);
                tzz = Math.min(15, area.getRegion().maxZ - blockZ);
                primaryMask = new DelegateLocalBlockQueue(queue) {
                    @Override public boolean setBlock(int x, int y, int z, PlotBlock id) {
                        if (area.contains(x, z)) {
                            return super.setBlock(x, y, z, id);
                        }
                        return false;
                    }

                    @Override public boolean setBiome(int x, int z, String biome) {
                        if (area.contains(x, z)) {
                            return super.setBiome(x, z, biome);
                        }
                        return false;
                    }
                };
            } else {
                bxx = bzz = 0;
                txx = tzz = 15;
                primaryMask = queue;
            }
            LocalBlockQueue secondaryMask;
            PlotBlock air = StringPlotBlock.EVERYTHING;
            if (area.TERRAIN == 2) {
                PlotManager manager = area.getPlotManager();
                final boolean[][] canPlace = new boolean[16][16];
                boolean has = false;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        int rx = x + blockX;
                        int rz = z + blockZ;
                        boolean can = manager.getPlotId(rx, 0, rz) == null;
                        if (can) {
                            for (int y = 1; y < 128; y++) {
                                queue.setBlock(rx, y, rz, air);
                            }
                            canPlace[x][z] = can;
                            has = true;
                        }
                    }
                }
                if (!has) {
                    continue;
                }
                toReturn = true;
                secondaryMask = new DelegateLocalBlockQueue(primaryMask) {
                    @Override public boolean setBlock(int x, int y, int z, PlotBlock id) {
                        if (canPlace[x - blockX][z - blockZ]) {
                            return super.setBlock(x, y, z, id);
                        }
                        return false;
                    }

                    @Override public boolean setBiome(int x, int y, String biome) {
                        return super.setBiome(x, y, biome);
                    }
                };
            } else {
                secondaryMask = primaryMask;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        for (int y = 1; y < 128; y++) {
                            queue.setBlock(blockX + x, y, blockZ + z, air);
                        }
                    }
                }
                toReturn = true;
            }
            ScopedLocalBlockQueue scoped = new ScopedLocalBlockQueue(secondaryMask,
                new Location(area.worldname, blockX, 0, blockZ),
                new Location(area.worldname, blockX + 15, 255, blockZ + 15));
            generator.generateChunk(scoped, area);
            generator.populateChunk(scoped, area);
        }
        if (queue != null) {
            queue.flush();
        }
        return toReturn;
    }
}
