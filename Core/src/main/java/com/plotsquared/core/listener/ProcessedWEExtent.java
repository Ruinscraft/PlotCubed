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
package com.plotsquared.core.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.util.WEManager;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProcessedWEExtent extends AbstractDelegateExtent {

    private final Set<CuboidRegion> mask;
    private final String world;
    private final int max;
    int Ecount = 0;
    boolean Eblocked = false;
    private int count;
    private Extent parent;
    private Map<Long, Integer[]> tileEntityCount = new HashMap<>();

    public ProcessedWEExtent(String world, Set<CuboidRegion> mask, int max, Extent child,
        Extent parent) {
        super(child);
        this.mask = mask;
        this.world = world;
        if (max == -1) {
            max = Integer.MAX_VALUE;
        }
        this.max = max;
        this.count = 0;
        this.parent = parent;
    }

    @Override public BlockState getBlock(BlockVector3 position) {
        if (WEManager.maskContains(this.mask, position.getX(), position.getY(), position.getZ())) {
            return super.getBlock(position);
        }
        return WEExtent.AIRSTATE;
    }

    @Override public BaseBlock getFullBlock(BlockVector3 position) {
        if (WEManager.maskContains(this.mask, position.getX(), position.getY(), position.getZ())) {
            return super.getFullBlock(position);
        }
        return WEExtent.AIRBASE;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block)
        throws WorldEditException {

        final boolean isTile = WorldUtil.IMP.getTileEntityTypes().contains(block.getBlockType());
        if (isTile) {
            final Integer[] tileEntityCount = this.tileEntityCount.computeIfAbsent(getChunkKey(location),
                key -> new Integer[] {WorldUtil.IMP.getTileEntityCount(world,
                    BlockVector2.at(location.getBlockX() >> 4, location.getBlockZ() >> 4))});
            if (tileEntityCount[0] >= Settings.Chunk_Processor.MAX_TILES) {
                return false;
            } else {
                tileEntityCount[0]++;
                PlotSquared.debug(Captions.PREFIX + "&cDetected unsafe WorldEdit: " + location.getX() + ","
                    + location.getZ());
            }
        }
        if (WEManager.maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
            if (this.count++ > this.max) {
                if (this.parent != null) {
                    try {
                        Field field =
                            AbstractDelegateExtent.class.getDeclaredField("extent");
                        field.setAccessible(true);
                        field.set(this.parent, new NullExtent());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.parent = null;
                }
                return false;
            }
            return super.setBlock(location, block);
        }

        return !isTile;
    }

    @Override public Entity createEntity(Location location, BaseEntity entity) {
        if (this.Eblocked) {
            return null;
        }
        this.Ecount++;
        if (this.Ecount > Settings.Chunk_Processor.MAX_ENTITIES) {
            this.Eblocked = true;
            PlotSquared.debug(
                Captions.PREFIX + "&cDetected unsafe WorldEdit: " + location.getBlockX() + ","
                    + location.getBlockZ());
        }
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(),
            location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return WEManager.maskContains(this.mask, position.getX(), position.getZ()) && super
            .setBiome(position, biome);
    }


    private static long getChunkKey(final BlockVector3 location) {
        return (long) (location.getBlockX() >> 4) & 4294967295L | ((long) (location.getBlockZ() >> 4) & 4294967295L) << 32;
    }

}
