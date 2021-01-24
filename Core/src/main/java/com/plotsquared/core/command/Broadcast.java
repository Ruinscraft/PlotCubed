// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

@CommandDeclaration(command = "broadcast",
        description = "Broadcast a message to a plot",
        usage = "/plot broadcast <message>",
        aliases = {"bcast", "announce", "say", "bc"},
        permission = "plots.broadcast",
        category = CommandCategory.ROLEPLAY,
        requiredType = RequiredType.NONE)
public class Broadcast extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        checkTrue(args.length > 0, Captions.COMMAND_SYNTAX, getUsage());

        Plot plot = player.getCurrentPlot();

        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Captions.PERMISSION_USE_BROADCAST_OTHER.getTranslated())) {
            return !sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_USE_BROADCAST_OTHER.getTranslated());
        }

        String msg = String.join(" ", args);

        if (msg.length() > 128) {
            msg = msg.substring(0, 127);

            sendMessage(player, Captions.BROADCAST_MSG_SHORTENED);
        }

        for (PlotPlayer playerInPlot : plot.getPlayersInPlot()) {
            playerInPlot.sendTitle(Captions.color(msg), "");
            playerInPlot.sendMessage(Captions.color(Captions.BROADCAST_PREFIX.getTranslated() + msg));
        }

        return true;
    }
}
// PlotCubed end