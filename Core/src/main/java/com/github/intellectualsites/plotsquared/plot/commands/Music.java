package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@CommandDeclaration(command = "music", permission = "plots.music",
    description = "Play music in your plot", usage = "/plot music",
    category = CommandCategory.APPEARANCE, requiredType = RequiredType.PLAYER) public class Music
    extends SubCommand {

    private static final Collection<String> DISCS = Arrays
        .asList("music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp",
            "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal",
            "music_disc_strad", "music_disc_ward", "music_disc_11", "music_disc_wait");

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.isAdded(player.getUUID())) {
            sendMessage(player, Captions.NO_PLOT_PERMS);
            return true;
        }
        PlotInventory inv = new PlotInventory(player, 2, "Plot Jukebox") {
            @Override public boolean onClick(int index) {
                PlotItemStack item = getItem(index);
                if (item == null) {
                    return true;
                }
                if (item.getPlotBlock().equalsAny(7, "bedrock")) {
                    plot.removeFlag(Flags.MUSIC);
                    Captions.FLAG_REMOVED.send(player);
                } else if (item.name.toLowerCase(Locale.ENGLISH).contains("disc")) {
                    plot.setFlag(Flags.MUSIC, item.getPlotBlock().getRawId());
                    Captions.FLAG_ADDED.send(player);
                } else {
                    Captions.FLAG_NOT_ADDED.send(player);
                }
                return false;
            }
        };
        int index = 0;

        for (final String disc : DISCS) {
            final String name = String.format("&r&6%s", disc);
            final String[] lore = {"&r&aClick to play!"};
            final PlotItemStack item = new PlotItemStack(disc, 1, name, lore);
            inv.setItem(index++, item);
        }

        // Always add the cancel button
        // if (player.getMeta("music") != null) {
        String name = "&r&6Cancel music";
        String[] lore = {"&r&cClick to cancel!"};
        inv.setItem(index, new PlotItemStack("bedrock", 1, name, lore));
        // }

        inv.openInventory();
        return true;
    }
}
