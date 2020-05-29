// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MathMan;

@CommandDeclaration(command = "give",
        description = "Give additional plots to users",
        usage = "/plot give <username> [amount]",
        category = CommandCategory.ADMINISTRATION,
        permission = "plots.give",
        requiredType = RequiredType.NONE)
public class Give extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
        String username = args[0];
        int amount = 1;

        if (args.length > 1) {
            if (MathMan.isInteger(args[1])) {
                amount = Integer.parseInt(args[1]);
            }
        }

        PlotPlayer target = PlotPlayer.from(username);
        checkTrue(target != null, Captions.INVALID_PLAYER, getUsage());

        int current = target.getAllowedPlots();
        int updated = current + amount;
        String permission = "plots.plot." + updated;

        // TODO: world context??
        //EconHandler.getEconHandler().setPermission(null, target.getName(), permission, true);

        Captions.GIVE_GAVE_PLOT.send(player, target.getName(), amount);

        return true;
    }

}
// PlotCubed end