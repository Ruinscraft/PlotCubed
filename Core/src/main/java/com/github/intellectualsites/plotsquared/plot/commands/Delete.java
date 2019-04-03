package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.*;


@CommandDeclaration(command = "delete", permission = "plots.delete",
    description = "Delete the plot you stand on", usage = "/plot delete",
    aliases = {"dispose", "del"}, category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE, confirmation = true) public class Delete extends SubCommand {

    // Note: To delete a specific plot use /plot <plot> delete
    // The syntax also works with any command: /plot <plot> <command>

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, Captions.PLOT_UNOWNED);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DELETE)) {
            return !sendMessage(player, Captions.NO_PLOT_PERMS);
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        final int currentPlots =
            Settings.Limit.GLOBAL ? player.getPlotCount() : player.getPlotCount(loc.getWorld());
        Runnable run = new Runnable() {
            @Override public void run() {
                if (plot.getRunning() > 0) {
                    MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
                    return;
                }
                final long start = System.currentTimeMillis();
                boolean result = plot.deletePlot(new Runnable() {
                    @Override public void run() {
                        plot.removeRunning();
                        if ((EconHandler.manager != null) && plotArea.USE_ECONOMY) {
                            Expression<Double> valueExr = plotArea.PRICES.get("sell");
                            double value = plots.size() * valueExr.evaluate((double) currentPlots);
                            if (value > 0d) {
                                EconHandler.manager.depositMoney(player, value);
                                sendMessage(player, Captions.ADDED_BALANCE, String.valueOf(value));
                            }
                        }
                        MainUtil.sendMessage(player, Captions.DELETING_DONE,
                            System.currentTimeMillis() - start);
                    }
                });
                if (result) {
                    plot.addRunning();
                } else {
                    MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
                }
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, getCommandString() + ' ' + plot.getId(), run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
