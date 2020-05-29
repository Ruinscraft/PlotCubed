// PlotCubed start
package com.plotsquared.bukkit;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotBossBar;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class BukkitPlotBossBar extends PlotBossBar {

    private BossBar bossBar;

    public BukkitPlotBossBar(String title, BarColor color, BarStyle style) {
        bossBar = Bukkit.createBossBar(title, color, style);
    }

    public static BarColor getBarColor(PlotBarColor plotBarColor) {
        for (BarColor barColor : BarColor.values()) {
            if (barColor.name().equals(plotBarColor.name())) {
                return barColor;
            }
        }

        return null;
    }

    public static BarStyle getBarStyle(PlotBarStyle plotBarStyle) {
        for (BarStyle barStyle : BarStyle.values()) {
            if (barStyle.name().equals(plotBarStyle.name())) {
                return barStyle;
            }
        }

        return null;
    }

    public static PlotBarColor getPlotBarColor(BarColor barColor) {
        for (PlotBarColor plotBarColor : PlotBarColor.values()) {
            if (plotBarColor.name().equals(barColor.name())) {
                return plotBarColor;
            }
        }

        return null;
    }

    public static PlotBarStyle getPlotBarStyle(BarStyle barStyle) {
        for (PlotBarStyle plotBarStyle : PlotBarStyle.values()) {
            if (plotBarStyle.name().equals(barStyle.name())) {
                return plotBarStyle;
            }
        }

        return null;
    }

    @Override
    public void setTitle(String title) {
        bossBar.setTitle(title);
    }

    @Override
    public void setColor(PlotBarColor color) {
        bossBar.setColor(getBarColor(color));
    }

    @Override
    public void setStyle(PlotBarStyle style) {
        bossBar.setStyle(getBarStyle(style));
    }

    @Override
    public void setPct(double pct) {
        bossBar.setProgress(pct);
    }

    @Override
    public void addPlayer(PlotPlayer plotPlayer) {
        BukkitPlayer bukkitPlayer = (BukkitPlayer) plotPlayer;
        Player player = bukkitPlayer.player;

        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    @Override
    public void removePlayer(PlotPlayer plotPlayer) {
        BukkitPlayer bukkitPlayer = (BukkitPlayer) plotPlayer;
        Player player = bukkitPlayer.player;
        bossBar.removePlayer(player);
    }

    @Override
    public String getTitle() {
        return bossBar.getTitle();
    }

    @Override
    public PlotBarColor getColor() {
        return getPlotBarColor(bossBar.getColor());
    }

    @Override
    public PlotBarStyle getStyle() {
        return getPlotBarStyle(bossBar.getStyle());
    }

    @Override
    public double getPct() {
        return bossBar.getProgress();
    }

    @Override
    public Set<PlotPlayer> getPlayers() {
        Set<PlotPlayer> plotPlayers = new HashSet<>();
        for (Player player : bossBar.getPlayers()) {
            plotPlayers.add(PlotPlayer.wrap(player));
        }
        return plotPlayers;
    }
}
// PlotCubed end