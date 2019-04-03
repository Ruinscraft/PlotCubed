// PlotCubed start
package com.github.intellectualsites.plotsquared.plot.object;

import java.util.Set;

public abstract class PlotBossBar {

    public abstract void setTitle(String title);
    public abstract void setColor(PlotBarColor color);
    public abstract void setStyle(PlotBarStyle style);
    public abstract void setPct(double pct);

    public abstract void addPlayer(PlotPlayer player);
    public abstract void removePlayer(PlotPlayer player);

    public abstract String getTitle();
    public abstract PlotBarColor getColor();
    public abstract PlotBarStyle getStyle();
    public abstract double getPct();
    public abstract Set<PlotPlayer> getPlayers();

    public enum PlotBarStyle {
        SEGMENTED_10,
        SEGMENTED_12,
        SEGMENTED_20,
        SEGMENTED_6,
        SOLID
    }

    public enum PlotBarColor {
        BLUE,
        GREEN,
        PINK,
        PURPLE,
        RED,
        WHITE,
        YELLOW
    }
}
// PlotCubed end