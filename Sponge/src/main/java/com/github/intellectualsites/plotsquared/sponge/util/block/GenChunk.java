package com.github.intellectualsites.plotsquared.sponge.util.block;

import com.github.intellectualsites.plotsquared.plot.object.ChunkWrapper;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import com.github.intellectualsites.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;

public class GenChunk extends ScopedLocalBlockQueue {

    private final MutableBlockVolume terrain;
    private final MutableBiomeVolume biome;
    private final int bz;
    private final int bx;
    private final String world;

    public boolean modified = false;

    public GenChunk(MutableBlockVolume terrain, MutableBiomeVolume biome, ChunkWrapper wrap) {
        super(null, new Location(null, 0, 0, 0), new Location(null, 15, 255, 15));
        this.bx = wrap.x << 4;
        this.bz = wrap.z << 4;
        this.terrain = terrain;
        this.biome = biome;
        this.world = wrap.world;
    }

    @Override public void fillBiome(String biomeName) {
        if (this.biome == null) {
            return;
        }
        BiomeType biome = SpongeUtil.getBiome(biomeName.toUpperCase());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                this.biome.setBiome(this.bx + x, 0, this.bz + z, biome);
            }
        }
    }

    @Override public boolean setBiome(int x, int z, String biomeName) {
        modified = true;
        BiomeType biome = SpongeUtil.getBiome(biomeName.toUpperCase());
        this.biome.setBiome(this.bx + x, 0, this.bz + z, biome);
        return true;
    }

    @Override public boolean setBlock(int x, int y, int z, int id, int data) {
        modified = true;
        this.terrain.setBlock(this.bx + x, y, this.bz + z, SpongeUtil.getBlockState(id, data));
        return true;
    }

    @Override public PlotBlock getBlock(int x, int y, int z) {
        return SpongeUtil.getPlotBlock(this.terrain.getBlock(this.bx + x, y, this.bz + z));
    }

    @Override public String getWorld() {
        return this.world;
    }

    @Override public Location getMax() {
        return new Location(getWorld(), 15 + bx, 255, 15 + bz);
    }

    @Override public Location getMin() {
        return new Location(getWorld(), bx, 0, bz);
    }



    public GenChunk clone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    public GenChunk shallowClone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
