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
package com.plotsquared.core.events;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for {@link CancellablePlotEvent}.
 * <p>
 * DENY: do not allow the event to happen
 * ALLOW: allow the event to continue as normal, subject to standard checks
 * FORCE: force the event to occur, even if normal checks would deny.
 * WARNING: this may have unintended consequences! Make sure you study the appropriate code before using!
 */
public enum Result {

    DENY(0), ACCEPT(1), FORCE(2);

    private static Map<Integer, Result> map = new HashMap<>();

    static {
        for (Result eventResult : Result.values()) {
            map.put(eventResult.value, eventResult);
        }
    }

    private int value;

    Result(int value) {
        this.value = value;
    }

    /**
     * Obtain the Result enum associated with the int value
     *
     * @param eventResult the int value
     * @return the corresponding Result
     */
    public static Result valueOf(int eventResult) {
        return map.get(eventResult);
    }

    /**
     * Get int value of enum
     *
     * @return integer value
     */
    public int getValue() {
        return value;
    }
}
