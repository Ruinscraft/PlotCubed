package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.RequiredType;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.util.PlotGameMode;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ConsolePlayer extends PlotPlayer {

    private static ConsolePlayer instance;

    private ConsolePlayer() {
        PlotArea area = PlotSquared.get().getFirstPlotArea();
        Location loc;
        if (area != null) {
            RegionWrapper region = area.getRegion();
            loc = new Location(area.worldname, region.minX + region.maxX / 2, 0,
                region.minZ + region.maxZ / 2);
        } else {
            loc = new Location("world", 0, 0, 0);
        }
        setMeta("location", loc);
    }

    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = new ConsolePlayer();
            instance.teleport(instance.getLocation());
        }
        return instance;
    }

    @Override public boolean canTeleport(Location loc) {
        return true;
    }

    @Override public Location getLocation() {
        return this.getMeta("location");
    }

    @Override public Location getLocationFull() {
        return getLocation();
    }

    @Nonnull @Override public UUID getUUID() {
        return DBFunc.EVERYONE;
    }

    @Override public long getLastPlayed() {
        return 0;
    }

    @Override public boolean hasPermission(String permission) {
        return true;
    }

    @Override public boolean isPermissionSet(String permission) {
        return true;
    }

    @Override public void sendMessage(String message) {
        PlotSquared.log(message);
    }

    @Override public void teleport(Location location) {
        setMeta(PlotPlayer.META_LAST_PLOT, location.getPlot());
        setMeta(PlotPlayer.META_LOCATION, location);
    }

    @Override public boolean isOnline() {
        return true;
    }

    @Override public String getName() {
        return "*";
    }

    @Override public void setCompassTarget(Location location) {
    }

    @Override public void setAttribute(String key) {
    }

    @Override public boolean getAttribute(String key) {
        return false;
    }

    @Override public void removeAttribute(String key) {
    }

    @Override public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }

    @Override public void setWeather(PlotWeather weather) {
    }

    @Override public PlotGameMode getGameMode() {
        return PlotGameMode.NOT_SET;
    }

    @Override public void setGameMode(PlotGameMode gameMode) {
    }

    @Override public void setTime(long time) {
    }

    @Override public boolean getFlight() {
        return true;
    }

    @Override public void setFlight(boolean fly) {
    }

    @Override public void playMusic(Location location, PlotBlock id) {
    }

    @Override public void kick(String message) {
    }

    @Override public void stopSpectating() {
    }

    // PlotCubed start
    @Override public boolean canSee(PlotPlayer player) {
        return false;
    }
    // PlotCubed end

    @Override public boolean isBanned() {
        return false;
    }

}
