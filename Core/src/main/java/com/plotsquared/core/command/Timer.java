// PlotCubed start
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotBossBar;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CommandDeclaration(command = "timer",
        description = "Start a timer on a plot",
        permission = "plots.timer",
        usage = "/plot timer <duration>",
        category = CommandCategory.ROLEPLAY,
        requiredType = RequiredType.PLAYER)
public class Timer extends SubCommand {

    private static Map<PlotTimer, PlotBossBar> activeTimers = new HashMap<>();

    private static int taskID = -1;

    private static final Runnable updateTimersTask = () -> {
        for (PlotTimer plotTimer : activeTimers.keySet()) {
            if (plotTimer.endTime <= System.currentTimeMillis()) {
                cancel(plotTimer);
                continue;
            }

            String timeLeft = plotTimer.getTimeLeftWords();
            double pct = plotTimer.getPctLeft();

            if (pct >= 1.0) {
                pct = 0.99;
            }

            if (activeTimers.get(plotTimer) == null) {
                PlotBossBar bossBar = PlotSquared.imp().createBossBar(timeLeft, PlotBossBar.PlotBarColor.GREEN, PlotBossBar.PlotBarStyle.SOLID);
                bossBar.setPct(pct);

                activeTimers.put(plotTimer, bossBar);
                continue;
            }

            activeTimers.get(plotTimer).setTitle(timeLeft);
            activeTimers.get(plotTimer).setPct(pct);

            if (pct < 0.30) {
                if (pct < 0.15) {
                    activeTimers.get(plotTimer).setColor(PlotBossBar.PlotBarColor.RED);
                } else {
                    activeTimers.get(plotTimer).setColor(PlotBossBar.PlotBarColor.YELLOW);
                }
            }

            Plot plot = plotTimer.plot;

            for (PlotPlayer plotPlayer : plot.getPlayersInPlot()) {
                activeTimers.get(plotTimer).addPlayer(plotPlayer);
            }

            for (PlotPlayer plotPlayer : activeTimers.get(plotTimer).getPlayers()) {
                if (plotPlayer.getCurrentPlot() != plot) {
                    activeTimers.get(plotTimer).removePlayer(plotPlayer);
                }
            }
        }
    };

    private class PlotTimer {
        private final Plot plot;
        private final long startedAt;
        private final long endTime;

        public PlotTimer(Plot plot, long startedAt, long duration) {
            this.plot = plot;
            this.startedAt = startedAt;
            this.endTime = startedAt + duration;
        }

        public double getPctLeft() {
            if (System.currentTimeMillis() >= endTime) {
                return 0;
            }

            if (System.currentTimeMillis() <= startedAt) {
                return 100;
            }

            double pctWhole = ((endTime - System.currentTimeMillis()) * 100 / (endTime - startedAt));

            return pctWhole / 100;
        }

        public String getTimeLeftWords() {
            long elapsedTime = endTime - System.currentTimeMillis();

            return MainUtil.secToTime(TimeUnit.MILLISECONDS.toSeconds(elapsedTime));
        }
    }

    private static void cancel(PlotTimer plotTimer) {
        if (!activeTimers.containsKey(plotTimer)) {
            return;
        }

        PlotBossBar bossBar = activeTimers.get(plotTimer);

        if (bossBar != null) {
            for (PlotPlayer plotPlayer : bossBar.getPlayers()) {
                bossBar.removePlayer(plotPlayer);
            }
        }

        activeTimers.remove(plotTimer);
    }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        checkTrue(args.length >= 1, Captions.COMMAND_SYNTAX, getUsage());

        if (taskID == -1) {
            taskID = TaskManager.runTaskRepeat(updateTimersTask, 20);
        }

        Plot plot = player.getCurrentPlot();

        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Captions.PERMISSION_USE_TIMER_OTHER.getTranslated())) {
            return sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_USE_TIMER_OTHER.getTranslated());
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            for (PlotTimer plotTimer : activeTimers.keySet()) {
                if (plotTimer.plot == plot) {
                    cancel(plotTimer);
                    return sendMessage(player, Captions.TIMER_CANCELLED);
                }
            }
            return sendMessage(player, Captions.TIMER_NOT_FOUND);
        }

        String time = String.join(" ", args);

        long sec = 0;

        try {
            sec = MainUtil.timeToSec(time);
        } catch (Exception e) {
            return sendMessage(player, Captions.TIMER_INCORRECT_FORMAT);
        }

        if (sec <= 0) {
            return sendMessage(player, Captions.TIMER_INCORRECT_FORMAT);
        }

        if (sec > 28800) {
            return sendMessage(player, Captions.TIMER_TOO_LONG);
        }

        for (PlotTimer plotTimer : activeTimers.keySet()) {
            if (plotTimer.plot == plot) {
                return sendMessage(player, Captions.TIMER_ALREADY_EXISTS);
            }
        }

        PlotTimer plotTimer = new PlotTimer(plot, System.currentTimeMillis(), TimeUnit.SECONDS.toMillis(sec));

        activeTimers.put(plotTimer, null);

        updateTimersTask.run();

        return sendMessage(player, Captions.TIMER_CREATED);
    }
}
// PlotCubed end