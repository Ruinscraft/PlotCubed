package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;

import java.util.List;

/**
 * A plot manager with square plots which tessellate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot).
 */
public class ClassicPlotManager extends SquarePlotManager {

    @Override public boolean setComponent(PlotArea plotArea, PlotId plotId, String component,
        BlockBucket blocks) {
        switch (component) {
            case "floor":
                setFloor(plotArea, plotId, blocks);
                return true;
            case "wall":
                setWallFilling(plotArea, plotId, blocks);
                return true;
            case "all":
                setAll(plotArea, plotId, blocks);
                return true;
            case "air":
                setAir(plotArea, plotId, blocks);
                return true;
            case "main":
                setMain(plotArea, plotId, blocks);
                return true;
            case "middle":
                setMiddle(plotArea, plotId, blocks);
                return true;
            case "outline":
                setOutline(plotArea, plotId, blocks);
                return true;
            case "border":
                setWall(plotArea, plotId, blocks);
                return true;
        }
        return false;
    }

    @Override public boolean unClaimPlot(PlotArea plotArea, Plot plot, Runnable whenDone) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        setWallFilling(dpw, plot.getId(), dpw.WALL_FILLING);
        setWall(dpw, plot.getId(), dpw.WALL_BLOCK);
        GlobalBlockQueue.IMP.addTask(whenDone);
        return true;
    }

    public boolean setFloor(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        Plot plot = plotArea.getPlotAbs(plotId);
        LocalBlockQueue queue = plotArea.getQueue(false);
        if (plot.isBasePlot()) {
            ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
            for (RegionWrapper region : plot.getRegions()) {
                Location pos1 =
                    new Location(plotArea.worldname, region.minX, dpw.PLOT_HEIGHT, region.minZ);
                Location pos2 =
                    new Location(plotArea.worldname, region.maxX, dpw.PLOT_HEIGHT, region.maxZ);
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        queue.enqueue();
        return true;
    }

    public boolean setAll(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        Plot plot = plotArea.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        LocalBlockQueue queue = plotArea.getQueue(false);
        int maxY = plotArea.getPlotManager().getWorldHeight();
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(plotArea.worldname, region.minX, 1, region.minZ);
            Location pos2 = new Location(plotArea.worldname, region.maxX, maxY, region.maxZ);
            queue.setCuboid(pos1, pos2, blocks);
        }
        queue.enqueue();
        return true;
    }

    public boolean setAir(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        Plot plot = plotArea.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        LocalBlockQueue queue = plotArea.getQueue(false);
        int maxY = plotArea.getPlotManager().getWorldHeight();
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 =
                new Location(plotArea.worldname, region.minX, dpw.PLOT_HEIGHT + 1, region.minZ);
            Location pos2 = new Location(plotArea.worldname, region.maxX, maxY, region.maxZ);
            queue.setCuboid(pos1, pos2, blocks);
        }
        queue.enqueue();
        return true;
    }

    public boolean setMain(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        Plot plot = plotArea.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        LocalBlockQueue queue = plotArea.getQueue(false);
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(plotArea.worldname, region.minX, 1, region.minZ);
            Location pos2 =
                new Location(plotArea.worldname, region.maxX, dpw.PLOT_HEIGHT - 1, region.maxZ);
            queue.setCuboid(pos1, pos2, blocks);
        }
        queue.enqueue();
        return true;
    }

    public boolean setMiddle(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        Plot plot = plotArea.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        Location[] corners = plot.getCorners();
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        LocalBlockQueue queue = plotArea.getQueue(false);

        int x = MathMan.average(corners[0].getX(), corners[1].getX());
        int z = MathMan.average(corners[0].getZ(), corners[1].getZ());
        queue.setBlock(x, dpw.PLOT_HEIGHT, z, blocks.getBlock());
        queue.enqueue();
        return true;
    }

    public boolean setOutline(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = plotArea.getPlotAbs(plotId);
        Location bottom = plot.getBottomAbs();
        Location top = plot.getExtendedTopAbs();
        LocalBlockQueue queue = plotArea.getQueue(false);
        int maxY = plotArea.getPlotManager().getWorldHeight();
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bottom.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = dpw.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bottom.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = dpw.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }

        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = dpw.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = dpw.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (plot.isBasePlot()) {
            for (RegionWrapper region : plot.getRegions()) {
                Location pos1 = new Location(plotArea.worldname, region.minX, maxY, region.minZ);
                Location pos2 = new Location(plotArea.worldname, region.maxX, maxY, region.maxZ);
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        queue.enqueue();
        return true;
    }

    public boolean setWallFilling(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = plotArea.getPlotAbs(plotId);
        Location bot = plot.getExtendedBottomAbs()
            .subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0,
                plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        LocalBlockQueue queue = plotArea.getQueue(false);
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX();
                 x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ();
                 z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        queue.enqueue();
        return true;
    }

    public boolean setWall(PlotArea plotArea, PlotId plotId, BlockBucket blocks) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = plotArea.getPlotAbs(plotId);
        Location bot = plot.getExtendedBottomAbs()
            .subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0,
                plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        PseudoRandom random = new PseudoRandom();
        LocalBlockQueue queue = plotArea.getQueue(false);
        int y = dpw.WALL_HEIGHT + 1;
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX();
                 x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ();
                 z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        queue.enqueue();
        return true;
    }

    /**
     * PLOT MERGING.
     */
    @Override public boolean createRoadEast(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location pos1 = getPlotBottomLocAbs(plotArea, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotArea, plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 2;
        int ez = pos2.getZ() + 2;
        LocalBlockQueue queue = plotArea.getQueue(false);
        int maxY = plotArea.getPlotManager().getWorldHeight();
        queue.setCuboid(
            new Location(plotArea.worldname, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1,
                sz + 1), new Location(plotArea.worldname, ex, maxY, ez - 1),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx, 0, sz + 1),
            new Location(plotArea.worldname, ex, 0, ez - 1), PlotBlock.get((short) 7, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx, 1, sz + 1),
            new Location(plotArea.worldname, sx, dpw.WALL_HEIGHT, ez - 1), dpw.WALL_FILLING);
        queue.setCuboid(new Location(plotArea.worldname, sx, dpw.WALL_HEIGHT + 1, sz + 1),
            new Location(plotArea.worldname, sx, dpw.WALL_HEIGHT + 1, ez - 1), dpw.WALL_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, ex, 1, sz + 1),
            new Location(plotArea.worldname, ex, dpw.WALL_HEIGHT, ez - 1), dpw.WALL_FILLING);
        queue.setCuboid(new Location(plotArea.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1),
            new Location(plotArea.worldname, ex, dpw.WALL_HEIGHT + 1, ez - 1), dpw.WALL_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 1, sz + 1),
            new Location(plotArea.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        queue.enqueue();
        return true;
    }

    @Override public boolean createRoadSouth(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location pos1 = getPlotBottomLocAbs(plotArea, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotArea, plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 2;
        int ex = pos2.getX() + 2;
        LocalBlockQueue queue = plotArea.getQueue(false);
        queue.setCuboid(
            new Location(plotArea.worldname, sx + 1, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1,
                sz),
            new Location(plotArea.worldname, ex - 1, plotArea.getPlotManager().getWorldHeight(),
                ez), PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 0, sz),
            new Location(plotArea.worldname, ex - 1, 0, ez), PlotBlock.get((short) 7, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 1, sz),
            new Location(plotArea.worldname, ex - 1, dpw.WALL_HEIGHT, sz), dpw.WALL_FILLING);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, dpw.WALL_HEIGHT + 1, sz),
            new Location(plotArea.worldname, ex - 1, dpw.WALL_HEIGHT + 1, sz), dpw.WALL_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 1, ez),
            new Location(plotArea.worldname, ex - 1, dpw.WALL_HEIGHT, ez), dpw.WALL_FILLING);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez),
            new Location(plotArea.worldname, ex - 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 1, sz + 1),
            new Location(plotArea.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        queue.enqueue();
        return true;
    }

    @Override public boolean createRoadSouthEast(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location pos2 = getPlotTopLocAbs(plotArea, plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = pos2.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        LocalBlockQueue queue = plotArea.getQueue(false);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, dpw.ROAD_HEIGHT + 1, sz + 1),
            new Location(plotArea.worldname, ex - 1, dpw.getPlotManager().getWorldHeight(), ez - 1),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 0, sz + 1),
            new Location(plotArea.worldname, ex - 1, 0, ez - 1),
            PlotBlock.get((short) 7, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 1, sz + 1),
            new Location(plotArea.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        queue.enqueue();
        return true;
    }

    @Override public boolean removeRoadEast(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location pos1 = getPlotBottomLocAbs(plotArea, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotArea, plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 1;
        int ez = pos2.getZ() + 1;
        LocalBlockQueue queue = plotArea.getQueue(false);
        queue.setCuboid(
            new Location(plotArea.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1,
                sz),
            new Location(plotArea.worldname, ex, plotArea.getPlotManager().getWorldHeight(), ez),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx, 1, sz + 1),
            new Location(plotArea.worldname, ex, dpw.PLOT_HEIGHT - 1, ez - 1), dpw.MAIN_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, sx, dpw.PLOT_HEIGHT, sz + 1),
            new Location(plotArea.worldname, ex, dpw.PLOT_HEIGHT, ez - 1), dpw.TOP_BLOCK);
        queue.enqueue();
        return true;
    }

    @Override public boolean removeRoadSouth(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location pos1 = getPlotBottomLocAbs(plotArea, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotArea, plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 1;
        int ex = pos2.getX() + 1;
        LocalBlockQueue queue = plotArea.getQueue(false);
        queue.setCuboid(
            new Location(plotArea.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1,
                sz),
            new Location(plotArea.worldname, ex, plotArea.getPlotManager().getWorldHeight(), ez),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, 1, sz),
            new Location(plotArea.worldname, ex - 1, dpw.PLOT_HEIGHT - 1, ez), dpw.MAIN_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, sx + 1, dpw.PLOT_HEIGHT, sz),
            new Location(plotArea.worldname, ex - 1, dpw.PLOT_HEIGHT, ez), dpw.TOP_BLOCK);
        queue.enqueue();
        return true;
    }

    @Override public boolean removeRoadSouthEast(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location location = getPlotTopLocAbs(dpw, plot.getId());
        int sx = location.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = location.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        LocalBlockQueue queue = plotArea.getQueue(false);
        queue.setCuboid(new Location(plotArea.worldname, sx, dpw.ROAD_HEIGHT + 1, sz),
            new Location(plotArea.worldname, ex, plotArea.getPlotManager().getWorldHeight(), ez),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(plotArea.worldname, sx, 1, sz),
            new Location(plotArea.worldname, ex, dpw.ROAD_HEIGHT - 1, ez), dpw.MAIN_BLOCK);
        queue.setCuboid(new Location(plotArea.worldname, sx, dpw.ROAD_HEIGHT, sz),
            new Location(plotArea.worldname, ex, dpw.ROAD_HEIGHT, ez), dpw.TOP_BLOCK);
        queue.enqueue();
        return true;
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED).
     */
    @Override public boolean finishPlotMerge(PlotArea plotArea, List<PlotId> plotIds) {
        final BlockBucket block = ((ClassicPlotWorld) plotArea).CLAIMED_WALL_BLOCK;
        plotIds.forEach(id -> setWall(plotArea, id, block));
        if (Settings.General.MERGE_REPLACE_WALL) {
            final BlockBucket wallBlock = ((ClassicPlotWorld) plotArea).WALL_FILLING;
            plotIds.forEach(id -> setWallFilling(plotArea, id, wallBlock));
        }
        return true;
    }

    @Override public boolean finishPlotUnlink(PlotArea plotArea, List<PlotId> plotIds) {
        final BlockBucket block = ((ClassicPlotWorld) plotArea).CLAIMED_WALL_BLOCK;
        plotIds.forEach(id -> setWall(plotArea, id, block));
        return true;
    }

    @Override public boolean startPlotMerge(PlotArea plotArea, List<PlotId> plotIds) {
        return true;
    }

    @Override public boolean startPlotUnlink(PlotArea plotArea, List<PlotId> plotIds) {
        return true;
    }

    @Override public boolean claimPlot(PlotArea plotArea, Plot plot) {
        final BlockBucket claim = ((ClassicPlotWorld) plotArea).CLAIMED_WALL_BLOCK;
        setWall(plotArea, plot.getId(), claim);
        return true;
    }

    @Override public String[] getPlotComponents(PlotArea plotArea, PlotId plotId) {
        return new String[] {"main", "floor", "air", "all", "border", "wall", "outline", "middle"};
    }

    /**
     * Remove sign for a plot.
     */
    @Override public Location getSignLoc(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        plot = plot.getBasePlot(false);
        Location bot = plot.getBottomAbs();
        return new Location(plotArea.worldname, bot.getX() - 1, dpw.ROAD_HEIGHT + 1,
            bot.getZ() - 2);
    }
}
