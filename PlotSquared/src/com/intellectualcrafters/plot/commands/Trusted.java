/*
 * Copyright (c) IntellectualCrafters - 2014.
 * You are not allowed to distribute and/or monetize any of our intellectual property.
 * IntellectualCrafters is not affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 *
 * >> File = Trusted.java
 * >> Generated by: Citymonstret at 2014-08-09 01:41
 */

package com.intellectualcrafters.plot.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlayerPlotTrustedEvent;

@SuppressWarnings("deprecation")
public class Trusted extends SubCommand {

    public Trusted() {
        super(Command.TRUSTED, "Manage trusted users for a plot", "trusted {add|remove} {player}", CommandCategory.ACTIONS);
    }

    private boolean hasBeenOnServer(String name) {
        Player plr = Bukkit.getPlayerExact(name);
        if (plr == null) {
            OfflinePlayer oplr = Bukkit.getOfflinePlayer(name);
            if (oplr == null) {
                return false;
            } else {
                return oplr.hasPlayedBefore();
            }
        } else {
            if (plr.isOnline()) {
                return true;
            } else {
                return plr.hasPlayedBefore();
            }
        }
    }

    @Override
    public boolean execute(Player plr, String... args) {
        if (args.length < 2) {
            PlayerFunctions.sendMessage(plr, C.TRUSTED_NEED_ARGUMENT);
            return true;
        }
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return true;
        }
        Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if ((plot.owner == null) || !plot.hasRights(plr)) {
            PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (args[1].equalsIgnoreCase("*")) {
                UUID uuid = DBFunc.everyone;
                plot.addTrusted(uuid);
                DBFunc.setTrusted(plr.getWorld().getName(), plot, Bukkit.getOfflinePlayer(args[1]));
                PlayerFunctions.sendMessage(plr, C.TRUSTED_ADDED);
                return true;
            }
            if (!hasBeenOnServer(args[1])) {
                PlayerFunctions.sendMessage(plr, C.PLAYER_HAS_NOT_BEEN_ON);
                return true;
            }
            UUID uuid = null;
            if ((Bukkit.getPlayerExact(args[1]) != null)) {
                uuid = Bukkit.getPlayerExact(args[1]).getUniqueId();
            } else {
                uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
            }
            if (uuid == null) {
                PlayerFunctions.sendMessage(plr, C.PLAYER_HAS_NOT_BEEN_ON);
                return true;
            }
            plot.addTrusted(uuid);
            DBFunc.setTrusted(plr.getWorld().getName(), plot, Bukkit.getOfflinePlayer(args[1]));
            PlayerPlotTrustedEvent event = new PlayerPlotTrustedEvent(plr, plot, uuid, true);
            Bukkit.getPluginManager().callEvent(event);
            PlayerFunctions.sendMessage(plr, C.TRUSTED_ADDED);
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args[1].equalsIgnoreCase("*")) {
                UUID uuid = DBFunc.everyone;
                if (!plot.trusted.contains(uuid)) {
                    PlayerFunctions.sendMessage(plr, C.T_WAS_NOT_ADDED);
                    return true;
                }
                plot.removeTrusted(uuid);
                DBFunc.removeTrusted(plr.getWorld().getName(), plot, Bukkit.getOfflinePlayer(args[1]));
                PlayerFunctions.sendMessage(plr, C.TRUSTED_REMOVED);
                return true;
            }
            if (!hasBeenOnServer(args[1])) {
                PlayerFunctions.sendMessage(plr, C.PLAYER_HAS_NOT_BEEN_ON);
                return true;
            }
            UUID uuid = null;
            if (Bukkit.getPlayerExact(args[1]) != null) {
                uuid = Bukkit.getPlayerExact(args[1]).getUniqueId();
            } else {
                uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
            }
            if (uuid == null) {
                PlayerFunctions.sendMessage(plr, C.PLAYER_HAS_NOT_BEEN_ON);
                return true;
            }
            if (!plot.trusted.contains(uuid)) {
                PlayerFunctions.sendMessage(plr, C.T_WAS_NOT_ADDED);
                return true;
            }
            plot.removeTrusted(uuid);
            DBFunc.removeTrusted(plr.getWorld().getName(), plot, Bukkit.getOfflinePlayer(args[1]));
            PlayerPlotTrustedEvent event = new PlayerPlotTrustedEvent(plr, plot, uuid, false);
            Bukkit.getPluginManager().callEvent(event);
            PlayerFunctions.sendMessage(plr, C.TRUSTED_REMOVED);
        } else {
            PlayerFunctions.sendMessage(plr, C.TRUSTED_NEED_ARGUMENT);
            return true;
        }
        return true;
    }
}
