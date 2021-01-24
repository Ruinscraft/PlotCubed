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
package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import org.jetbrains.annotations.NotNull;

public abstract class IntegerFlag<F extends NumberFlag<Integer, F>> extends NumberFlag<Integer, F> {

    protected IntegerFlag(final int value, int minimum, int maximum,
        @NotNull Caption flagDescription) {
        super(value, minimum, maximum, Captions.FLAG_CATEGORY_INTEGERS, flagDescription);
    }

    protected IntegerFlag(@NotNull Caption flagDescription) {
        this(0, Integer.MIN_VALUE, Integer.MAX_VALUE, flagDescription);
    }

    @Override public F merge(@NotNull Integer newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

    @Override public String getExample() {
        return "10";
    }

    @NotNull @Override protected Integer parseNumber(String input) throws FlagParseException {
        try {
            return Integer.parseInt(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_INTEGER);
        }
    }
}
