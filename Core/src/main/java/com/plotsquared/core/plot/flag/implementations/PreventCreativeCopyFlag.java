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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class PreventCreativeCopyFlag extends BooleanFlag<PreventCreativeCopyFlag> {

    public static final PreventCreativeCopyFlag PREVENT_CREATIVE_COPY_TRUE =
        new PreventCreativeCopyFlag(true);
    public static final PreventCreativeCopyFlag PREVENT_CREATIVE_COPY_FALSE =
        new PreventCreativeCopyFlag(false);

    private PreventCreativeCopyFlag(@NotNull final Boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_PREVENT_CREATIVE_COPY);
    }

    @Override protected PreventCreativeCopyFlag flagOf(@NotNull final Boolean value) {
        return value ? PREVENT_CREATIVE_COPY_TRUE : PREVENT_CREATIVE_COPY_FALSE;
    }

}
