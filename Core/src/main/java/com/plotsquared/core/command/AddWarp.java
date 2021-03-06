// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotWarp;
import com.plotsquared.core.util.StringMan;

@CommandDeclaration(command = "addwarp",
        description = "Add a warp to a plot",
        usage = "/plot addwarp <warpname>",
        aliases = {"setwarp", "warpadd", "warpset"},
        permission = "plots.addwarp",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.PLAYER)
public class AddWarp extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();

        if (plot == null || !plot.hasOwner()) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Captions.PERMISSION_ADDWARP_OTHER.getTranslated())) {
            return sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_ADDWARP_OTHER.getTranslated());
        }

        Plot base = plot.getBasePlot(false);
        Location bottom = base.getBottomAbs();
        Location playerLoc = player.getLocationFull();
        BlockLoc plotRelLoc = new BlockLoc(playerLoc.getX() - bottom.getX(), playerLoc.getY(), playerLoc.getZ() - bottom.getZ(), playerLoc.getYaw(), playerLoc.getPitch());

        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
        String arg0 = args[0];

        if (arg0.length() > 50) {
            return sendMessage(player, Captions.WARP_NAME_INVALID, arg0);
        }

        if (!StringMan.isAlphanumeric(arg0)) {
            return sendMessage(player, Captions.WARP_NOT_ALPHANUM);
        }

        if (arg0.isEmpty()) {
            return sendMessage(player, Captions.WARP_NAME_INVALID, arg0);
        }

        if (arg0.contains(" ")) {
            return sendMessage(player, Captions.WARP_NAME_INVALID, arg0);
        }

        if (plot.getWarps().size() >= 64) {
            return sendMessage(player, Captions.WARP_TOO_MANY, Integer.toString(64));
        }

        if (plot.hasWarp(arg0)) {
            return sendMessage(player, Captions.WARP_ALREADY_EXISTS, arg0);
        }

        plot.addWarp(new PlotWarp(arg0, plotRelLoc));

        return sendMessage(player, Captions.WARP_ADDED, arg0);
    }
}
// PlotCubed end