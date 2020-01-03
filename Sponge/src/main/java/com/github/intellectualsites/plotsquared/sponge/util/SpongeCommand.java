package com.github.intellectualsites.plotsquared.sponge.util;

import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.sponge.SpongeMain;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SpongeCommand implements CommandCallable {

    @Override public CommandResult process(CommandSource source, String arguments)
        throws CommandException {
        TaskManager.runTask(() -> {
            String id = source.getIdentifier();
            PlotPlayer plotPlayer = null;
            try {
                UUID uuid = UUID.fromString(id);

                Optional<Player> player = SpongeMain.THIS.getServer().getPlayer(uuid);
                if (player.isPresent()) {
                    plotPlayer = SpongeUtil.getPlayer(player.get());
                }
            } catch (Exception ignored) {
                plotPlayer = ConsolePlayer.getConsole();
            }
            MainCommand.onCommand(plotPlayer,
                arguments.isEmpty() ? new String[] {} : arguments.split(" "));
        });
        return CommandResult.success();
    }

    @Override public List<String> getSuggestions(CommandSource source, String arguments,
        Location<World> targetPosition) throws CommandException {
        if (!(source instanceof Player)) {
            return ImmutableList.of();
        }
        PlotPlayer player = SpongeUtil.getPlayer((Player) source);
        String[] args = arguments.split(" ");
        if (args.length == 0) {
            return Collections.singletonList(MainCommand.getInstance().toString());
        }
        Collection objects = MainCommand.getInstance().tab(player, args, arguments.endsWith(" "));
        if (objects != null && !objects.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (Object o : objects) {
                result.add(o.toString());
            }
            return result;
        }
        List<String> names = new ArrayList<>();
        String startsWith = arguments.endsWith(" ") ? "" : args[args.length - 1];
        for (Map.Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            String name = entry.getKey();
            if (name.startsWith(startsWith)) {
                names.add(name);
            }
        }
        return names;
    }

    @Override public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Shows plot help"));
    }

    @Override public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("/plot"));
    }

    @Override public Text getUsage(CommandSource source) {
        return Text.of("/plot <command>");
    }

}
