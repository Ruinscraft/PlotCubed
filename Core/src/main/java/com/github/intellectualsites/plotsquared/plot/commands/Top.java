// PlotCubed start
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandDeclaration(command = "top",
        description = "Show and visit the most visited plots",
        usage = "/plot top <current|day|week|month|year|all>",
        aliases = {"popular"},
        permission = "plots.top",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.NONE)
public class Top extends SubCommand {

    private static final String INVENTORY_ITEM_ID = "minecraft:grass";
    private static final int INVENTORY_ITEM_STACK_AMT = 1;
    private static final int INVENTORY_SIZE = 9 * 4;

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 1) {
            return sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
        }

        TaskManager.runTaskAsync(() -> {
            int days;

            switch (args[0].toLowerCase()) {
                case "current":
                case "now":
                    days = 0;
                    break;
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

            final PlotInventory inventory = new PlotInventory(player, INVENTORY_SIZE, Captions.TOP_INVENTORY_NAME.s());
            final List<TopInventoryEntry> inventoryEntries = new ArrayList<>();

            // handle current visitors
            if (days == 0) {
                Map<Plot, Integer> unsorted = MainUtil.getCurrentVisitors();

                unsorted.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Plot, Integer>comparingByValue().reversed())
                        .limit(INVENTORY_SIZE).forEach(entry -> {
                            inventoryEntries.add(new TopInventoryEntry(entry.getKey(), entry.getValue()));
                });
            }

            // handle time period
            else {
                PlotArea applicableArea = player.getApplicablePlotArea();
                Map<PlotId, Integer> unsorted = DBFunc.getTopVisits(days);

                unsorted.entrySet()
                        .stream()
                        .sorted(Map.Entry.<PlotId, Integer>comparingByValue().reversed())
                        .limit(INVENTORY_SIZE).forEach(entry -> {
                            Plot plot = PlotSquared.get().getPlot(applicableArea, entry.getKey());
                            inventoryEntries.add(new TopInventoryEntry(plot, entry.getValue()));
                });
            }

            // fill the inventory contents
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                if (inventoryEntries.size() < i + 1) break;
                TopInventoryEntry entry = inventoryEntries.get(i);
                String plotOwnerName = UUIDHandler.getName(entry.plot.guessOwner());
                String itemName = Captions.color(Captions.TOP_ITEM_NAME.f(plotOwnerName));
                PlotItemStack itemStack = new PlotItemStack(INVENTORY_ITEM_ID, INVENTORY_ITEM_STACK_AMT, itemName);
                inventory.setItem(i, itemStack);
            }

            // display the inventory
            PlotInventory.setPlotInventoryOpen(player, inventory); // TODO: why is this not implemented with PlotPlayer#openInventory()
        });

        return true;
    }

    private static final class TopInventoryEntry {
        public final Plot plot;
        public final int visits;
        public TopInventoryEntry(Plot plot, int visits) {
            this.plot = plot;
            this.visits = visits;
        }
    }
}
// PlotCubed end