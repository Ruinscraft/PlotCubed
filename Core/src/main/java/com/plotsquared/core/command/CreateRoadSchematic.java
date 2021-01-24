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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;

@CommandDeclaration(command = "createroadschematic",
    aliases = {"crs"},
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.PLAYER,
    permission = "plots.createroadschematic",
    description = "Add a road schematic to your world using the roads around your current plot",
    usage = "/plot createroadschematic")
public class CreateRoadSchematic extends SubCommand {

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!(location.getPlotArea() instanceof HybridPlotWorld)) {
            return sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        HybridUtils.manager.setupRoadSchematic(plot);
        MainUtil.sendMessage(player, Captions.SCHEMATIC_ROAD_CREATED);
        return true;
    }
}
