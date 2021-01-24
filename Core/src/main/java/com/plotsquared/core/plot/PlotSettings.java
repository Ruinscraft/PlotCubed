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
package com.plotsquared.core.plot;

import com.google.common.collect.ImmutableList;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.plot.comment.PlotComment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generic settings class.
 * - Does not keep a reference to a parent class
 * - Direct changes here will not occur in the db (Use the parent plot object for that)
 */
public class PlotSettings {

    /**
     * Merged plots.
     */
    @Getter private boolean[] merged = new boolean[] {false, false, false, false};
    /**
     * Plot alias.
     */
    @Getter @Setter private String alias = "";
    /**
     * The ratings for a plot.
     */
    @Setter private HashMap<UUID, Integer> ratings;
    /**
     * Plot comments.
     */
    @Setter private List<PlotComment> comments = null;
    /**
     * Home Position.
     */
    private BlockLoc position;

    /**
     * <b>Check if the plot is merged in a direction</b><br> 0 = North<br> 1 = East<br> 2 = South<br> 3 = West<br>
     *
     * @param direction Direction to check
     * @return boolean merged
     */
    public boolean getMerged(int direction) {
        return this.merged[direction];
    }

    public void setMerged(boolean[] merged) {
        this.merged = merged;
    }

    public Map<UUID, Integer> getRatings() {
        if (this.ratings == null) {
            this.ratings = new HashMap<>();
        }
        return this.ratings;
    }

    public boolean setMerged(int direction, boolean merged) {
        if (this.merged[direction] != merged) {
            this.merged[direction] = merged;
            return true;
        }
        return false;
    }

    public boolean setMerged(Direction direction, boolean merged) {
        if (Direction.ALL == direction) {
            throw new IllegalArgumentException("You cannot use Direction.ALL in this method!");
        }
        if (this.merged[direction.getIndex()] != merged) {
            this.merged[direction.getIndex()] = merged;
            return true;
        }
        return false;
    }

    public BlockLoc getPosition() {
        if (this.position == null) {
            return new BlockLoc(0, 0, 0);
        }
        return this.position;
    }

    public void setPosition(BlockLoc position) {
        if (position != null && position.getX() == 0 && position.getY() == 0
            && position.getZ() == 0) {
            position = null;
        }
        this.position = position;
    }

    @SuppressWarnings({"UnstableApiUsage"}) public List<PlotComment> getComments(String inbox) {
        if (this.comments == null) {
            return Collections.emptyList();
        }

        return this.comments.stream().filter(comment -> comment.inbox.equals(inbox))
            .collect(ImmutableList.toImmutableList());
    }

    boolean removeComment(PlotComment comment) {
        if (this.comments == null) {
            return false;
        }
        return this.comments.remove(comment);
    }

    void removeComments(List<PlotComment> comments) {
        comments.forEach(this::removeComment);
    }

    void addComment(PlotComment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }
}
