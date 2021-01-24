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
package com.plotsquared.core.setup;


import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.SetupUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class PlotAreaBuilder {
    @Getter @Setter private String generatorName;
    @Getter @Setter private String plotManager;
    @Getter @Setter private PlotAreaType plotAreaType;
    @Getter @Setter private PlotAreaTerrainType terrainType;
    @Getter @Setter private String worldName;
    @Getter @Setter private String areaName;
    @Getter private PlotId minimumId;
    @Getter private PlotId maximumId;
    @Getter @Setter private SettingsNodesWrapper settingsNodesWrapper;
    @Getter @Setter private SetupUtils setupManager;

    public static PlotAreaBuilder ofPlotArea(PlotArea area) {
        return new PlotAreaBuilder()
                .worldName(area.getWorldName())
                .areaName(area.getId())
                .plotAreaType(area.getType())
                .terrainType(area.getTerrain())
                .generatorName(area.getGenerator().getName())
                .plotManager(PlotSquared.imp().getPluginName())
                .minimumId(area.getMin())
                .maximumId(area.getMax())
                .settingsNodesWrapper(new SettingsNodesWrapper(area.getSettingNodes(), null));
    }

    public PlotAreaBuilder minimumId(PlotId minimumId) {
        if (this.maximumId != null
                && (minimumId.getX() > this.maximumId.getX() || minimumId.getY() > this.maximumId.getY())) {
            throw new IllegalStateException("minId >= maxId");
        }
        this.minimumId = minimumId;
        return this;
    }

    public PlotAreaBuilder maximumId(PlotId maximumId) {
        if (this.minimumId != null
                && (maximumId.getX() < this.minimumId.getX() || maximumId.getY() < this.minimumId.getY())) {
            throw new IllegalStateException("maxId <= minId");
        }
        this.maximumId = maximumId;
        return this;
    }

}
