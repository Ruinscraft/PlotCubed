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
package com.plotsquared.core.database;

import com.plotsquared.core.plot.*;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.task.RunnableVal;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AbstractDBTest implements AbstractDB {

    @Override public void setOwner(Plot plot, UUID uuid) {
    }

    @Override public void createPlotsAndData(List<Plot> plots, Runnable whenDone) {
    }

    @Override public void createPlotSafe(Plot plot, Runnable success, Runnable failure) {
    }

    @Override public void createTables() {
    }

    @Override public void delete(Plot plot) {
    }

    @Override public void deleteSettings(Plot plot) {
    }

    @Override public void deleteHelpers(Plot plot) {
    }

    // PlotCubed start
    @Override public void deleteWarps(Plot plot) {
    }

    @Override public void deleteVisits(Plot plot) {
    }
    // PlotCubed end

    @Override public void deleteTrusted(Plot plot) {
    }

    @Override public void deleteDenied(Plot plot) {
    }

    @Override public void deleteComments(Plot plot) {
    }

    @Override public void deleteRatings(Plot plot) {
    }

    @Override public void delete(PlotCluster cluster) {
    }

    @Override public void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete) {
    }

    @Override public void removePersistentMeta(UUID uuid, String key) {
    }

    @Override public void getPersistentMeta(UUID uuid, RunnableVal<Map<String, byte[]>> result) {
    }

    @Override public void createPlotSettings(int id, Plot plot) {
    }

    @Override public int getId(Plot plot) {
        return 0;
    }

    // PlotCubed start
    @Override public PlotId getPlotId(int id) {
        return null;
    }
    // PlotCubed end

    @Override public int getClusterId(PlotCluster cluster) {
        return 0;
    }

    @Override public boolean convertFlags() {
        return true;
    }

    @Override public HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        return null;
    }

    @Override public void validateAllPlots(Set<Plot> toValidate) {
    }

    @Override public HashMap<String, Set<PlotCluster>> getClusters() {
        return null;
    }

    @Override public void setMerged(Plot plot, boolean[] merged) {
    }

    @Override public CompletableFuture<Boolean> swapPlots(Plot plot1, Plot plot2) {
        return CompletableFuture.completedFuture(true);
    }

    @Override public void setFlag(Plot plot, PlotFlag<?, ?> flag) {
    }

    @Override public void removeFlag(Plot plot, PlotFlag<?, ?> flag) {
    }

    @Override public void setClusterName(PlotCluster cluster, String name) {
    }

    @Override public void setAlias(Plot plot, String alias) {
    }

    @Override public void purgeIds(Set<Integer> uniqueIds) {
    }

    @Override public void purge(PlotArea area, Set<PlotId> plotIds) {
    }

    @Override public void setPosition(Plot plot, String position) {
    }

    @Override public void setPosition(PlotCluster cluster, String position) {
    }

    // PlotCubed start
    @Override public void removeWarp(Plot plot, PlotWarp warp) {
    }
    // PlotCubed end

    @Override public void removeTrusted(Plot plot, UUID uuid) {
    }

    @Override public void removeHelper(PlotCluster cluster, UUID uuid) {
    }

    @Override public void removeMember(Plot plot, UUID uuid) {
    }

    @Override public void removeInvited(PlotCluster cluster, UUID uuid) {
    }

    // PlotCubed start
    @Override public void setWarp(Plot plot, PlotWarp warp) {
    }

    @Override public void addVisit(Plot plot, UUID visitor) {
    }
    // PlotCubed end

    @Override public void setTrusted(Plot plot, UUID uuid) {
    }

    @Override public void setHelper(PlotCluster cluster, UUID uuid) {
    }

    @Override public void setMember(Plot plot, UUID uuid) {
    }

    @Override public void setInvited(PlotCluster cluster, UUID uuid) {
    }

    @Override public void removeDenied(Plot plot, UUID uuid) {
    }

    @Override public void setDenied(Plot plot, UUID uuid) {
    }

    // PlotCubed start
    @Override public HashMap<Plot, Integer> getTopVisits(PlotArea plotArea, int days, int sizeLimit) {
        return null;
    }
    // PlotCubed end

    @Override public HashMap<UUID, Integer> getRatings(Plot plot) {
        return null;
    }

    @Override public void setRating(Plot plot, UUID rater, int value) {
    }

    @Override public void removeComment(Plot plot, PlotComment comment) {
    }

    @Override public void clearInbox(Plot plot, String inbox) {
    }

    @Override public void setComment(Plot plot, PlotComment comment) {
    }

    @Override
    public void getComments(@NotNull Plot plot, String inbox,
        RunnableVal<List<PlotComment>> whenDone) {
    }

    @Override public void createPlotAndSettings(Plot plot, Runnable whenDone) {
    }

    @Override public void createCluster(PlotCluster cluster) {
    }

    @Override public void resizeCluster(PlotCluster current, PlotId min, PlotId max) {
    }

    @Override public void movePlot(Plot originalPlot, Plot newPlot) {
    }

    @Override public void replaceUUID(UUID old, UUID now) {
    }

    @Override public boolean deleteTables() {
        return false;
    }

    @Override public void close() {
    }

    @Override public void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max) {
    }

    @Override public void updateTables(int[] oldVersion) {
    }
}

