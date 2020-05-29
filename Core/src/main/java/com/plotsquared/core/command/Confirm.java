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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;

@CommandDeclaration(command = "confirm",
    permission = "plots.use",
    description = "Confirm an action",
    category = CommandCategory.INFO)
public class Confirm extends SubCommand {

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        CmdInstance command = CmdConfirm.getPending(player);
        if (command == null) {
            MainUtil.sendMessage(player, Captions.FAILED_CONFIRM);
            return false;
        }
        CmdConfirm.removePending(player);
        if ((System.currentTimeMillis() - command.timestamp)
            > Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS * 1000) {
            MainUtil.sendMessage(player, Captions.EXPIRED_CONFIRM);
            return false;
        }
        TaskManager.runTask(command.command);
        return true;
    }
}
