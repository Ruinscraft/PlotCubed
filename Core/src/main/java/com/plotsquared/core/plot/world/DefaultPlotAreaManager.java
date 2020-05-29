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
package com.plotsquared.core.plot.world;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotWorld;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultPlotAreaManager implements PlotAreaManager {

    final PlotArea[] noPlotAreas = new PlotArea[0];
    private final Map<String, PlotWorld> plotWorlds = new HashMap<>();

    @Override public PlotArea[] getAllPlotAreas() {
        final Set<PlotArea> area = new HashSet<>();
        for (final PlotWorld world : plotWorlds.values()) {
            area.addAll(world.getAreas());
        }
        return area.toArray(new PlotArea[0]);
    }

    @Override @Nullable public PlotArea getApplicablePlotArea(final Location location) {
        if (location == null) {
            return null;
        }
        final PlotWorld world = this.plotWorlds.get(location.getWorld());
        if (world == null) {
            return null;
        }
        return world.getArea(location);
    }

    @Override public void addPlotArea(final PlotArea plotArea) {
        PlotWorld world = this.plotWorlds.get(plotArea.getWorldName());
        if (world != null) {
            if (world instanceof StandardPlotWorld && world.getAreas().isEmpty()) {
                this.plotWorlds.remove(plotArea.getWorldName());
            } else {
                world.addArea(plotArea);
                return;
            }
        }
        if (plotArea.getType() != PlotAreaType.PARTIAL) {
            world = new StandardPlotWorld(plotArea.getWorldName(), plotArea);
        } else {
            world = new ScatteredPlotWorld(plotArea.getWorldName());
            world.addArea(plotArea);
        }
        this.plotWorlds.put(plotArea.getWorldName(), world);
    }

    @Override public void removePlotArea(final PlotArea area) {
        final PlotWorld world = this.plotWorlds.get(area.getWorldName());
        if (world == null) {
            return;
        }
        if (world instanceof StandardPlotWorld) {
            this.plotWorlds.remove(world.getWorld());
        } else {
            world.removeArea(area);
            if (world.getAreas().isEmpty()) {
                this.plotWorlds.remove(world.getWorld());
            }
        }
    }

    @Override public PlotArea getPlotArea(final String world, final String id) {
        final PlotWorld plotWorld = this.plotWorlds.get(world);
        if (plotWorld == null) {
            return null;
        }
        final List<PlotArea> areas = new ArrayList<>(plotWorld.getAreas());
        if (areas.size() == 1) {
            return areas.get(0);
        }
        if (id == null) {
            return null;
        }
        for (final PlotArea area : areas) {
            if (StringMan.isEqual(id, area.getId())) {
                return area;
            }
        }
        return null;
    }

    @Override public PlotArea getPlotArea(@NotNull final Location location) {
        return this.getApplicablePlotArea(location);
    }

    @Override public PlotArea[] getPlotAreas(final String world, final CuboidRegion region) {
        final PlotWorld plotWorld = this.plotWorlds.get(world);
        if (plotWorld == null) {
            return noPlotAreas;
        }
        if (region == null) {
            return plotWorld.getAreas().toArray(new PlotArea[0]);
        }
        return plotWorld.getAreasInRegion(region).toArray(new PlotArea[0]);
    }

    @Override public void addWorld(final String worldName) {
        PlotWorld world = this.plotWorlds.get(worldName);
        if (world != null) {
            return;
        }
        // Create a new empty world. When a new area is added
        // the world will be re-recreated with the correct type
        world = new StandardPlotWorld(worldName, null);
        this.plotWorlds.put(worldName, world);
    }

    @Override public void removeWorld(final String worldName) {
        this.plotWorlds.remove(worldName);
    }

    @Override public String[] getAllWorlds() {
        return this.plotWorlds.keySet().toArray(new String[0]);
    }
}
