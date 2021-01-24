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
package com.plotsquared.core.plot.util;

import com.plotsquared.core.events.PlayerAutoPlotEvent;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlayerEnterPlotEvent;
import com.plotsquared.core.events.PlayerLeavePlotEvent;
import com.plotsquared.core.events.PlayerPlotDeniedEvent;
import com.plotsquared.core.events.PlayerPlotHelperEvent;
import com.plotsquared.core.events.PlayerPlotTrustedEvent;
import com.plotsquared.core.events.PlayerTeleportToPlotEvent;
import com.plotsquared.core.events.PlotAutoMergeEvent;
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotClearEvent;
import com.plotsquared.core.events.PlotComponentSetEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlotDoneEvent;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.PlotRateEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.sk89q.worldedit.function.pattern.Pattern;

import java.util.List;
import java.util.UUID;

public class EventDispatcherTest extends EventDispatcher {

    @Override public PlotRateEvent callRating(PlotPlayer player, Plot plot, Rating rating) {
        return null;
    }

    @Override
    public PlayerClaimPlotEvent callClaim(PlotPlayer player, Plot plot, String schematic) {
        return null;
    }

    @Override
    public PlayerAutoPlotEvent callAuto(PlotPlayer player, PlotArea area, String schematic,
        int size_x, int size_z) {
        return null;
    }

    @Override
    public PlayerTeleportToPlotEvent callTeleport(PlotPlayer player, Location from, Plot plot) {
        return null;
    }

    @Override
    public PlotComponentSetEvent callComponentSet(Plot plot, String component, Pattern pattern) {
        return null;
    }

    @Override public PlotClearEvent callClear(Plot plot) {
        return null;
    }

    @Override public PlotDeleteEvent callDelete(Plot plot) {
        return null;
    }

    @Override public PlotFlagAddEvent callFlagAdd(PlotFlag<?, ?> flag, Plot plot) {
        return null;
    }

    @Override public PlotFlagRemoveEvent callFlagRemove(PlotFlag<?, ?> flag, Plot plot) {
        return null;
    }

    @Override
    public PlotMergeEvent callMerge(Plot plot, Direction dir, int max, PlotPlayer player) {
        return null;
    }

    @Override public PlotAutoMergeEvent callAutoMerge(Plot plot, List<PlotId> plots) {
        return null;
    }

    @Override
    public PlotUnlinkEvent callUnlink(PlotArea area, Plot plot, boolean createRoad,
        boolean createSign, PlotUnlinkEvent.REASON reason) {
        return null;
    }

    @Override public PlayerEnterPlotEvent callEntry(PlotPlayer player, Plot plot) {
        return null;
    }

    @Override public PlayerLeavePlotEvent callLeave(PlotPlayer player, Plot plot) {
        return null;
    }

    @Override
    public PlayerPlotDeniedEvent callDenied(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        return null;
    }

    @Override
    public PlayerPlotTrustedEvent callTrusted(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        return null;
    }

    @Override
    public PlayerPlotHelperEvent callMember(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        return null;
    }

    @Override
    public PlotChangeOwnerEvent callOwnerChange(PlotPlayer initiator, Plot plot, UUID newOwner,
        UUID oldOwner, boolean hasOldOwner) {
        return null;
    }

    @Override public PlotDoneEvent callDone(Plot plot) {
        return null;
    }
}
