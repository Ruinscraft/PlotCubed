/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

@CommandDeclaration(command = "setbiome",
    permission = "plots.set.biome",
    description = "Set the plot biome",
    usage = "/plot biome [biome]",
    aliases = {"biome", "sb", "setb", "b"},
    category = CommandCategory.APPEARANCE,
    requiredType = RequiredType.NONE)
public class Biome extends SetCommand {

    @Override public boolean set(final PlotPlayer player, final Plot plot, final String value) {
        BiomeType biome = null;
        try {
            biome = BiomeTypes.get(value.toLowerCase());
        } catch (final Exception ignore) {
        }
        if (biome == null) {
            String biomes = StringMan
                .join(BiomeType.REGISTRY.values(), Captions.BLOCK_LIST_SEPARATOR.getTranslated());
            Captions.NEED_BIOME.send(player);
            MainUtil.sendMessage(player,
                Captions.SUBCOMMAND_SET_OPTIONS_HEADER.getTranslated() + biomes);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        plot.setBiome(biome, () -> {
            plot.removeRunning();
            MainUtil
                .sendMessage(player, Captions.BIOME_SET_TO.getTranslated() + value.toLowerCase());
        });
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer player, final String[] args,
        final boolean space) {
        return SuggestionHelper.getNamespacedRegistrySuggestions(BiomeType.REGISTRY, args[0])
            .map(value -> value.toLowerCase(Locale.ENGLISH).replace("minecraft:", ""))
            .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
            .map(value -> new Command(null, false, value, "", RequiredType.NONE, null) {
            }).collect(Collectors.toList());
    }

}
