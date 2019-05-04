package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.ByteArrayUtilities;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.UUID;

@CommandDeclaration(command = "grant", category = CommandCategory.CLAIMING,
    usage = "/plot grant <check|add> [player]", permission = "plots.grant",
    requiredType = RequiredType.NONE) public class Grant extends Command {

    public Grant() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        checkTrue(args.length >= 1 && args.length <= 2, Captions.COMMAND_SYNTAX, getUsage());
        final String arg0 = args[0].toLowerCase();
        switch (arg0) {
            case "add":
            case "check":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_GRANT.f(arg0))) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_GRANT.f(arg0));
                    return;
                }
                if (args.length > 2) {
                    break;
                }
                final UUID uuid;
                if (args.length == 2) {
                    uuid = UUIDHandler.getUUIDFromString(args[1]);
                } else {
                    uuid = player.getUUID();
                }
                if (uuid == null) {
                    Captions.INVALID_PLAYER.send(player, args[1]);
                    return;
                }
                MainUtil.getPersistentMeta(uuid, "grantedPlots", new RunnableVal<byte[]>() {
                    @Override public void run(byte[] array) {
                        if (arg0.equals("check")) { // check
                            int granted;
                            if (array == null) {
                                granted = 0;
                            } else {
                                granted = ByteArrayUtilities.bytesToInteger(array);
                            }
                            Captions.GRANTED_PLOTS.send(player, granted);
                        } else { // add
                            int amount;
                            if (array == null) {
                                amount = 1;
                            } else {
                                amount = 1 + ByteArrayUtilities.bytesToInteger(array);
                            }
                            boolean replace = array != null;
                            String key = "grantedPlots";
                            byte[] rawData = ByteArrayUtilities.integerToBytes(amount);
                            PlotPlayer online = UUIDHandler.getPlayer(uuid);
                            if (online != null) {
                                online.setPersistentMeta(key, rawData);
                            } else {
                                DBFunc.addPersistentMeta(uuid, key, rawData, replace);
                            }
                        }
                    }
                });
        }
        Captions.COMMAND_SYNTAX.send(player, getUsage());
    }
}
