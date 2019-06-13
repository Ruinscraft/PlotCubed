package com.github.intellectualsites.plotsquared.plot.uuid;

import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class UUIDWrapper {

    @NotNull public abstract UUID getUUID(PlotPlayer player);

    public abstract UUID getUUID(OfflinePlotPlayer player);

    public abstract UUID getUUID(String name);

    public abstract OfflinePlotPlayer getOfflinePlayer(UUID uuid);

    public abstract OfflinePlotPlayer getOfflinePlayer(String name);

    public abstract OfflinePlotPlayer[] getOfflinePlayers();
}
