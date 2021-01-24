// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PermHandler;

@CommandDeclaration(command = "give",
        description = "Give additional plots to users",
        usage = "/plot give <username> [amount]",
        category = CommandCategory.ADMINISTRATION,
        permission = "plots.give",
        requiredType = RequiredType.NONE)
public class Give extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        checkTrue(args.length >= 1, Captions.COMMAND_SYNTAX, getUsage());
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

        if (current >= Settings.Limit.MAX_PLOTS || current < 0) {
            Captions.GIVE_MAX_PLOTS.send(player, target.getName());
            return true;
        }

        int updated = current + amount;
        String permission = "plots.plot." + updated;

        /*
         * NOTE:
         * If using LuckPerms, set "vault-server" option in the LuckPerms config and enable "use-vault-server"
         * to provide a specific server context to the permission being assigned here
         */

        // PLOTCUBED TODO
//        EconHandler.getEconHandler().setPermission(null, target.getName(), permission, true);

        Captions.GIVE_GAVE_PLOT.send(player, target.getName(), amount);

        return true;
    }

}
// PlotCubed end