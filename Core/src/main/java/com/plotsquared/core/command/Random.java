// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.TaskManager;

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

            final Plot finalPlot = plot;

            TaskManager.runTask(() -> {
                if (player != null && player.isOnline()) {
                    player.teleport(finalPlot.getHome());
                    sendMessage(player, Captions.RANDOM_TELEPORTED);
                }
            });
        });

        return true;
    }

}
// PlotCubed end