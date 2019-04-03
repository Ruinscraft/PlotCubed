package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

@CommandDeclaration(command = "toggle", aliases = {"attribute"}, permission = "plots.use",
    description = "Toggle per user settings", requiredType = RequiredType.NONE,
    category = CommandCategory.SETTINGS) public class Toggle extends Command {
    public Toggle() {
        super(MainCommand.getInstance(), true);
    }

    @CommandDeclaration(command = "chatspy", aliases = {"spy"},
        permission = "plots.admin.command.chat", description = "Toggle admin chat spying")
    public void chatspy(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "chatspy")) {
            MainUtil.sendMessage(player, Captions.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, Captions.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(command = "worldedit", aliases = {"we", "wea"},
        permission = "plots.worldedit.bypass", description = "Toggle worldedit area restrictions")
    public void worldedit(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "worldedit")) {
            MainUtil.sendMessage(player, Captions.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, Captions.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(command = "chat", permission = "plots.toggle.chat",
        description = "Toggle plot chat")
    public void chat(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "chat")) {
            MainUtil.sendMessage(player, Captions.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, Captions.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(command = "clear-confirmation",
        permission = "plots.admin.command.autoclear", description = "Toggle autoclear confirmation")
    public void clearConfirmation(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "ignoreExpireTask")) {
            MainUtil.sendMessage(player, Captions.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, Captions.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(command = "titles", permission = "plots.toggle.titles",
        description = "Toggle plot title messages")
    public void titles(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        PlotArea area = player.getApplicablePlotArea();
        boolean chat = area != null && area.PLOT_CHAT;
        if (toggle(player, "disabletitles") != chat) {
            MainUtil.sendMessage(player, Captions.TOGGLE_ENABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, Captions.TOGGLE_DISABLED, command.toString());
        }
    }

    public boolean toggle(PlotPlayer player, String key) {
        if (player.getAttribute(key)) {
            player.removeAttribute(key);
            return true;
        } else {
            player.setAttribute(key);
            return false;
        }
    }
}
