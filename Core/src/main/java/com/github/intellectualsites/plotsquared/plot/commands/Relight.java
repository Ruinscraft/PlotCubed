package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "relight", description = "Relight your plot", usage = "/plot relight",
    category = CommandCategory.DEBUG, requiredType = RequiredType.PLAYER) public class Relight extends Command {
    public Relight() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            Captions.NOT_IN_PLOT.send(player);
            return CompletableFuture.completedFuture(false);
        }
        final LocalBlockQueue queue = plot.getArea().getQueue(false);
        ChunkManager.chunkTask(plot, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                queue.fixChunkLighting(value[0], value[1]);
            }
        }, () -> {
            plot.refreshChunks();
            Captions.SET_BLOCK_ACTION_FINISHED.send(player);
        }, 5);

        return CompletableFuture.completedFuture(true);
    }
}
