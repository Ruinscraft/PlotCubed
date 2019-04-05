// PlotCubed start
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotInventory;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

@CommandDeclaration(command = "top",
        description = "Show and visit the most visited plots",
        usage = "/plot top <current|day|week|month|year|all>",
        aliases = {"popular"},
        permission = "plots.top",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.NONE)
public class Top extends SubCommand {

    private static final int INVENTORY_SIZE = 9 * 4;

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        if (args.length < 1) {
            return sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
        }

        int days;

        switch (args[0].toLowerCase()) {
            case "day":
            case "d":
                days = 1;
                break;
            case "week":
            case "w":
                days = 7;
                break;
            case "month":
            case "m":
                days = 30;
                break;
            case "year":
            case "y":
                days = 365;
                break;
            case "all":
            default:
                days = -1;
                break;
        }




        PlotInventory inventory = new PlotInventory(player, INVENTORY_SIZE, Captions.TOP_INVENTORY_NAME.s());

        for (int i = 0; i < INVENTORY_SIZE; i++) {

        }

        return true;
    }
}
// PlotCubed end