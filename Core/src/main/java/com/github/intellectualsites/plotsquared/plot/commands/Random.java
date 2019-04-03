// PlotCubed start
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

@CommandDeclaration(command = "random",
        description = "Teleport to a random plot",
        usage = "/plot random",
        permission = "plots.random",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.NONE)
public class Random extends SubCommand {

    private static final java.util.Random random = new java.util.Random();

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        sendMessage(player, Captions.RANDOM_TELEPORTING);

        TaskManager.runTaskAsync(() -> {
            String worldName = player.getLocation().getWorld();

            Plot plot = null;

            // potentially could block
            while (plot == null) {
                plot = (Plot) PlotSquared.get().getPlots(worldName).toArray()[random.nextInt(PlotSquared.get().getPlots(worldName).size())];
            }

            if (player != null && player.isOnline()) {
                player.teleport(plot.getHome());
                sendMessage(player, Captions.RANDOM_TELEPORTED);
            }
        });

        return true;
    }

}
// PlotCubed end