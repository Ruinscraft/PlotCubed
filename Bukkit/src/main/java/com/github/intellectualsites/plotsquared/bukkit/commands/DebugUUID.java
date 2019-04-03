package com.github.intellectualsites.plotsquared.bukkit.commands;

import com.github.intellectualsites.plotsquared.bukkit.uuid.DatFileFilter;
import com.github.intellectualsites.plotsquared.bukkit.uuid.DefaultUUIDWrapper;
import com.github.intellectualsites.plotsquared.bukkit.uuid.LowerOfflineUUIDWrapper;
import com.github.intellectualsites.plotsquared.bukkit.uuid.OfflineUUIDWrapper;
import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.CommandCategory;
import com.github.intellectualsites.plotsquared.plot.commands.RequiredType;
import com.github.intellectualsites.plotsquared.plot.commands.SubCommand;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.StringWrapper;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

@CommandDeclaration(command = "uuidconvert", permission = "plots.admin",
    description = "Debug UUID conversion", usage = "/plot uuidconvert <lower|offline|online>",
    requiredType = RequiredType.CONSOLE, category = CommandCategory.DEBUG) public class DebugUUID
    extends SubCommand {

    public DebugUUID() {
        super(Argument.String);
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        final UUIDWrapper currentUUIDWrapper = UUIDHandler.getUUIDWrapper();
        final UUIDWrapper newWrapper;

        switch (args[0].toLowerCase()) {
            case "lower":
                newWrapper = new LowerOfflineUUIDWrapper();
                break;
            case "offline":
                newWrapper = new OfflineUUIDWrapper();
                break;
            case "online":
                newWrapper = new DefaultUUIDWrapper();
                break;
            default:
                try {
                    Class<?> clazz = Class.forName(args[0]);
                    newWrapper = (UUIDWrapper) clazz.newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ignored) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot uuidconvert <lower|offline|online>");
                    return false;
                }
        }

        if (args.length != 2 || !"-o".equals(args[1])) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                "/plot uuidconvert " + args[0] + " - o");
            MainUtil.sendMessage(player, "&cBe aware of the following!");
            MainUtil.sendMessage(player,
                "&8 - &cUse the database command or another method to backup your plots beforehand");
            MainUtil.sendMessage(player,
                "&8 - &cIf the process is interrupted, all plots could be deleted");
            MainUtil.sendMessage(player, "&8 - &cIf an error occurs, all plots could be deleted");
            MainUtil.sendMessage(player, "&8 - &cPlot settings WILL be lost upon conversion");
            MainUtil
                .sendMessage(player, "&cTO REITERATE: BACK UP YOUR DATABASE BEFORE USING THIS!!!");
            MainUtil.sendMessage(player,
                "&7Retype the command with the override parameter when ready :)");
            return false;
        }

        if (currentUUIDWrapper.getClass().getCanonicalName()
            .equals(newWrapper.getClass().getCanonicalName())) {
            MainUtil.sendMessage(player, "&cUUID mode already in use!");
            return false;
        }
        MainUtil.sendMessage(player, "&6Beginning UUID mode conversion");
        MainUtil.sendMessage(player, "&7 - Disconnecting players");
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            entry.getValue()
                .kick("UUID conversion has been initiated. You may reconnect when finished.");
        }

        MainUtil.sendMessage(player, "&7 - Initializing map");

        final HashMap<UUID, UUID> uCMap = new HashMap<>();
        final HashMap<UUID, UUID> uCReverse = new HashMap<>();

        MainUtil.sendMessage(player, "&7 - Collecting playerdata");

        HashSet<String> worlds = Sets.newHashSet(WorldUtil.IMP.getMainWorld(), "world");
        HashSet<UUID> uuids = new HashSet<>();
        HashSet<String> names = new HashSet<>();
        for (String worldName : worlds) {
            File playerDataFolder = new File(worldName + File.separator + "playerdata");
            String[] dat = playerDataFolder.list(new DatFileFilter());
            if (dat != null) {
                for (String current : dat) {
                    String s = current.replaceAll(".dat$", "");
                    try {
                        UUID uuid = UUID.fromString(s);
                        uuids.add(uuid);
                    } catch (Exception ignored) {
                        MainUtil.sendMessage(player,
                            Captions.PREFIX + "Invalid playerdata: " + current);
                    }
                }
            }
            File playersFolder = new File(worldName + File.separator + "players");
            dat = playersFolder.list(new DatFileFilter());
            if (dat != null) {
                for (String current : dat) {
                    names.add(current.replaceAll(".dat$", ""));
                }
            }
        }

        MainUtil.sendMessage(player, "&7 - Populating map");
        UUID uuid2;
        UUIDWrapper wrapper = new DefaultUUIDWrapper();
        for (UUID uuid : uuids) {
            try {
                OfflinePlotPlayer op = wrapper.getOfflinePlayer(uuid);
                uuid = currentUUIDWrapper.getUUID(op);
                uuid2 = newWrapper.getUUID(op);
                if (!uuid.equals(uuid2) && !uCMap.containsKey(uuid) && !uCReverse
                    .containsKey(uuid2)) {
                    uCMap.put(uuid, uuid2);
                    uCReverse.put(uuid2, uuid);
                }
            } catch (Throwable ignored) {
                MainUtil.sendMessage(player,
                    Captions.PREFIX + "&6Invalid playerdata: " + uuid.toString() + ".dat");
            }
        }
        for (String name : names) {
            UUID uuid = currentUUIDWrapper.getUUID(name);
            uuid2 = newWrapper.getUUID(name);
            if (!uuid.equals(uuid2)) {
                uCMap.put(uuid, uuid2);
                uCReverse.put(uuid2, uuid);
            }
        }
        if (uCMap.isEmpty()) {
            MainUtil.sendMessage(player, "&c - Error! Attempting to repopulate");
            for (OfflinePlotPlayer op : currentUUIDWrapper.getOfflinePlayers()) {
                if (op.getLastPlayed() != 0) {
                    //                    String name = op.getPluginName();
                    //                    StringWrapper wrap = new StringWrapper(name);
                    UUID uuid = currentUUIDWrapper.getUUID(op);
                    uuid2 = newWrapper.getUUID(op);
                    if (!uuid.equals(uuid2)) {
                        uCMap.put(uuid, uuid2);
                        uCReverse.put(uuid2, uuid);
                    }
                }
            }
            if (uCMap.isEmpty()) {
                MainUtil.sendMessage(player, "&cError. Failed to collect UUIDs!");
                return false;
            } else {
                MainUtil.sendMessage(player, "&a - Successfully repopulated");
            }
        }

        MainUtil.sendMessage(player, "&7 - Replacing cache");
        TaskManager.runTaskAsync(() -> {
            for (Entry<UUID, UUID> entry : uCMap.entrySet()) {
                String name = UUIDHandler.getName(entry.getKey());
                if (name != null) {
                    UUIDHandler.add(new StringWrapper(name), entry.getValue());
                }
            }

            MainUtil.sendMessage(player, "&7 - Scanning for applicable files (uuids.txt)");

            File file = new File(PlotSquared.get().IMP.getDirectory(), "uuids.txt");
            if (file.exists()) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    for (String line : lines) {
                        try {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            line = line.replaceAll("[\\|][0-9]+[\\|][0-9]+[\\|]", "");
                            String[] split = line.split("\\|");
                            String name = split[0];
                            if (name.isEmpty() || name.length() > 16 || !StringMan
                                .isAlphanumericUnd(name)) {
                                continue;
                            }
                            UUID old = currentUUIDWrapper.getUUID(name);
                            if (old == null) {
                                continue;
                            }
                            UUID now = newWrapper.getUUID(name);
                            UUIDHandler.add(new StringWrapper(name), now);
                            uCMap.put(old, now);
                            uCReverse.put(now, old);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            MainUtil.sendMessage(player, "&7 - Replacing wrapper");
            UUIDHandler.setUUIDWrapper(newWrapper);

            MainUtil.sendMessage(player, "&7 - Updating plot objects");

            for (Plot plot : PlotSquared.get().getPlots()) {
                UUID value = uCMap.get(plot.owner);
                if (value != null) {
                    plot.owner = value;
                }
                plot.getTrusted().clear();
                plot.getMembers().clear();
                plot.getDenied().clear();
            }

            MainUtil.sendMessage(player, "&7 - Deleting database");
            boolean result = DBFunc.deleteTables();

            MainUtil.sendMessage(player, "&7 - Creating tables");

            try {
                DBFunc.createTables();
                if (!result) {
                    MainUtil.sendMessage(player, "&cConversion failed! Attempting recovery");
                    for (Plot plot : PlotSquared.get().getPlots()) {
                        UUID value = uCReverse.get(plot.owner);
                        if (value != null) {
                            plot.owner = value;
                        }
                    }
                    DBFunc.createPlotsAndData(new ArrayList<>(PlotSquared.get().getPlots()),
                        () -> MainUtil.sendMessage(player, "&6Recovery was successful!"));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (newWrapper instanceof OfflineUUIDWrapper) {
                PlotSquared.get().worlds.set("UUID.force-lowercase", false);
                PlotSquared.get().worlds.set("UUID.offline", true);
            } else if (newWrapper instanceof DefaultUUIDWrapper) {
                PlotSquared.get().worlds.set("UUID.force-lowercase", false);
                PlotSquared.get().worlds.set("UUID.offline", false);
            }
            try {
                PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
            } catch (IOException ignored) {
                MainUtil.sendMessage(player,
                    "Could not save configuration. It will need to be manual set!");
            }

            MainUtil.sendMessage(player, "&7 - Populating tables");

            TaskManager.runTaskAsync(() -> {
                ArrayList<Plot> plots = new ArrayList<>(PlotSquared.get().getPlots());
                DBFunc.createPlotsAndData(plots,
                    () -> MainUtil.sendMessage(player, "&aConversion complete!"));
            });

            MainUtil.sendMessage(player, "&aIt is now safe for players to join");
            MainUtil.sendMessage(player,
                "&cConversion is still in progress, you will be notified when it is complete");
        });
        return true;
    }
}
