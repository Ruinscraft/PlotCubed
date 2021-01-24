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
package com.plotsquared.core.backup;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public interface BackupManager {

    /**
     * This will perform an automatic backup of the plot iff the plot has an owner,
     * automatic backups are enabled and the plot is not merged.
     * Otherwise it will complete immediately.
     *
     * @param player   Player that triggered the backup
     * @param plot     Plot to perform the automatic backup on
     * @param whenDone Action that runs when the automatic backup has been completed
     */
    static void backup(@Nullable PlotPlayer player, @NotNull final Plot plot, @NotNull Runnable whenDone) {
        Objects.requireNonNull(PlotSquared.imp()).getBackupManager().automaticBackup(player, plot, whenDone);
    }

    /**
     * Get the backup profile for a plot based on its
     * current owner (if there is one)
     *
     * @param plot Plot to get the backup profile for
     * @return Backup profile
     */
    @NotNull BackupProfile getProfile(@NotNull final Plot plot);

    /**
     * This will perform an automatic backup of the plot iff the plot has an owner,
     * automatic backups are enabled and the plot is not merged.
     * Otherwise it will complete immediately.
     *
     * @param player   Player that triggered the backup
     * @param plot     Plot to perform the automatic backup on
     * @param whenDone Action that runs when the automatic backup has been completed
     */
    void automaticBackup(@Nullable PlotPlayer player, @NotNull final Plot plot, @NotNull Runnable whenDone);

    /**
     * Get the directory in which backups are stored
     *
     * @return Backup directory path
     */
    @NotNull Path getBackupPath();

    /**
     * Get the maximum amount of backups that may be stored for
     * a plot-owner combo
     *
     * @return Backup limit
     */
    int getBackupLimit();

    /**
     * Returns true if (potentially) destructive actions should cause
     * PlotSquared to create automatic plot backups
     *
     * @return True if automatic backups are enabled
     */
    boolean shouldAutomaticallyBackup();

}
