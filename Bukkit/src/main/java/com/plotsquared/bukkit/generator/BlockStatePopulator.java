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
package com.plotsquared.bukkit.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

final class BlockStatePopulator extends BlockPopulator {

    private final IndependentPlotGenerator plotGenerator;
    private LocalBlockQueue queue;

    public BlockStatePopulator(IndependentPlotGenerator plotGenerator) {
        this.plotGenerator = plotGenerator;
    }

    @Override
    public void populate(@NotNull final World world, @NotNull final Random random,
        @NotNull final Chunk source) {
        if (this.queue == null) {
            this.queue = GlobalBlockQueue.IMP.getNewQueue(world.getName(), false);
        }
        final PlotArea area = PlotSquared.get().getPlotArea(world.getName(), null);
        final ChunkWrapper wrap =
            new ChunkWrapper(area.getWorldName(), source.getX(), source.getZ());
        final ScopedLocalBlockQueue chunk = this.queue.getForChunk(wrap.x, wrap.z);
        if (this.plotGenerator.populateChunk(chunk, area)) {
            this.queue.flush();
        }
    }

}
