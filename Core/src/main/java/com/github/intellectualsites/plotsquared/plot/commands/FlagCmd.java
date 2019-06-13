package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.flag.IntegerFlag;
import com.github.intellectualsites.plotsquared.plot.flag.ListFlag;
import com.github.intellectualsites.plotsquared.plot.flag.PlotBlockListFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.*;

@CommandDeclaration(command = "setflag", aliases = {"f", "flag", "setf", "setflag"},
    usage = "/plot flag <set|remove|add|list|info> <flag> <value>", description = "Set plot flags",
    category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE,
    permission = "plots.flag") public class FlagCmd extends SubCommand {

    private boolean checkPermValue(PlotPlayer player, Flag flag, String key, String value) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm =
            Captions.PERMISSION_SET_FLAG_KEY_VALUE.f(key.toLowerCase(), value.toLowerCase());
        if (flag instanceof IntegerFlag && MathMan.isInteger(value)) {
            try {
                int numeric = Integer.parseInt(value);
                perm = perm.substring(0, perm.length() - value.length() - 1);
                if (numeric > 0) {
                    int checkRange = PlotSquared.get().getPlatform().equalsIgnoreCase("bukkit") ?
                        numeric :
                        Settings.Limit.MAX_PLOTS;
                    final boolean result = player.hasPermissionRange(perm, checkRange) >= numeric;
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                            Captions.PERMISSION_SET_FLAG_KEY_VALUE
                                .f(key.toLowerCase(), value.toLowerCase()));
                    }
                    return result;
                }

            } catch (NumberFormatException ignore) {
            }
        } else if (flag instanceof PlotBlockListFlag) {
            final PlotBlockListFlag blockListFlag = (PlotBlockListFlag) flag;
            final HashSet<PlotBlock> parsedBlocks = blockListFlag.parseValue(value);
            for (final PlotBlock block : parsedBlocks) {
                final String permission = Captions.PERMISSION_SET_FLAG_KEY_VALUE
                    .f(key.toLowerCase(), block.getRawId().toString().toLowerCase());
                final boolean result = Permissions.hasPermission(player, permission);
                if (!result) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                        Captions.PERMISSION_SET_FLAG_KEY_VALUE
                            .f(key.toLowerCase(), value.toLowerCase()));
                    return false;
                }
            }
            return true;
        }
        final boolean result = Permissions.hasPermission(player, perm);
        if (!result) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                Captions.PERMISSION_SET_FLAG_KEY_VALUE.f(key.toLowerCase(), value.toLowerCase()));
        }
        return result;
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        /*
         *  plot flag set fly true
         *  plot flag remove fly
         *  plot flag remove use 1,3
         *  plot flag add use 2,4
         *  plot flag list
         */
        if (args.length == 0) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                "/plot flag <set|remove|add|list|info>");
            return false;
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_SET_FLAG_OTHER)) {
            MainUtil
                .sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_SET_FLAG_OTHER);
            return false;
        }
        Flag<?> flag = null;
        if (args.length > 1) {
            flag = FlagManager.getFlag(args[1]);
            if (flag == null || flag.isReserved()) {
                boolean suggested = false;
                try {
                    StringComparison<Flag<?>> stringComparison =
                        new StringComparison<>(args[1], Flags.getFlags());
                    String best = stringComparison.getBestMatch();
                    if (best != null) {
                        MainUtil.sendMessage(player, Captions.NOT_VALID_FLAG_SUGGESTED, best);
                        suggested = true;
                    }
                } catch (final Exception ignored) { /* Happens sometimes because of mean code */ }
                if (!suggested) {
                    MainUtil.sendMessage(player, Captions.NOT_VALID_FLAG);
                }
                return false;
            }
        }
        switch (args[0].toLowerCase()) {
            case "info": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_SET_FLAG)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION, "plots.flag.info");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                // flag key
                MainUtil.sendMessage(player, Captions.FLAG_KEY, flag.getName());
                // flag type
                MainUtil.sendMessage(player, Captions.FLAG_TYPE, flag.getClass().getSimpleName());
                // Flag type description
                MainUtil.sendMessage(player, Captions.FLAG_DESC, flag.getValueDescription());
                return true;
            }
            case "set": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_SET_FLAG)) {
                    MainUtil
                        .sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_SET_FLAG);
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot flag set <flag> <value>");
                    return false;
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                if (!checkPermValue(player, flag, args[1], value)) {
                    return false;
                }
                Object parsed = flag.parseValue(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + flag.getValueDescription());
                    return false;
                }
                boolean result = plot.setFlag(flag, parsed);
                if (!result) {
                    MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, Captions.FLAG_ADDED);
                return true;
            }
            case "remove": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_FLAG_REMOVE)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                        Captions.PERMISSION_FLAG_REMOVE);
                    return false;
                }
                if (args.length != 2 && args.length != 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot flag remove <flag> [values]");
                    return false;
                }
                if (!Permissions.hasPermission(player,
                    Captions.PERMISSION_SET_FLAG_KEY.f(args[1].toLowerCase()))) {
                    if (args.length != 3) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                            Captions.PERMISSION_SET_FLAG_KEY.f(args[1].toLowerCase()));
                        return false;
                    }
                    for (String entry : args[2].split(",")) {
                        if (!checkPermValue(player, flag, args[1], entry)) {
                            return false;
                        }
                    }
                }
                if (args.length == 3 && flag instanceof ListFlag) {
                    String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                    Optional<? extends Collection> flag1 =
                        plot.getFlag((Flag<? extends Collection<?>>) flag);
                    if (flag1.isPresent()) {
                        boolean o = flag1.get().removeAll((Collection) flag.parseValue(value));
                        if (o) {
                            if (flag1.get().isEmpty()) {
                                final boolean result = plot.removeFlag(flag);
                                if (result) {
                                    MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                                } else {
                                    MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                                }
                                return true;
                            } else {
                                MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                            }
                        } else {
                            MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                            return false;
                        }
                    }
                    DBFunc.setFlags(plot, plot.getFlags());
                    return true;
                } else {
                    boolean result = plot.removeFlag(flag);
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                        return false;
                    }
                }
                if (flag == Flags.TIME) {
                    player.setTime(Long.MAX_VALUE);
                } else if (flag == Flags.WEATHER) {
                    player.setWeather(PlotWeather.RESET);
                }
                MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                return true;
            }
            case "add":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_FLAG_ADD)) {
                    MainUtil
                        .sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_FLAG_ADD);
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot flag add <flag> <values>");
                    return false;
                }
                for (String entry : args[2].split(",")) {
                    if (!checkPermValue(player, flag, args[1], entry)) {
                        return false;
                    }
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                Object parsed = flag.parseValue(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + flag.getValueDescription());
                    return false;
                }
                Object val = parsed;
                if (flag instanceof ListFlag) {
                    Optional<? extends Collection> flag1 =
                        plot.getFlag((Flag<? extends Collection<?>>) flag);
                    if (flag1.isPresent()) {
                        boolean o = flag1.get().addAll((Collection) parsed);
                        if (o) {
                            MainUtil.sendMessage(player, Captions.FLAG_ADDED);
                            val = flag1.get();
                        } else {
                            MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
                            return false;
                        }
                    }
                }
                boolean result = plot.setFlag(flag, val);
                if (!result) {
                    MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, Captions.FLAG_ADDED);
                return true;
            case "list":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_FLAG_LIST)) {
                    MainUtil
                        .sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_FLAG_LIST);
                    return false;
                }
                if (args.length > 1) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag list");
                    return false;
                }
                HashMap<String, ArrayList<String>> flags = new HashMap<>();
                for (Flag<?> flag1 : Flags.getFlags()) {
                    String type = flag1.getClass().getSimpleName();
                    flags.computeIfAbsent(type, k -> new ArrayList<>());
                    flags.get(type).add(flag1.getName());
                }
                StringBuilder message = new StringBuilder();
                String prefix = "";
                for (Map.Entry<String, ArrayList<String>> entry : flags.entrySet()) {
                    String category = entry.getKey();
                    List<String> flagNames = entry.getValue();
                    Collections.sort(flagNames);
                    message.append(prefix).append("&6").append(category).append(": &7")
                        .append(StringMan.join(flagNames, ", "));
                    prefix = "\n";
                }
                MainUtil.sendMessage(player, message.toString());
                return true;
        }
        MainUtil
            .sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
        return false;
    }
}
