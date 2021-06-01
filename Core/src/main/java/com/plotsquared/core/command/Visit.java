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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.UntrustedVisitFlag;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.query.SortingStrategy;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.uuid.UUIDMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "visit",
    permission = "plots.visit",
    description = "Visit someones plot",
    usage = "/plot visit <player>|<alias>|<plot> [area]|[#] [#]",
    aliases = {"v", "tp", "teleport", "goto", "warp"},
    requiredType = RequiredType.PLAYER,
    category = CommandCategory.TELEPORT)
public class Visit extends Command {

    public Visit() {
        super(MainCommand.getInstance(), true);
    }

    private void visit(@NotNull final PlotPlayer player, @NotNull final PlotQuery query, final PlotArea sortByArea,
        final RunnableVal3<Command, Runnable, Runnable> confirm, final RunnableVal2<Command, CommandResult> whenDone) {
        this.visit(player, query, sortByArea, confirm, whenDone, 1);
    }

    private void visit(@NotNull final PlotPlayer player, @NotNull final PlotQuery query, final PlotArea sortByArea,
        final RunnableVal3<Command, Runnable, Runnable> confirm, final RunnableVal2<Command, CommandResult> whenDone, int page) {
        // We get the query once,
        // then we get it another time further on
        final List<Plot> unsorted = query.asList();

        if (unsorted.isEmpty()) {
            Captions.FOUND_NO_PLOTS.send(player);
            return;
        }

        if (unsorted.size() > 1) {
            query.whereBasePlot();
        }

        if (page == Integer.MIN_VALUE) {
            page = 1;
        }

        if (page < 1 || page > unsorted.size()) {
            MainUtil.sendMessage(player, String.format("(1, %d)", unsorted.size()));
            return;
        }

        PlotArea relativeArea = sortByArea;
        if (Settings.Teleport.PER_WORLD_VISIT && sortByArea == null) {
            relativeArea = player.getApplicablePlotArea();
        }

        if (relativeArea != null) {
            query.relativeToArea(relativeArea).withSortingStrategy(SortingStrategy.SORT_BY_CREATION);
        } else {
            query.withSortingStrategy(SortingStrategy.SORT_BY_TEMP);
        }

        final List<Plot> plots = query.asList();

        final Plot plot = plots.get(page - 1);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_UNOWNED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_UNOWNED);
                return;
            }
        } else if (plot.isOwner(player.getUUID())) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OWNED) && !Permissions
                .hasPermission(player, Captions.PERMISSION_HOME)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OWNED);
                return;
            }
        } else if (plot.isAdded(player.getUUID())) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_SHARED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_SHARED);
                return;
            }
        } else {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OTHER)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OTHER);
                return;
            }
            if (!plot.getFlag(UntrustedVisitFlag.class) && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED);
                return;
            }
        }

        confirm.run(this, () -> plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {
            if (result) {
                whenDone.run(Visit.this, CommandResult.SUCCESS);
            } else {
                whenDone.run(Visit.this, CommandResult.FAILURE);
            }
        }), () -> whenDone.run(Visit.this, CommandResult.FAILURE));
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player,
        String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 1 && args[0].contains(":")) {
            args = args[0].split(":");
        }

        PlotArea sortByArea;

        int page = Integer.MIN_VALUE;

        switch (args.length) {
            // /p v <user> <area> <page>
            case 3:
                if (!MathMan.isInteger(args[2])) {
                    Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return CompletableFuture.completedFuture(false);
                }
                page = Integer.parseInt(args[2]);
            // /p v <name> <area> [page]
            // /p v <name> [page]
            case 2:
                if (page != Integer.MIN_VALUE || !MathMan.isInteger(args[1])) {
                    sortByArea = PlotSquared.get().getPlotAreaByString(args[1]);
                    if (sortByArea == null) {
                        Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                        Captions.COMMAND_SYNTAX.send(player, getUsage());
                        return CompletableFuture.completedFuture(false);
                    }

                    final PlotArea finalSortByArea = sortByArea;
                    int finalPage1 = page;
                    MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
                        if (throwable instanceof TimeoutException) {
                            Captions.FETCHING_PLAYERS_TIMEOUT.send(player);
                        } else if (throwable != null || uuids.size() != 1) {
                            Captions.COMMAND_SYNTAX.send(player, getUsage());
                        } else {
                            final UUIDMapping mapping = uuids.toArray(new UUIDMapping[0])[0];
                            this.visit(player, PlotQuery.newQuery()
                                    .thatPasses(plot -> plot.isOwner(mapping.getUuid())),
                                    finalSortByArea, confirm, whenDone, finalPage1);
                        }
                    });
                    break;
                }
                page = Integer.parseInt(args[1]);
            // /p v <name> [page]
            // /p v <uuid> [page]
            // /p v <plot> [page]
            // /p v <alias>
            case 1:
                final String[] finalArgs = args;
                int finalPage = page;
                if (args[0].length() >= 2 && !args[0].contains(";") && !args[0].contains(",")) {
                    PlotSquared.get().getImpromptuUUIDPipeline().getSingle(args[0], (uuid, throwable) -> {
                        if (throwable instanceof TimeoutException) {
                            // The request timed out
                            MainUtil.sendMessage(player, Captions.FETCHING_PLAYERS_TIMEOUT);
                        } else if (uuid != null && !PlotSquared.get().hasPlot(uuid)) {
                            // It was a valid UUID but the player has no plots
                            MainUtil.sendMessage(player, Captions.PLAYER_NO_PLOTS);
                        } else if (uuid == null) {
                            // player not found, so we assume it's an alias if no page was provided
                            if (finalPage == Integer.MIN_VALUE) {
                                this.visit(player, PlotQuery.newQuery().withAlias(finalArgs[0]), player.getApplicablePlotArea(), confirm, whenDone, 1);
                            } else {
                                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, finalArgs[0]);
                            }
                        } else {
                            this.visit(player, PlotQuery.newQuery().thatPasses(plot -> plot.isOwner(uuid)).whereBasePlot(), null, confirm, whenDone, finalPage);
                        }
                    });
                } else {
                    // Try to parse a plot
                    final Plot plot = MainUtil.getPlotFromString(player, finalArgs[0], true);
                    if (plot != null) {
                        this.visit(player, PlotQuery.newQuery().withPlot(plot), null, confirm, whenDone, 1);
                    }
                }
                break;
            case 0:
                // /p v is invalid
                Captions.COMMAND_SYNTAX.send(player, getUsage());
                return CompletableFuture.completedFuture(false);
            default:
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        final List<Command> completions = new ArrayList<>();
        switch (args.length - 1) {
            case 0:
                completions.addAll(TabCompletions.completePlayers(args[0], Collections.emptyList()));
                break;
            case 1:
                completions.addAll(
                        TabCompletions.completeAreas(args[1]));
                if (args[1].isEmpty()) {
                    // if no input is given, only suggest 1 - 3
                    completions.addAll(
                            TabCompletions.asCompletions("1", "2", "3"));
                    break;
                }
                completions.addAll(
                        TabCompletions.completeNumbers(args[1], 10, 999));
                break;
            case 2:
                if (args[2].isEmpty()) {
                    // if no input is given, only suggest 1 - 3
                    completions.addAll(
                            TabCompletions.asCompletions("1", "2", "3"));
                    break;
                }
                completions.addAll(
                        TabCompletions.completeNumbers(args[2], 10, 999));
                break;
        }

        return completions;
    }
}
