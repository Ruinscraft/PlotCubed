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
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.uuid.UUIDMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "trust",
    aliases = {"t"},
    requiredType = RequiredType.PLAYER,
    usage = "/plot trust <player|*>",
    description = "Allow a user to build in a plot and use WorldEdit while the plot owner is offline.",
    category = CommandCategory.SETTINGS)
public class Trust extends Command {

    public Trust() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot currentPlot = player.getCurrentPlot();
        if (currentPlot == null) {
            throw new CommandException(Captions.NOT_IN_PLOT);
        }
        checkTrue(currentPlot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(currentPlot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
            Captions.NO_PLOT_PERMS);
        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());

        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof TimeoutException) {
                    Captions.FETCHING_PLAYERS_TIMEOUT.send(player);
                } else {
                    Captions.INVALID_PLAYER.send(player, args[0]);
                }
                future.completeExceptionally(throwable);
                return;
            } else {
                checkTrue(!uuids.isEmpty(), Captions.INVALID_PLAYER, args[0]);
                Iterator<UUIDMapping> iterator = uuids.iterator();
                int size = currentPlot.getTrusted().size() + currentPlot.getMembers().size();
                while (iterator.hasNext()) {
                    UUIDMapping uuidMapping = iterator.next();
                    if (uuidMapping.getUuid() == DBFunc.EVERYONE && !(
                        Permissions.hasPermission(player, Captions.PERMISSION_TRUST_EVERYONE) || Permissions
                            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST))) {
                        MainUtil.sendMessage(player, Captions.INVALID_PLAYER, uuidMapping.getUsername());
                        iterator.remove();
                        continue;
                    }
                    if (currentPlot.isOwner(uuidMapping.getUuid())) {
                        MainUtil.sendMessage(player, Captions.ALREADY_ADDED, uuidMapping.getUsername());
                        iterator.remove();
                        continue;
                    }
                    if (currentPlot.getTrusted().contains(uuidMapping.getUuid())) {
                        MainUtil.sendMessage(player, Captions.ALREADY_ADDED, uuidMapping.getUsername());
                        iterator.remove();
                        continue;
                    }
                    size += currentPlot.getMembers().contains(uuidMapping.getUuid()) ? 0 : 1;
                }
                checkTrue(!uuids.isEmpty(), null);
                checkTrue(size <= currentPlot.getArea().getMaxPlotMembers() || Permissions
                        .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
                    Captions.PLOT_MAX_MEMBERS);
                // Success
                confirm.run(this, () -> {
                    for (UUIDMapping uuidMapping : uuids) {
                        if (uuidMapping.getUuid() != DBFunc.EVERYONE) {
                            if (!currentPlot.removeMember(uuidMapping.getUuid())) {
                                if (currentPlot.getDenied().contains(uuidMapping.getUuid())) {
                                    currentPlot.removeDenied(uuidMapping.getUuid());
                                }
                            }
                        }
                        currentPlot.addTrusted(uuidMapping.getUuid());
                        PlotSquared.get().getEventDispatcher().callTrusted(player, currentPlot, uuidMapping.getUuid(), true);
                        MainUtil.sendMessage(player, Captions.TRUSTED_ADDED);
                    }
                }, null);
            }
            future.complete(true);
        });
        return CompletableFuture.completedFuture(true);
    }

    @Override public Collection<Command> tab(final PlotPlayer player, final String[] args, final boolean space) {
        return TabCompletions.completePlayers(String.join(",", args).trim(), Collections.emptyList());
    }

}
