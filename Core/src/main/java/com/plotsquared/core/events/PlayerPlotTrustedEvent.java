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
package com.plotsquared.core.events;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

import java.util.UUID;

public class PlayerPlotTrustedEvent extends PlotEvent {

    private final PlotPlayer initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotTrustedEvent: Called when a plot trusted user is added/removed
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was added/removed from the trusted list
     * @param added     true of the player was added, false if the player was removed
     */
    public PlayerPlotTrustedEvent(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    /**
     * If a player was added
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }

    /**
     * The UUID added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action
     *
     * @return PlotPlayer
     */
    public PlotPlayer getInitiator() {
        return this.initiator;
    }
}
