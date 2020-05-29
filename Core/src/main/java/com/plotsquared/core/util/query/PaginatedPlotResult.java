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
package com.plotsquared.core.util.query;

import com.google.common.base.Preconditions;
import com.plotsquared.core.plot.Plot;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Paginated collection of plots as a result of a {@link PlotQuery query}
 */
public final class PaginatedPlotResult {

    private final List<Plot> plots;
    private final int pageSize;

    PaginatedPlotResult(@NotNull final List<Plot> plots, final int pageSize) {
        this.plots = plots;
        this.pageSize = pageSize;
    }

    /**
     * Get the plots belonging to a certain page.
     *
     * @param page Positive page number. Indexed from 1
     * @return Plots that belong to the specified page
     */
    public List<Plot> getPage(final int page) {
        Preconditions.checkState(page >= 0, "Page must be positive");
        final int from = (page - 1) * this.pageSize;
        if (this.plots.size() < from) {
            return Collections.emptyList();
        }
        final int to = Math.max(from + pageSize, this.plots.size());
        return this.plots.subList(from, to);
    }

    /**
     * Get the number of available pages
     *
     * @return Available pages
     */
    public int getPages() {
        return (int) Math.ceil((double) plots.size() / (double) pageSize);
    }

}
