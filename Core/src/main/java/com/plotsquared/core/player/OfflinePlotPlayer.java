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
package com.plotsquared.core.player;

import java.util.UUID;

public interface OfflinePlotPlayer {

    /**
     * Gets the {@code UUID} of this player
     *
     * @return the player {@link UUID}
     */
    UUID getUUID();

    /**
     * Gets the time in milliseconds when the player was last seen online.
     *
     * @return the time in milliseconds when last online
     * @deprecated This method may be inconsistent across platforms. The javadoc may be wrong depending on which platform is used.
     */
    @SuppressWarnings("DeprecatedIsStillUsed") @Deprecated long getLastPlayed();

    /**
     * Checks if this player is online.
     *
     * @return {@code true} if this player is online
     */
    boolean isOnline();

    /**
     * Gets the name of this player.
     *
     * @return the player name
     */
    String getName();
}
