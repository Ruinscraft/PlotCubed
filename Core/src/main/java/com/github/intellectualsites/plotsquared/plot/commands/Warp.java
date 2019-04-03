// PlotCubed start
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

@CommandDeclaration(command = "warp",
        description = "Warp to a location on a plot",
        usage = "/plot warp <name>",
        permission = "plots.warp",
        aliases = {"warps"},
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.NONE)
public class Warp extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Plot plot = player.getCurrentPlot();

        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (args.length < 1) {
            PlotMessage plotMessage = new PlotMessage().text(Captions.WARP_LIST
                    .f(Integer.toString(plot.getWarps().size()))).color("$1");
            plotMessage = MainUtil.getWarpsList(plotMessage, plot.getWarps()).color("$2");
            plotMessage.send(player);
            return true;
        }

        String arg0 = args[0];

        if (!plot.teleportToWarp(arg0, player)) {
            return sendMessage(player, Captions.WARP_NOT_FOUND);
        }

        return sendMessage(player, Captions.WARP_TELEPORTED, arg0);
    }
}
// PlotCubed end