package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.Optional;
import java.util.Set;

@CommandDeclaration(command = "buy", description = "Buy the plot you are standing on",
    usage = "/plot buy", permission = "plots.buy", category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE) public class Buy extends Command {

    public Buy() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {

        check(EconHandler.manager, Captions.ECON_DISABLED);
        final Plot plot;
        if (args.length != 0) {
            checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
            plot = check(MainUtil.getPlotFromString(player, args[0], true), null);
        } else {
            plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        }
        checkTrue(plot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(!plot.isOwner(player.getUUID()), Captions.CANNOT_BUY_OWN);
        Set<Plot> plots = plot.getConnectedPlots();
        checkTrue(player.getPlotCount() + plots.size() <= player.getAllowedPlots(),
            Captions.CANT_CLAIM_MORE_PLOTS);
        Optional<Double> flag = plot.getFlag(Flags.PRICE);
        if (!flag.isPresent()) {
            throw new CommandException(Captions.NOT_FOR_SALE);
        }
        final double price = flag.get();
        checkTrue(player.getMoney() >= price, Captions.CANNOT_AFFORD_PLOT);
        player.withdraw(price);
        // Failure
        // Success
        confirm.run(this, () -> {
            Captions.REMOVED_BALANCE.send(player, price);
            EconHandler.manager
                .depositMoney(UUIDHandler.getUUIDWrapper().getOfflinePlayer(plot.owner), price);
            PlotPlayer owner = UUIDHandler.getPlayer(plot.owner);
            if (owner != null) {
                Captions.PLOT_SOLD.send(owner, plot.getId(), player.getName(), price);
            }
            plot.removeFlag(Flags.PRICE);
            plot.setOwner(player.getUUID());
            Captions.CLAIMED.send(player);
            whenDone.run(Buy.this, CommandResult.SUCCESS);
        }, () -> {
            player.deposit(price);
            whenDone.run(Buy.this, CommandResult.FAILURE);
        });
    }
}
