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
package com.plotsquared.core.util;

import com.plotsquared.core.IPlotMain;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class EconHandler {

    /**
     * @deprecated This will be removed in the future,
     * call {@link IPlotMain#getEconomyHandler()} instead.
     */
    @Deprecated @Nullable public static EconHandler manager;

    /**
     * Initialize the economy handler using {@link IPlotMain#getEconomyHandler()}
     * @deprecated Call {@link #init} instead or use {@link IPlotMain#getEconomyHandler()}
     * which does this already.
     */
    @Deprecated public static void initializeEconHandler() {
        manager = PlotSquared.get().IMP.getEconomyHandler();
    }

    /**
     * Return the econ handler instance, if one exists
     *
     * @return Economy handler instance
     * @deprecated Call {@link IPlotMain#getEconomyHandler()} instead
     */
    @Deprecated @Nullable public static EconHandler getEconHandler() {
        manager = PlotSquared.get().IMP.getEconomyHandler();
        return manager;
    }

    public abstract boolean init();

    public double getMoney(PlotPlayer<?> player) {
        if (player instanceof ConsolePlayer) {
            return Double.MAX_VALUE;
        }
        return getBalance(player);
    }

    public abstract double getBalance(PlotPlayer<?> player);

    public abstract void withdrawMoney(PlotPlayer<?> player, double amount);

    public abstract void depositMoney(PlotPlayer<?> player, double amount);

    public abstract void depositMoney(OfflinePlotPlayer player, double amount);

    /**
     * @deprecated Use {@link PermHandler#hasPermission(String, String, String)} instead
     */
    @Deprecated public abstract boolean hasPermission(String world, String player, String perm);

    /**
     * @deprecated Use {@link PermHandler#hasPermission(String, String)} instead
     */
    @Deprecated public boolean hasPermission(String player, String perm) {
        return hasPermission(null, player, perm);
    }
}
