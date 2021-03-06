// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.message.PlotMessage;
import com.plotsquared.core.util.MainUtil;

@CommandDeclaration(command = "warp",
        description = "Warp to a location on a plot",
        usage = "/plot warp <name>",
        permission = "plots.warp",
        aliases = {"warps"},
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.PLAYER)
public class Warp extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Plot plot = player.getCurrentPlot();

        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }

        // TODO: somehow clean this up, it's really hacky
        if (args.length < 1) {
            PlotMessage clickableWarps = new PlotMessage(
                    Captions.color(Captions.PLOT_INFO_WARPS.getTranslated() // .color doesn't work here?
                            .replaceAll("%warpcount%", Integer.toString(plot.getWarps().size()))
                            .replaceAll("%warps%", ""))).color("$1"); // basically ignore %warps%

            String csvWarps = MainUtil.getWarpsList(plot.getWarps());

            for (String warp : csvWarps.split(" ")) {
                String command = "/plot warp " + warp.replace(",", "");
                clickableWarps = clickableWarps.text(warp + " ").command(command).tooltip(command).color("$2");
            }

            clickableWarps.send(player);

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