// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

@CommandDeclaration(command = "delwarp",
        description = "Delete a warp from a plot",
        usage = "/plot delwarp <warpname>",
        permission = "plots.delwarp",
        aliases = {"removewarp", "deletewarp", "warpdel"},
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.PLAYER)
public class DelWarp extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Plot plot = player.getCurrentPlot();

        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Captions.PERMISSION_DELWARP_OTHER.getTranslated())) {
            return sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_DELWARP_OTHER.getTranslated());
        }

        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
        String arg0 = args[0];

        if (!plot.hasWarp(arg0)) {
            return sendMessage(player, Captions.WARP_NOT_FOUND, arg0);
        }

        plot.removeWarp(plot.getWarpByName(arg0));

        return sendMessage(player, Captions.WARP_REMOVED, arg0);
    }
}
// PlotCubed end