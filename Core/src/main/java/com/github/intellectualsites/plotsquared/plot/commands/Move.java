package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

@CommandDeclaration(usage = "/plot move <X;Z>", command = "move", description = "Move a plot",
    aliases = {"debugmove"}, permission = "plots.move", category = CommandCategory.CLAIMING,
    requiredType = RequiredType.PLAYER) public class Move extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        Plot plot1 = loc.getPlotAbs();
        if (plot1 == null) {
            return !MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot1.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.s())) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        boolean override = false;
        if (args.length == 2 && args[1].equalsIgnoreCase("-f")) {
            args = new String[] {args[0]};
            override = true;
        }
        if (args.length != 1) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        Plot plot2;
        if (area == null) {
            plot2 = MainUtil.getPlotFromString(player, args[0], true);
            if (plot2 == null) {
                return false;
            }
        } else {
            plot2 = area.getPlotAbs(plot1.getId());
        }
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot copy <X;Z>");
            return false;
        }
        if (!plot1.getArea().isCompatible(plot2.getArea()) && (!override || !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.s()))) {
            Captions.PLOTWORLD_INCOMPATIBLE.send(player);
            return false;
        }
        if (plot1.move(plot2, () -> MainUtil.sendMessage(player, Captions.MOVE_SUCCESS), false)) {
            return true;
        } else {
            MainUtil.sendMessage(player, Captions.REQUIRES_UNOWNED);
            return false;
        }
    }

}
