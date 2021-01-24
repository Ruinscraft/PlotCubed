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
package com.plotsquared.core.util.placeholders;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

public final class PlotFlagPlaceholder extends PlotSpecificPlaceholder {

    private final PlotFlag<?, ?> flag;
    private final boolean local;

    public PlotFlagPlaceholder(@NotNull final PlotFlag<?, ?> flag, final boolean local) {
        super(String.format("currentplot_%sflag_%s", local ? "local": "", flag.getName()));
        this.flag = flag;
        this.local = local;
    }

    @Override @NotNull public String getValue(@NotNull final PlotPlayer<?> player, @NotNull final Plot plot) {
        return this.getFlagValue(plot, this.flag.getName(), !this.local);
    }

    /**
     * Return the flag value from its name on the current plot.
     * If the flag doesn't exist it returns an empty string.
     * If the flag exists but it is not set on current plot and the parameter inherit is set to true,
     * it returns the default value.
     *
     * @param plot     Current plot where the player is
     * @param flagName Name of flag to get from current plot
     * @param inherit  Define if it returns only the flag set on the current plot or also inherited flags
     * @return The value of flag serialized in string
     */
    @NotNull private String getFlagValue(@NotNull final Plot plot, @NotNull final String flagName, final boolean inherit) {
        if (flagName.isEmpty()) {
            return "";
        }
        final PlotFlag<?, ?> flag = GlobalFlagContainer.getInstance().getFlagFromString(flagName);
        if (flag == null) {
            return "";
        }
        if (inherit) {
            return plot.getFlag(flag).toString();
        } else {
            final PlotFlag<?, ?> plotFlag = plot.getFlagContainer().queryLocal(flag.getClass());
            return (plotFlag != null) ? plotFlag.getValue().toString() : "";
        }
    }

}
