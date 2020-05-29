// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandDeclaration(command = "visits",
        description = "Show and visit the most visited plots",
        usage = "/plot visits <current|day|week|month|year|all>",
        aliases = {"popular", "top"},
        permission = "plots.visits",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.PLAYER)
public class Visits extends SubCommand {

    private static final int INVENTORY_ITEM_STACK_AMT = 1;
    private static final int INVENTORY_ROWS = 6; // this automatically gets multiplied by 9 in PlotInventory

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 1) {
            return sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
        }

        final PlotArea applicableArea = player.getApplicablePlotArea();
        String blockIdKey = "worlds." + applicableArea.getWorldName() + ".wall.block_claimed";
        final String itemStackBlockId = PlotSquared.get().worlds.getString(blockIdKey);

        TaskManager.runTaskAsync(() -> {
            final String optionName;
            final int days;

            switch (args[0].toLowerCase()) {
                case "current":
                case "now":
                    days = 0;
                    optionName = "right now";
                    break;
                case "day":
                case "d":
                    days = 1;
                    optionName = "daily";
                    break;
                case "week":
                case "w":
                    days = 7;
                    optionName = "weekly";
                    break;
                case "month":
                case "m":
                    days = 30;
                    optionName = "monthly";
                    break;
                case "year":
                case "y":
                    days = 365;
                    optionName = "yearly";
                    break;
                case "all":
                default:
                    days = -1;
                    optionName = "all time";
                    break;
            }

            final List<TopInventoryEntry> inventoryEntries = new ArrayList<>();
            final String inventoryName = Captions.format(Captions.TOP_INVENTORY_NAME, optionName);
            final PlotInventory inventory = new PlotInventory(player, INVENTORY_ROWS, inventoryName) {
                @Override
                public boolean onClick(final int index) {
                    if (inventoryEntries.size() - 1 < index) return false;
                    TopInventoryEntry entry = inventoryEntries.get(index);
                    Plot plot = entry.plot;
                    plot.teleportPlayer(player); // TODO: fix
                    return false;
                }
            };

            Map<Plot, Integer> unsorted;

            // handle current visitors
            if (days == 0) {
                unsorted = MainUtil.getCurrentVisitors();
            }

            // handle database visits
            else {
                unsorted = DBFunc.getTopVisits(applicableArea, days, INVENTORY_ROWS * 9);
            }

            // sort the entries into inventoryEntries
            unsorted.entrySet()
                    .stream()
                    .sorted(Map.Entry.<Plot, Integer>comparingByValue().reversed())
                    .limit(INVENTORY_ROWS * 9).forEach(entry -> {
                inventoryEntries.add(new TopInventoryEntry(entry.getKey(), entry.getValue()));
            });

            // fill the inventory contents
            for (int i = 0; i < INVENTORY_ROWS * 9; i++) {
                if (inventoryEntries.size() < i + 1) break;
                TopInventoryEntry entry = inventoryEntries.get(i);
                String plotOwnerName = MainUtil.getName(entry.plot.getOwnerAbs());
                String itemName = Captions.color(Captions.format(Captions.TOP_ITEM_NAME, plotOwnerName));
                PlotItemStack itemStack = new PlotItemStack(
                        itemStackBlockId,
                        INVENTORY_ITEM_STACK_AMT,
                        itemName,
                        "Plot ID: " + entry.plot.getId(),
                        "Visitors: " + entry.visits,
                        "Click to visit");
                inventory.setItem(i, itemStack);
            }

            inventory.openInventory();
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