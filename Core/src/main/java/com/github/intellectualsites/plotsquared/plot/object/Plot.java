package com.github.intellectualsites.plotsquared.plot.object;

import static java.util.concurrent.TimeUnit.SECONDS;


import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.generator.SquarePlotWorld;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.plot.object.comment.PlotComment;
import com.github.intellectualsites.plotsquared.plot.object.schematic.Schematic;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The plot class<br>
 * [IMPORTANT]
 * - Unclaimed plots will not have persistent information.
 * - Any information set/modified in an unclaimed object may not be reflected in other instances
 * - Using the `new` operator will create an unclaimed plot instance
 * - Use the methods from the PlotArea/PS/Location etc to get existing plots
 */
public class Plot {

    public static final int MAX_HEIGHT = 256;

    /**
     * @deprecated raw access is deprecated
     */
    @Deprecated private static Set<Plot> connected_cache;
    private static Set<CuboidRegion> regions_cache;

    @NotNull private final PlotId id;

    /**
     * plot owner
     * (Merged plots can have multiple owners)
     * Direct access is Deprecated: use getOwners()
     *
     * @deprecated
     */
    @Deprecated public UUID owner;

    /**
     * Has the plot changed since the last save cycle?
     */
    public boolean countsTowardsMax = true;

    /**
     * Represents whatever the database manager needs it to: <br>
     * - A value of -1 usually indicates the plot will not be stored in the DB<br>
     * - A value of 0 usually indicates that the DB manager hasn't set a value<br>
     *
     * @deprecated magical
     */
    @Deprecated public int temp;

    /**
     * Plot creation timestamp (not accurate if the plot was created before this was implemented)<br>
     * - Milliseconds since the epoch<br>
     */
    private long timestamp;
    // PlotCubed start
    /**
     * List of plot warps.
     */
    private HashSet<PlotWarp> warps;

    /**
     * Transient set of users who have visited this plot.
     */
    private transient Set<UUID> visitors;
    // PlotCubed end
    /**
     * List of trusted (with plot permissions).
     */
    private HashSet<UUID> trusted;

    /**
     * List of members users (with plot permissions).
     */
    private HashSet<UUID> members;

    /**
     * List of denied players.
     */
    private HashSet<UUID> denied;

    /**
     * External settings class.
     * - Please favor the methods over direct access to this class<br>
     * - The methods are more likely to be left unchanged from version changes<br>
     */
    private PlotSettings settings;

    private PlotArea area;

    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     *
     * @see FlagManager
     */
    private ConcurrentHashMap<String, Object> meta;

    /**
     * The cached origin plot.
     * - The origin plot is used for plot grouping and relational data
     */
    private Plot origin;

    /**
     * Constructor for a new plot.
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @param area the PlotArea where the plot is located
     * @param id the plot id
     * @param owner the plot owner
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(PlotArea area, @NotNull PlotId id, UUID owner) {
        this.area = area;
        this.id = id;
        this.owner = owner;
    }

    /**
     * Constructor for an unowned plot.
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @param area the PlotArea where the plot is located
     * @param id the plot id
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(PlotArea area, @NotNull PlotId id) {
        this.area = area;
        this.id = id;
    }

    /**
     * Constructor for a temporary plot (use -1 for temp)<br>
     * The database will ignore any queries regarding temporary plots.
     * Please note that some bulk plot management functions may still affect temporary plots (TODO: fix this)
     *
     * @param area the PlotArea where the plot is located
     * @param id the plot id
     * @param owner the owner of the plot
     * @param temp Represents whatever the database manager needs it to
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(PlotArea area, @NotNull PlotId id, UUID owner, int temp) {
        this.area = area;
        this.id = id;
        this.owner = owner;
        this.temp = temp;
    }

    /**
     * Constructor for a saved plots (Used by the database manager when plots are fetched)
     *
     * @param id the plot id
     * @param owner the plot owner
     * @param trusted the plot trusted players
     * @param denied the plot denied players
     * @param merged an array giving merged plots
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(@NotNull PlotId id, UUID owner, HashSet<UUID> trusted, HashSet<UUID> members,
        HashSet<UUID> denied, String alias, BlockLoc position, Collection<Flag> flags,
        PlotArea area, boolean[] merged, long timestamp, int temp) {
        this.id = id;
        this.area = area;
        this.owner = owner;
        this.settings = new PlotSettings();
        this.members = members;
        this.trusted = trusted;
        this.denied = denied;
        this.settings.setAlias(alias);
        this.settings.setPosition(position);
        this.settings.setMerged(merged);
        if (flags != null) {
            for (Flag flag : flags) {
                this.settings.flags.put(flag, flag);
            }
        }
        this.timestamp = timestamp;
        this.temp = temp;
    }

    /**
     * Gets a plot from a string e.g. [area];[id]
     *
     * @param defaultArea if no area is specified
     * @param string plot id/area + id
     * @return New or existing plot object
     */
    public static Plot fromString(PlotArea defaultArea, String string) {
        String[] split = string.split(";|,");
        if (split.length == 2) {
            if (defaultArea != null) {
                PlotId id = PlotId.fromString(split[0] + ';' + split[1]);
                return defaultArea.getPlotAbs(id);
            }
        } else if (split.length == 3) {
            PlotArea pa = PlotSquared.get().getPlotArea(split[0], null);
            if (pa != null) {
                PlotId id = PlotId.fromString(split[1] + ';' + split[2]);
                return pa.getPlotAbs(id);
            }
        } else if (split.length == 4) {
            PlotArea pa = PlotSquared.get().getPlotArea(split[0], split[1]);
            if (pa != null) {
                PlotId id = PlotId.fromString(split[1] + ';' + split[2]);
                return pa.getPlotAbs(id);
            }
        }
        return null;
    }

    /**
     * Return a new/cached plot object at a given location.
     *
     * @param location the location of the plot
     * @return plot at location or null
     * @see PlotPlayer#getCurrentPlot() if a player is expected here.
     */
    public static Plot getPlot(Location location) {
        PlotArea pa = location.getPlotArea();
        if (pa != null) {
            return pa.getPlot(location);
        }
        return null;
    }

    public String getWorldName() {
        return area.worldname;
    }

    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     *
     * @param key   metadata key
     * @param value metadata value
     * @see FlagManager
     */
    public void setMeta(String key, Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<>();
        }
        this.meta.put(key, value);
    }

    /**
     * Gets the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     *
     * @param key metadata key to get value for
     * @return Object value
     */
    public Object getMeta(String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }

    /**
     * Delete the metadata for a key<br>
     * - metadata is session only
     * - deleting other plugin's metadata may cause issues
     *
     * @param key key to delete
     */
    public void deleteMeta(String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }

    /**
     * Gets the cluster this plot is associated with
     *
     * @return the PlotCluster object, or null
     */
    public PlotCluster getCluster() {
        return this.getArea().getCluster(this.id);
    }

    /**
     * Efficiently get the players currently inside this plot<br>
     * - Will return an empty list if no players are in the plot<br>
     * - Remember, you can cast a PlotPlayer to it's respective implementation (BukkitPlayer, SpongePlayer) to obtain the player object
     *
     * @return list of PlotPlayer(s) or an empty list
     */
    public List<PlotPlayer> getPlayersInPlot() {
        ArrayList<PlotPlayer> players = new ArrayList<>();
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer plotPlayer = entry.getValue();
            if (this.equals(plotPlayer.getCurrentPlot())) {
                players.add(plotPlayer);
            }
        }
        return players;
    }

    /**
     * Checks if the plot has an owner.
     *
     * @return false if there is no owner
     */
    public boolean hasOwner() {
        return this.owner != null;
    }

    /**
     * Checks if a UUID is a plot owner (merged plots may have multiple owners)
     *
     * @param uuid the player uuid
     * @return if the provided uuid is the owner of the plot
     */
    public boolean isOwner(@NotNull UUID uuid) {
        if (uuid.equals(this.getOwner())) {
            return true;
        }
        if (!isMerged()) {
            return false;
        }
        Set<Plot> connected = getConnectedPlots();
        return connected.stream().anyMatch(current -> uuid.equals(current.getOwner()));
    }

    public boolean isOwnerAbs(UUID uuid) {
        return uuid.equals(this.getOwner());
    }

    /**
     * plot owner
     * (Merged plots can have multiple owners)
     * Direct access is Deprecated: use getOwners()
     *
     * @deprecated
     */
    @Deprecated public UUID getOwner() {
        if (MainUtil.isServerOwned(this)) {
            return DBFunc.SERVER;
        }
        return this.owner;
    }

    /**
     * Sets the plot owner (and update the database)
     *
     * @param owner uuid to set as owner
     */
    public void setOwner(UUID owner) {
        if (!hasOwner()) {
            this.owner = owner;
            create();
            return;
        }
        if (!isMerged()) {
            if (!this.owner.equals(owner)) {
                this.owner = owner;
                DBFunc.setOwner(this, owner);
            }
            return;
        }
        for (Plot current : getConnectedPlots()) {
            if (!owner.equals(current.owner)) {
                current.owner = owner;
                DBFunc.setOwner(current, owner);
            }
        }
    }

    /**
     * Gets a immutable set of owner UUIDs for a plot (supports multi-owner mega-plots).
     * <p>
     * This method cannot be used to add or remove owners from a plot.
     * </p>
     *
     * @return the plot owners
     */
    public Set<UUID> getOwners() {
        if (this.getOwner() == null) {
            return ImmutableSet.of();
        }
        if (isMerged()) {
            Set<Plot> plots = getConnectedPlots();
            Plot[] array = plots.toArray(new Plot[plots.size()]);
            ImmutableSet.Builder<UUID> owners = ImmutableSet.builder();
            UUID last = this.getOwner();
            owners.add(this.getOwner());
            for (Plot current : array) {
                if (last == null || current.getOwner().getMostSignificantBits() != last
                    .getMostSignificantBits()) {
                    owners.add(current.getOwner());
                    last = current.getOwner();
                }
            }
            return owners.build();
        }
        return ImmutableSet.of(this.getOwner());
    }

    /**
     * Checks if the player is either the owner or on the trusted/added list.
     *
     * @param uuid uuid to check
     * @return true if the player is added/trusted or is the owner
     */
    public boolean isAdded(UUID uuid) {
        if (this.owner == null || getDenied().contains(uuid)) {
            return false;
        }
        if (isOwner(uuid)) {
            return true;
        }
        if (getMembers().contains(uuid)) {
            return isOnline();
        }
        if (getTrusted().contains(uuid) || getTrusted().contains(DBFunc.EVERYONE)) {
            return true;
        }
        if (getMembers().contains(DBFunc.EVERYONE)) {
            return isOnline();
        }
        return false;
    }

    /**
     * Checks if the player is not permitted on this plot.
     *
     * @param uuid uuid to check
     * @return boolean false if the player is allowed to enter
     */
    public boolean isDenied(UUID uuid) {
        return this.denied != null && (this.denied.contains(DBFunc.EVERYONE) && !this.isAdded(uuid)
            || !this.isAdded(uuid) && this.denied.contains(uuid));
    }

    /**
     * Gets the {@link PlotId} of this plot.
     *
     * @return the PlotId for this plot
     */
    @NotNull public PlotId getId() {
        return this.id;
    }

    /**
     * Gets the plot world object for this plot<br>
     * - The generic PlotArea object can be casted to its respective class for more control (e.g. HybridPlotWorld)
     *
     * @return PlotArea
     */
    public PlotArea getArea() {
        return this.area;
    }

    /**
     * Assigns this plot to a plot area.<br>
     * (Mostly used during startup when worlds are being created)<br>
     * <p>
     * Do not use this unless you absolutely know what you are doing.
     * </p>
     *
     * @param area area to assign to
     */
    public void setArea(PlotArea area) {
        if (this.getArea() == area) {
            return;
        }
        if (this.getArea() != null) {
            this.area.removePlot(this.id);
        }
        this.area = area;
        area.addPlot(this);
    }

    /**
     * Gets the plot manager object for this plot<br>
     * - The generic PlotManager object can be casted to its respective class for more control (e.g. HybridPlotManager)
     *
     * @return PlotManager
     */
    public PlotManager getManager() {
        return this.area.getPlotManager();
    }

    /**
     * Gets or create plot settings.
     *
     * @return PlotSettings
     * @deprecated use equivalent plot method; please file github issue if one does not exist.
     */
    @Deprecated public PlotSettings getSettings() {
        if (this.settings == null) {
            this.settings = new PlotSettings();
        }
        return this.settings;
    }

    /**
     * Returns true if the plot is not merged, or it is the base
     * plot of multiple merged plots.
     *
     * @return Boolean
     */
    public boolean isBasePlot() {
        return !this.isMerged() || this.equals(this.getBasePlot(false));
    }

    /**
     * The base plot is an arbitrary but specific connected plot. It is useful for the following:<br>
     * - Merged plots need to be treated as a single plot for most purposes<br>
     * - Some data such as home location needs to be associated with the group rather than each plot<br>
     * - If the plot is not merged it will return itself.<br>
     * - The result is cached locally
     *
     * @return base Plot
     */
    public Plot getBasePlot(boolean recalculate) {
        if (this.origin != null && !recalculate) {
            if (this.equals(this.origin)) {
                return this;
            }
            return this.origin.getBasePlot(false);
        }
        if (!this.isMerged()) {
            this.origin = this;
            return this.origin;
        }
        this.origin = this;
        PlotId min = this.id;
        for (Plot plot : this.getConnectedPlots()) {
            if (plot.id.y < min.y || plot.id.y == min.y && plot.id.x < min.x) {
                this.origin = plot;
                min = plot.id;
            }
        }
        for (Plot plot : this.getConnectedPlots()) {
            plot.origin = this.origin;
        }
        return this.origin;
    }

    /**
     * Checks if this plot is merged in any direction.
     *
     * @return true if this plot is merged, otherwise false
     */
    public boolean isMerged() {
        return getSettings().getMerged(0) || getSettings().getMerged(2) || getSettings()
            .getMerged(1) || getSettings().getMerged(3);
    }

    /**
     * Gets the timestamp of when the plot was created (unreliable)<br>
     * - not accurate if the plot was created before this was implemented<br>
     * - Milliseconds since the epoch<br>
     *
     * @return the creation date of the plot
     */
    public long getTimestamp() {
        if (this.timestamp == 0) {
            this.timestamp = System.currentTimeMillis();
        }
        return this.timestamp;
    }

    // PlotCubed start
    public HashSet<PlotWarp> getWarps() {
        if (warps == null) {
            warps = new HashSet<>();
        }
        return warps;
    }

    public PlotWarp getWarpByName(String name) {
        for (PlotWarp warp : getWarps()) {
            if (warp.name.equalsIgnoreCase(name)) {
                return warp;
            }
        }
        return null;
    }

    public boolean hasWarp(String name) {
        return getWarpByName(name) != null;
    }

    public void setWarp(PlotWarp warp) {
        getWarps().add(warp);
    }

    public void removeWarp(PlotWarp warp) {
        for (Plot current : getConnectedPlots()) {
            if (current.getWarps().remove(warp)) {
                DBFunc.removeWarp(current, warp);
            }
        }
    }

    public void clearWarps() {
        for (Plot current : getConnectedPlots()) {
            for (PlotWarp warp : getWarps()) {
                DBFunc.removeWarp(current, warp);
            }

            current.getWarps().clear();
        }
    }

    public void addWarp(PlotWarp warp) {
        for (Plot current : getConnectedPlots()) {
            if (current.getWarps().add(warp)) {
                DBFunc.setWarp(current, warp);
            }
        }
    }

    public Set<UUID> getVisitors() {
        if (this.visitors == null) {
            this.visitors = new HashSet<>();
        }
        return visitors;
    }

    public boolean addVisitor(UUID uuid) {
        boolean added = false;
        for (Plot current : getConnectedPlots()) {
            if (current.getVisitors().add(uuid)) {
                added = true;
                DBFunc.addVisit(current, uuid);
            }
        }
        return added;
    }

    public void deleteVisits() {
        for (Plot current : getConnectedPlots()) {
            current.clearVisitors();
            DBFunc.deleteVisits(current);
        }
    }

    public void clearVisitors() {
        for (Plot current : getConnectedPlots()) {
            current.getVisitors().clear();
        }
    }
    // PlotCubed end

    /**
     * Gets if the plot is merged in a direction<br>
     * ------- Actual -------<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * ----- Artificial -----<br>
     * 4 = north-east<br>
     * 5 = south-east<br>
     * 6 = south-west<br>
     * 7 = north-west<br>
     * ----------<br>
     * //todo these artificial values are way too confusing.
     * Note: A plot that is merged north and east will not be merged northeast if the northeast plot is not part of the same group<br>
     *
     * @param dir direction to check for merged plot
     * @return true if merged in that direction
     */
    public boolean getMerged(int dir) {
        if (this.settings == null) {
            return false;
        }
        switch (dir) {
            case 0:
            case 1:
            case 2:
            case 3:
                return this.getSettings().getMerged(dir);
            case 7:
                int i = dir - 4;
                int i2 = 0;
                if (this.getSettings().getMerged(i2)) {
                    if (this.getSettings().getMerged(i)) {
                        if (this.area.getPlotAbs(this.id.getRelative(i)).getMerged(i2)) {
                            if (this.area.getPlotAbs(this.id.getRelative(i2)).getMerged(i)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            case 4:
            case 5:
            case 6:
                i = dir - 4;
                i2 = dir - 3;
                return this.getSettings().getMerged(i2) && this.getSettings().getMerged(i)
                    && this.area.getPlotAbs(this.id.getRelative(i)).getMerged(i2) && this.area
                    .getPlotAbs(this.id.getRelative(i2)).getMerged(i);

        }
        return false;
    }

    /**
     * Gets the denied users.
     *
     * @return a set of denied users
     */
    public HashSet<UUID> getDenied() {
        if (this.denied == null) {
            this.denied = new HashSet<>();
        }
        return this.denied;
    }

    /**
     * Sets the denied users for this plot.
     *
     * @param uuids uuids to deny
     */
    public void setDenied(Set<UUID> uuids) {
        boolean larger = uuids.size() > getDenied().size();
        HashSet<UUID> intersection;
        if (larger) {
            intersection = new HashSet<>(getDenied());
        } else {
            intersection = new HashSet<>(uuids);
        }
        if (larger) {
            intersection.retainAll(uuids);
        } else {
            intersection.retainAll(getDenied());
        }
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(getDenied());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            removeDenied(uuid);
        }
        for (UUID uuid : uuids) {
            addDenied(uuid);
        }
    }

    /**
     * Gets the trusted users.
     *
     * @return a set of trusted users
     */
    public HashSet<UUID> getTrusted() {
        if (this.trusted == null) {
            this.trusted = new HashSet<>();
        }
        return this.trusted;
    }

    /**
     * Sets the trusted users for this plot.
     *
     * @param uuids uuids to trust
     */
    public void setTrusted(Set<UUID> uuids) {
        boolean larger = uuids.size() > getTrusted().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? getTrusted() : uuids);
        intersection.retainAll(larger ? uuids : getTrusted());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(getTrusted());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            removeTrusted(uuid);
        }
        for (UUID uuid : uuids) {
            addTrusted(uuid);
        }
    }

    /**
     * Gets the members
     *
     * @return a set of members
     */
    public HashSet<UUID> getMembers() {
        if (this.members == null) {
            this.members = new HashSet<>();
        }
        return this.members;
    }

    /**
     * Sets the members for this plot.
     *
     * @param uuids uuids to set member status for
     */
    public void setMembers(Set<UUID> uuids) {
        boolean larger = uuids.size() > getMembers().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? getMembers() : uuids);
        intersection.retainAll(larger ? uuids : getMembers());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(getMembers());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            removeMember(uuid);
        }
        for (UUID uuid : uuids) {
            addMember(uuid);
        }
    }

    /**
     * Denies a player from this plot. (updates database as well)
     *
     * @param uuid the uuid of the player to deny.
     */
    public void addDenied(UUID uuid) {
        for (Plot current : getConnectedPlots()) {
            if (current.getDenied().add(uuid)) {
                DBFunc.setDenied(current, uuid);
            }
        }
    }

    /**
     * Add someone as a helper (updates database as well)
     *
     * @param uuid the uuid of the player to trust
     */
    public void addTrusted(UUID uuid) {
        for (Plot current : getConnectedPlots()) {
            if (current.getTrusted().add(uuid)) {
                DBFunc.setTrusted(current, uuid);
            }
        }
    }

    /**
     * Add someone as a trusted user (updates database as well)
     *
     * @param uuid the uuid of the player to add as a member
     */
    public void addMember(UUID uuid) {
        for (Plot current : getConnectedPlots()) {
            if (current.getMembers().add(uuid)) {
                DBFunc.setMember(current, uuid);
            }
        }
    }

    /**
     * Sets the plot owner (and update the database)
     *
     * @param owner uuid to set as owner
     * @param initiator player initiating set owner
     * @return boolean
     */
    public boolean setOwner(UUID owner, PlotPlayer initiator) {
        boolean result = EventUtil.manager
            .callOwnerChange(initiator, this, owner, hasOwner() ? this.owner : null, hasOwner());
        if (!result) {
            return false;
        }
        if (!hasOwner()) {
            this.owner = owner;
            create();
            return true;
        }
        if (!isMerged()) {
            if (!this.owner.equals(owner)) {
                this.owner = owner;
                DBFunc.setOwner(this, owner);
            }
            return true;
        }
        for (Plot current : getConnectedPlots()) {
            if (!owner.equals(current.owner)) {
                current.owner = owner;
                DBFunc.setOwner(current, owner);
            }
        }
        return true;
    }

    /**
     * Clear a plot.
     *
     * @param whenDone A runnable to execute when clearing finishes, or null
     * @see this#clear(boolean, boolean, Runnable)
     * @see #deletePlot(Runnable) to clear and delete a plot
     */
    public void clear(Runnable whenDone) {
        this.clear(false, false, whenDone);
    }

    public boolean clear(boolean checkRunning, final boolean isDelete, final Runnable whenDone) {
        if (checkRunning && this.getRunning() != 0) {
            return false;
        }
        if (isDelete) {
            if (!EventUtil.manager.callDelete(this)) {
                return false;
            }
        } else {
            if (!EventUtil.manager.callClear(this)) {
                return false;
            }
        }
        final Set<CuboidRegion> regions = this.getRegions();
        final Set<Plot> plots = this.getConnectedPlots();
        final ArrayDeque<Plot> queue = new ArrayDeque<>(plots);
        if (isDelete) {
            this.removeSign();
        }
        this.unlinkPlot(true, !isDelete);
        // PlotCubed start
        this.clearWarps();
        // PlotCubed end
        final PlotManager manager = this.area.getPlotManager();
        Runnable run = new Runnable() {
            @Override public void run() {
                if (queue.isEmpty()) {
                    Runnable run = () -> {
                        for (CuboidRegion region : regions) {
                            Location[] corners = MainUtil.getCorners(getWorldName(), region);
                            ChunkManager.manager.clearAllEntities(corners[0], corners[1]);
                        }
                        TaskManager.runTask(whenDone);
                    };
                    for (Plot current : plots) {
                        if (isDelete || current.owner == null) {
                            manager.unClaimPlot(current, null);
                        } else {
                            manager.claimPlot(current);
                        }
                    }
                    GlobalBlockQueue.IMP.addEmptyTask(run);
                    return;
                }
                Plot current = queue.poll();
                if (Plot.this.area.TERRAIN != 0) {
                    try {
                        ChunkManager.manager
                            .regenerateRegion(current.getBottomAbs(), current.getTopAbs(), false,
                                this);
                    } catch (UnsupportedOperationException exception) {
                        MainUtil.sendMessage(null,
                            "Please ask md_5 to fix regenerateChunk() because it breaks plugins. We apologize for the inconvenience");
                        return;
                    }
                    return;
                }
                manager.clearPlot(current, this);
            }
        };
        run.run();
        return true;
    }

    /**
     * Sets the biome for a plot asynchronously.
     *
     * @param biome    The biome e.g. "forest"
     * @param whenDone The task to run when finished, or null
     */
    public void setBiome(final BiomeType biome, final Runnable whenDone) {
        final ArrayDeque<CuboidRegion> regions = new ArrayDeque<>(this.getRegions());
        final int extendBiome;
        if (area instanceof SquarePlotWorld) {
            extendBiome = (((SquarePlotWorld) area).ROAD_WIDTH > 0) ? 1 : 0;
        } else {
            extendBiome = 0;
        }
        Runnable run = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    Plot.this.refreshChunks();
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion region = regions.poll();
                Location pos1 = new Location(getWorldName(), region.getMinimumPoint().getX() - extendBiome, region.getMinimumPoint().getY(),
                    region.getMinimumPoint().getZ() - extendBiome);
                Location pos2 = new Location(getWorldName(), region.getMaximumPoint().getX() + extendBiome, region.getMaximumPoint().getY(),
                    region.getMaximumPoint().getZ() + extendBiome);
                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override public void run(int[] value) {
                        BlockVector2 loc = BlockVector2.at(value[0], value[1]);
                        long start = System.currentTimeMillis();
                        ChunkManager.manager.loadChunk(getWorldName(), loc, false);
                        long end = System.currentTimeMillis();
                        PlotSquared.debug("[Biome Operation] Loading chunk took: " + TimeUnit.MILLISECONDS.toSeconds(end - start));
                        MainUtil.setBiome(getWorldName(), value[2], value[3], value[4], value[5],
                            biome);
                        start = System.currentTimeMillis();
                        ChunkManager.manager.unloadChunk(getWorldName(), loc, true);
                        end = System.currentTimeMillis();
                        PlotSquared.debug("[Biome Operation] Unloading chunk took: " + TimeUnit.MILLISECONDS.toSeconds(end - start));
                    }
                }, this, 5);

            }
        };
        run.run();
    }

    /**
     * Unlink the plot and all connected plots.
     *
     * @param createRoad whether to recreate road
     * @param createSign whether to recreate signs
     * @return success/!cancelled
     */
    public boolean unlinkPlot(boolean createRoad, boolean createSign) {
        if (!this.isMerged()) {
            return false;
        }
        final Set<Plot> plots = this.getConnectedPlots();
        ArrayList<PlotId> ids = new ArrayList<>(plots.size());
        for (Plot current : plots) {
            current.setHome(null);
            ids.add(current.getId());
        }
        boolean result = EventUtil.manager.callUnlink(this.area, ids);
        if (!result) {
            return false;
        }
        this.clearRatings();
        if (createSign) {
            this.removeSign();
        }
        PlotManager manager = this.area.getPlotManager();
        if (createRoad) {
            manager.startPlotUnlink(ids);
        }
        if (this.area.TERRAIN != 3 && createRoad) {
            for (Plot current : plots) {
                if (current.getMerged(Direction.EAST)) {
                    manager.createRoadEast(current);
                    if (current.getMerged(Direction.SOUTH)) {
                        manager.createRoadSouth(current);
                        if (current.getMerged(Direction.SOUTHEAST)) {
                            manager.createRoadSouthEast(current);
                        }
                    }
                }
                if (current.getMerged(Direction.SOUTH)) {
                    manager.createRoadSouth(current);
                }
            }
        }
        for (Plot current : plots) {
            boolean[] merged = new boolean[] {false, false, false, false};
            current.setMerged(merged);
        }
        if (createSign) {
            GlobalBlockQueue.IMP.addEmptyTask(() -> {
                for (Plot current : plots) {
                    current.setSign(MainUtil.getName(current.owner));
                }
            });
        }
        if (createRoad) {
            manager.finishPlotUnlink(ids);
        }
        return true;
    }

    /**
     * Sets the sign for a plot to a specific name
     *
     * @param name name
     */
    public void setSign(@NotNull String name) {
        if (!isLoaded()) {
            return;
        }
        if (!PlotSquared.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(() -> Plot.this.setSign(name));
            return;
        }
        PlotManager manager = this.area.getPlotManager();
        if (this.area.ALLOW_SIGNS) {
            Location location = manager.getSignLoc(this);
            String id = this.id.x + ";" + this.id.y;
            String[] lines =
                new String[] {Captions.OWNER_SIGN_LINE_1.formatted().replaceAll("%id%", id),
                    Captions.OWNER_SIGN_LINE_2.formatted().replaceAll("%id%", id).replaceAll(
                        "%plr%", name),
                    Captions.OWNER_SIGN_LINE_3.formatted().replaceAll("%id%", id).replaceAll(
                        "%plr%", name),
                    Captions.OWNER_SIGN_LINE_4.formatted().replaceAll("%id%", id).replaceAll(
                        "%plr%", name)};
            WorldUtil.IMP
                .setSign(this.getWorldName(), location.getX(), location.getY(), location.getZ(),
                    lines);
        }
    }

    protected boolean isLoaded() {
        return WorldUtil.IMP.isWorld(getWorldName());
    }

    /**
     * This will return null if the plot hasn't been analyzed
     *
     * @return analysis of plot
     */
    public PlotAnalysis getComplexity(Settings.Auto_Clear settings) {
        return PlotAnalysis.getAnalysis(this, settings);
    }

    public void analyze(RunnableVal<PlotAnalysis> whenDone) {
        PlotAnalysis.analyzePlot(this, whenDone);
    }

    /**
     * Sets a flag for this plot
     *
     * @param flag  Flag to set
     * @param value Flag value
     */
    public <V> boolean setFlag(Flag<V> flag, Object value) {
        if (flag == Flags.KEEP && ExpireManager.IMP != null) {
            ExpireManager.IMP.updateExpired(this);
        }
        return FlagManager.addPlotFlag(this, flag, value);
    }

    /**
     * Remove a flag from this plot
     *
     * @param flag the flag to remove
     * @return success
     */
    public boolean removeFlag(Flag<?> flag) {
        return FlagManager.removePlotFlag(this, flag);
    }

    /**
     * Gets the flag for a given key
     *
     * @param key Flag to get value for
     */
    public <V> Optional<V> getFlag(Flag<V> key) {
        return FlagManager.getPlotFlag(this, key);
    }

    /**
     * Gets the flag for a given key
     *
     * @param key          the flag
     * @param defaultValue if the key is null, the value to return
     */
    public <V> V getFlag(Flag<V> key, V defaultValue) {
        V value = FlagManager.getPlotFlagRaw(this, key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * Delete a plot (use null for the runnable if you don't need to be notified on completion)
     *
     * @see PlotSquared#removePlot(Plot, boolean)
     * @see #clear(boolean, boolean, Runnable) to simply clear a plot
     */
    public boolean deletePlot(final Runnable whenDone) {
        if (!this.hasOwner()) {
            return false;
        }
        final Set<Plot> plots = this.getConnectedPlots();
        this.clear(false, true, () -> {
            for (Plot current : plots) {
                current.unclaim();
            }
            TaskManager.runTask(whenDone);
        });
        return true;
    }

    /**
     * Count the entities in a plot
     *
     * @return array of entity counts
     * @see ChunkManager#countEntities(Plot)
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     */
    public int[] countEntities() {
        int[] count = new int[6];
        for (Plot current : this.getConnectedPlots()) {
            int[] result = ChunkManager.manager.countEntities(current);
            count[0] += result[0];
            count[1] += result[1];
            count[2] += result[2];
            count[3] += result[3];
            count[4] += result[4];
            count[5] += result[5];
        }
        return count;
    }

    /**
     * Returns true if a previous task was running
     *
     * @return true if a previous task is running
     */
    public int addRunning() {
        int value = this.getRunning();
        for (Plot plot : this.getConnectedPlots()) {
            plot.setMeta("running", value + 1);
        }
        return value;
    }

    /**
     * Decrement the number of tracked tasks this plot is running<br>
     * - Used to track/limit the number of things a player can do on the plot at once
     *
     * @return previous number of tasks (int)
     */
    public int removeRunning() {
        int value = this.getRunning();
        if (value < 2) {
            for (Plot plot : this.getConnectedPlots()) {
                plot.deleteMeta("running");
            }
        } else {
            for (Plot plot : this.getConnectedPlots()) {
                plot.setMeta("running", value - 1);
            }
        }
        return value;
    }

    /**
     * Gets the number of tracked running tasks for this plot<br>
     * - Used to track/limit the number of things a player can do on the plot at once
     *
     * @return number of tasks (int)
     */
    public int getRunning() {
        Integer value = (Integer) this.getMeta("running");
        return value == null ? 0 : value;
    }

    /**
     * Unclaim the plot (does not modify terrain). Changes made to this plot will not be reflected in unclaimed plot objects.
     *
     * @return false if the Plot has no owner, otherwise true.
     */
    public boolean unclaim() {
        if (this.owner == null) {
            return false;
        }
        for (Plot current : getConnectedPlots()) {
            List<PlotPlayer> players = current.getPlayersInPlot();
            for (PlotPlayer pp : players) {
                PlotListener.plotExit(pp, current);
            }
            getArea().removePlot(getId());
            DBFunc.delete(current);
            current.owner = null;
            current.settings = null;
            for (PlotPlayer pp : players) {
                PlotListener.plotEntry(pp, current);
            }
        }
        return true;
    }

    /**
     * Unlink a plot and remove the roads
     *
     * @return true if plot was linked
     * @see this#unlinkPlot(boolean, boolean)
     */
    public boolean unlink() {
        return this.unlinkPlot(true, true);
    }

    public Location getCenter() {
        Location[] corners = getCorners();
        Location top = corners[0];
        Location bot = corners[1];
        Location location =
            new Location(this.getWorldName(), MathMan.average(bot.getX(), top.getX()),
            MathMan.average(bot.getY(), top.getY()), MathMan.average(bot.getZ(), top.getZ()));
        if (!isLoaded()) {
            return location;
        }
        int y = WorldUtil.IMP.getHighestBlock(getWorldName(), location.getX(), location.getZ());
        if (area.ALLOW_SIGNS) {
            y = Math.max(y, getManager().getSignLoc(this).getY());
        }
        location.setY(1 + y);
        return location;
    }

    public Location getSide() {
        CuboidRegion largest = getLargestRegion();
        int x = (largest.getMaximumPoint().getX() >> 1) - (largest.getMinimumPoint().getX() >> 1) + largest.getMinimumPoint().getX();
        int z = largest.getMinimumPoint().getZ() - 1;
        PlotManager manager = getManager();
        int y = isLoaded() ? WorldUtil.IMP.getHighestBlock(getWorldName(), x, z) : 62;
        if (area.ALLOW_SIGNS && (y <= 0 || y >= 255)) {
            y = Math.max(y, manager.getSignLoc(this).getY() - 1);
        }
        return new Location(getWorldName(), x, y + 1, z);
    }

    /**
     * Return the home location for the plot
     *
     * @return Home location
     */
    public Location getHome() {
        BlockLoc home = this.getPosition();
        if (home == null || home.getX() == 0 && home.getZ() == 0) {
            return this.getDefaultHome(true);
        } else {
            Location bottom = this.getBottomAbs();
            Location location =
                new Location(bottom.getWorld(), bottom.getX() + home.getX(), bottom.getY() + home
                    .getY(),
                    bottom.getZ() + home.getZ(), home.getYaw(), home.getPitch());
            if (!isLoaded()) {
                return location;
            }
            if (!WorldUtil.IMP.getBlock(location).getBlockType().getMaterial().isAir()) {
                location.setY(Math.max(1 + WorldUtil.IMP
                        .getHighestBlock(this.getWorldName(), location.getX(), location.getZ()),
                    bottom.getY()));
            }
            return location;
        }
    }

    /**
     * Sets the home location
     *
     * @param location location to set as home
     */
    public void setHome(BlockLoc location) {
        Plot plot = this.getBasePlot(false);
        if (location != null && new BlockLoc(0, 0, 0).equals(location)) {
            return;
        }
        plot.getSettings().setPosition(location);
        if (location != null) {
            DBFunc.setPosition(plot, plot.getSettings().getPosition().toString());
            return;
        }
        DBFunc.setPosition(plot, null);
    }

    /**
     * Gets the default home location for a plot<br>
     * - Ignores any home location set for that specific plot
     *
     * @return Location
     */
    public Location getDefaultHome() {
        return getDefaultHome(false);
    }

    public Location getDefaultHome(boolean member) {
        Plot plot = this.getBasePlot(false);
        PlotLoc loc = member ? area.DEFAULT_HOME : area.NONMEMBER_HOME;
        if (loc != null) {
            int x;
            int z;
            if (loc.getX() == Integer.MAX_VALUE && loc.getZ() == Integer.MAX_VALUE) {
                // center
                CuboidRegion largest = plot.getLargestRegion();
                x = (largest.getMaximumPoint().getX() >> 1) - (largest.getMinimumPoint().getX() >> 1) + largest.getMinimumPoint().getX();
                z = (largest.getMaximumPoint().getZ() >> 1) - (largest.getMinimumPoint().getZ() >> 1) + largest.getMinimumPoint().getZ();
            } else {
                // specific
                Location bot = plot.getBottomAbs();
                x = bot.getX() + loc.getX();
                z = bot.getZ() + loc.getZ();
            }
            int y = loc.getY() < 1 ?
                (isLoaded() ? WorldUtil.IMP.getHighestBlock(plot.getWorldName(), x, z) + 1 : 63) :
                loc.getY();
            return new Location(plot.getWorldName(), x, y, z);
        }
        // Side
        return plot.getSide();
    }

    public double getVolume() {
        double count = 0;
        for (CuboidRegion region : getRegions()) {
            count +=
                (region.getMaximumPoint().getX() - (double) region.getMinimumPoint().getX() + 1) * (region.getMaximumPoint().getZ() - (double) region.getMinimumPoint().getZ() + 1)
                    * MAX_HEIGHT;
        }
        return count;
    }

    /**
     * Gets the average rating of the plot. This is the value displayed in /plot info
     *
     * @return average rating as double
     */
    public double getAverageRating() {
        Collection<Rating> ratings = this.getRatings().values();
        double sum = ratings.stream().mapToDouble(Rating::getAverageRating).sum();
        return sum / ratings.size();
    }

    /**
     * Sets a rating for a user<br>
     * - If the user has already rated, the following will return false
     *
     * @param uuid   uuid of rater
     * @param rating rating
     * @return success
     */
    public boolean addRating(UUID uuid, Rating rating) {
        Plot base = this.getBasePlot(false);
        PlotSettings baseSettings = base.getSettings();
        if (baseSettings.getRatings().containsKey(uuid)) {
            return false;
        }
        int aggregate = rating.getAggregate();
        baseSettings.getRatings().put(uuid, aggregate);
        DBFunc.setRating(base, uuid, aggregate);
        return true;
    }

    /**
     * Clear the ratings/likes for this plot
     */
    public void clearRatings() {
        Plot base = this.getBasePlot(false);
        PlotSettings baseSettings = base.getSettings();
        if (baseSettings.ratings != null && !baseSettings.getRatings().isEmpty()) {
            DBFunc.deleteRatings(base);
            baseSettings.ratings = null;
        }
    }

    public Map<UUID, Boolean> getLikes() {
        final Map<UUID, Boolean> map = new HashMap<>();
        final Map<UUID, Rating> ratings = this.getRatings();
        ratings.forEach((uuid, rating) -> map.put(uuid, rating.getLike()));
        return map;
    }

    /**
     * Gets the ratings associated with a plot<br>
     * - The rating object may contain multiple categories
     *
     * @return Map of user who rated to the rating
     */
    public HashMap<UUID, Rating> getRatings() {
        Plot base = this.getBasePlot(false);
        HashMap<UUID, Rating> map = new HashMap<>();
        if (!base.hasRatings()) {
            return map;
        }
        for (Entry<UUID, Integer> entry : base.getSettings().getRatings().entrySet()) {
            map.put(entry.getKey(), new Rating(entry.getValue()));
        }
        return map;
    }

    public boolean hasRatings() {
        Plot base = this.getBasePlot(false);
        return base.settings != null && base.settings.ratings != null;
    }

    /**
     * Resend all chunks inside the plot to nearby players<br>
     * This should not need to be called
     */
    public void refreshChunks() {
        LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(getWorldName(), false);
        HashSet<BlockVector2> chunks = new HashSet<>();
        for (CuboidRegion region : Plot.this.getRegions()) {
            for (int x = region.getMinimumPoint().getX() >> 4; x <= region.getMaximumPoint().getX() >> 4; x++) {
                for (int z = region.getMinimumPoint().getZ() >> 4; z <= region.getMaximumPoint().getZ() >> 4; z++) {
                    if (chunks.add(BlockVector2.at(x, z))) {
                        queue.refreshChunk(x, z);
                    }
                }
            }
        }
    }

    /**
     * Remove the plot sign if it is set.
     */
    public void removeSign() {
        PlotManager manager = this.area.getPlotManager();
        if (!this.area.ALLOW_SIGNS) {
            return;
        }
        Location location = manager.getSignLoc(this);
        LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(getWorldName(), false);
        queue.setBlock(location.getX(), location.getY(), location.getZ(), BlockTypes.AIR.getDefaultState());
        queue.flush();
    }

    /**
     * Sets the plot sign if plot signs are enabled.
     */
    public void setSign() {
        if (this.owner == null) {
            this.setSign("unknown");
            return;
        }
        String name = UUIDHandler.getName(this.owner);
        if (name == null) {
            this.setSign("unknown");
        } else {
            this.setSign(name);
        }
    }

    /**
     * Register a plot and create it in the database<br>
     * - The plot will not be created if the owner is null<br>
     * - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot
     * creation.
     *
     * @return true if plot was created successfully
     */
    public boolean create() {
        return this.create(this.owner, true);
    }

    public boolean claim(final PlotPlayer player, boolean teleport, String schematic) {
        if (!canClaim(player)) {
            return false;
        }
        return claim(player, teleport, schematic, true);
    }

    public boolean claim(final PlotPlayer player, boolean teleport, String schematic,
        boolean updateDB) {
        boolean result = EventUtil.manager.callClaim(player, this, false);
        if (updateDB) {
            if (!result || (!create(player.getUUID(), true))) {
                return false;
            }
        } else {
            area.addPlot(this);
        }
        setSign(player.getName());
        MainUtil.sendMessage(player, Captions.CLAIMED);
        if (teleport && Settings.Teleport.ON_CLAIM) {
            teleportPlayer(player);
        }
        PlotArea plotworld = getArea();
        if (plotworld.SCHEMATIC_ON_CLAIM) {
            Schematic sch;
            try {
                if (schematic == null || schematic.isEmpty()) {
                    sch = SchematicHandler.manager.getSchematic(plotworld.SCHEMATIC_FILE);
                } else {
                    sch = SchematicHandler.manager.getSchematic(schematic);
                    if (sch == null) {
                        sch = SchematicHandler.manager.getSchematic(plotworld.SCHEMATIC_FILE);
                    }
                }
            } catch (SchematicHandler.UnsupportedFormatException e) {
                e.printStackTrace();
                return true;
            }
            SchematicHandler.manager.paste(sch, this, 0, 1, 0, Settings.Schematics.PASTE_ON_TOP,
                new RunnableVal<Boolean>() {
                    @Override public void run(Boolean value) {
                        if (value) {
                            MainUtil.sendMessage(player, Captions.SCHEMATIC_PASTE_SUCCESS);
                        } else {
                            MainUtil.sendMessage(player, Captions.SCHEMATIC_PASTE_FAILED);
                        }
                    }
                });
        }
        plotworld.getPlotManager().claimPlot(this);
        return true;
    }

    /**
     * Register a plot and create it in the database<br>
     * - The plot will not be created if the owner is null<br>
     * - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot
     * creation.
     *
     * @param uuid   the uuid of the plot owner
     * @param notify notify
     * @return true if plot was created successfully
     */
    public boolean create(@NotNull UUID uuid, final boolean notify) {
        this.owner = uuid;
        Plot existing = this.area.getOwnedPlotAbs(this.id);
        if (existing != null) {
            throw new IllegalStateException("Plot already exists!");
        }
        if (notify) {
            Integer meta = (Integer) this.area.getMeta("worldBorder");
            if (meta != null) {
                this.updateWorldBorder();
            }
        }
        connected_cache = null;
        regions_cache = null;
        this.getTrusted().clear();
        this.getMembers().clear();
        this.getDenied().clear();
        this.settings = new PlotSettings();
        if (this.area.addPlot(this)) {
            DBFunc.createPlotAndSettings(this, () -> {
                PlotArea plotworld = Plot.this.area;
                if (notify && plotworld.AUTO_MERGE) {
                    Plot.this.autoMerge(Direction.ALL, Integer.MAX_VALUE, uuid, true);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Sets components such as border, wall, floor.
     * (components are generator specific)
     */
    @Deprecated
    public boolean setComponent(String component, String blocks) {
        BlockBucket parsed = Configuration.BLOCK_BUCKET.parseString(blocks);
        if (parsed != null && parsed.isEmpty()) {
            return false;
        }
        return this.setComponent(component, parsed.toPattern());
    }

    /**
     * Retrieve the biome of the plot.
     *
     * @return the name of the biome
     */
    public BiomeType getBiome() {
        Location location = this.getCenter();
        return WorldUtil.IMP.getBiome(location.getWorld(), location.getX(), location.getZ());
    }

    //TODO Better documentation needed.

    /**
     * Returns the top location for the plot.
     */
    public Location getTopAbs() {
        Location top = getManager().getPlotTopLocAbs(this.id);
        top.setWorld(getWorldName());
        return top;
    }

    //TODO Better documentation needed.

    /**
     * Returns the bottom location for the plot.
     */
    public Location getBottomAbs() {
        Location location = getManager().getPlotBottomLocAbs(this.id);
        location.setWorld(getWorldName());
        return location;
    }

    /**
     * Swaps the settings for two plots.
     *
     * @param plot     the plot to swap data with
     * @param whenDone the task to run at the end of this method.
     * @return
     */
    public boolean swapData(Plot plot, Runnable whenDone) {
        if (this.owner == null) {
            if (plot == null) {
                return false;
            }
            if (plot.hasOwner()) {
                plot.moveData(this, whenDone);
                return true;
            }
            return false;
        }
        if (plot == null) {
            this.moveData(plot, whenDone);
            return true;
        } else if (plot.getOwner() == null) {
            this.moveData(plot, whenDone);
            return true;
        }
        // Swap cached
        PlotId temp = new PlotId(this.getId().x, this.getId().y);
        this.getId().x = plot.getId().x;
        this.getId().y = plot.getId().y;
        plot.getId().x = temp.x;
        plot.getId().y = temp.y;
        this.area.removePlot(this.getId());
        plot.area.removePlot(plot.getId());
        this.getId().recalculateHash();
        plot.getId().recalculateHash();
        this.area.addPlotAbs(this);
        plot.area.addPlotAbs(plot);
        // Swap database
        DBFunc.swapPlots(plot, this);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }

    /**
     * Moves the settings for a plot.
     *
     * @param plot the plot to move
     * @param whenDone
     * @return
     */
    public boolean moveData(Plot plot, Runnable whenDone) {
        if (this.owner == null) {
            PlotSquared.debug(plot + " is unowned (single)");
            TaskManager.runTask(whenDone);
            return false;
        }
        if (plot.hasOwner()) {
            PlotSquared.debug(plot + " is unowned (multi)");
            TaskManager.runTask(whenDone);
            return false;
        }
        this.area.removePlot(this.id);
        this.getId().x = plot.getId().x;
        this.getId().y = plot.getId().y;
        this.getId().recalculateHash();
        this.area.addPlotAbs(this);
        DBFunc.movePlot(this, plot);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }

    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @return Location top of mega plot
     */
    public Location getExtendedTopAbs() {
        Location top = this.getTopAbs();
        if (!this.isMerged()) {
            return top;
        }
        if (this.getMerged(Direction.SOUTH)) {
            top.setZ(this.getRelative(Direction.SOUTH).getBottomAbs().getZ() - 1);
        }
        if (this.getMerged(Direction.EAST)) {
            top.setX(this.getRelative(Direction.EAST).getBottomAbs().getX() - 1);
        }
        return top;
    }

    /**
     * Gets the bottom location for a plot.<br>
     * - Does not respect mega plots<br>
     * - Merged plots, only the road will be considered part of the plot<br>
     *
     * @return Location bottom of mega plot
     */
    public Location getExtendedBottomAbs() {
        Location bot = this.getBottomAbs();
        if (!this.isMerged()) {
            return bot;
        }
        if (this.getMerged(Direction.NORTH)) {
            bot.setZ(this.getRelative(Direction.NORTH).getTopAbs().getZ() + 1);
        }
        if (this.getMerged(Direction.WEST)) {
            bot.setX(this.getRelative(Direction.WEST).getTopAbs().getX() + 1);
        }
        return bot;
    }

    /**
     * Returns the top and bottom location.<br>
     * - If the plot is not connected, it will return its own corners<br>
     * - the returned locations will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     *
     * @return new Location[] { bottom, top }
     * @deprecated as merged plots no longer need to be rectangular
     */
    @Deprecated public Location[] getCorners() {
        if (!this.isMerged()) {
            return new Location[] {this.getBottomAbs(), this.getTopAbs()};
        }
        return MainUtil.getCorners(this.getWorldName(), this.getRegions());
    }

    /**
     * Remove the east road section of a plot<br>
     * - Used when a plot is merged<br>
     */
    public void removeRoadEast() {
        if (this.area.TYPE != 0 && this.area.TERRAIN > 1) {
            if (this.area.TERRAIN == 3) {
                return;
            }
            Plot other = this.getRelative(Direction.EAST);
            Location bot = other.getBottomAbs();
            Location top = this.getTopAbs();
            Location pos1 = new Location(this.getWorldName(), top.getX(), 0, bot.getZ());
            Location pos2 = new Location(this.getWorldName(), bot.getX(), MAX_HEIGHT, top.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            this.area.getPlotManager().removeRoadEast(this);
        }
    }

    /**
     * @return
     * @deprecated in favor of getCorners()[0];<br>
     */
    // Won't remove as suggestion also points to deprecated method
    @Deprecated public Location getBottom() {
        return this.getCorners()[0];
    }

    /**
     * @return the top corner of the plot
     * @deprecated in favor of getCorners()[1];
     */
    // Won't remove as suggestion also points to deprecated method
    @Deprecated public Location getTop() {
        return this.getCorners()[1];
    }

    /**
     * Swap the plot contents and settings with another location<br>
     * - The destination must correspond to a valid plot of equal dimensions
     *
     * @param destination The other plot to swap with
     * @param whenDone    A task to run when finished, or null
     * @return boolean if swap was successful
     * @see ChunkManager#swap(Location, Location, Location, Location, Runnable) to swap terrain
     * @see this#swapData(Plot, Runnable) to swap plot settings
     * @see this#swapData(Plot, Runnable)
     */
    public boolean swap(Plot destination, Runnable whenDone) {
        return this.move(destination, whenDone, true);
    }

    /**
     * Moves the plot to an empty location<br>
     * - The location must be empty
     *
     * @param destination Where to move the plot
     * @param whenDone    A task to run when done, or null
     * @return if the move was successful
     */
    public boolean move(Plot destination, Runnable whenDone) {
        return this.move(destination, whenDone, false);
    }

    /**
     * Gets plot display name.
     *
     * @return alias if set, else id
     */
    @Override public String toString() {
        if (this.settings != null && this.settings.getAlias().length() > 1) {
            return this.settings.getAlias();
        }
        return this.area + ";" + this.id.x + ";" + this.id.y;
    }


    /**
     * Remove a denied player (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     *
     * @param uuid
     */
    public boolean removeDenied(UUID uuid) {
        if (uuid == DBFunc.EVERYONE && !denied.contains(uuid)) {
            boolean result = false;
            for (UUID other : new HashSet<>(getDenied())) {
                result = rmvDenied(other) || result;
            }
            return result;
        }
        return rmvDenied(uuid);
    }

    private boolean rmvDenied(UUID uuid) {
        for (Plot current : this.getConnectedPlots()) {
            if (current.getDenied().remove(uuid)) {
                DBFunc.removeDenied(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove a helper (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     *
     * @param uuid
     */
    public boolean removeTrusted(UUID uuid) {
        if (uuid == DBFunc.EVERYONE && !trusted.contains(uuid)) {
            boolean result = false;
            for (UUID other : new HashSet<>(getTrusted())) {
                result = rmvTrusted(other) || result;
            }
            return result;
        }
        return rmvTrusted(uuid);
    }

    private boolean rmvTrusted(UUID uuid) {
        for (Plot plot : this.getConnectedPlots()) {
            if (plot.getTrusted().remove(uuid)) {
                DBFunc.removeTrusted(plot, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove a trusted user (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     *
     * @param uuid
     */
    public boolean removeMember(UUID uuid) {
        if (this.members == null) {
            return false;
        }
        if (uuid == DBFunc.EVERYONE && !members.contains(uuid)) {
            boolean result = false;
            for (UUID other : new HashSet<>(this.members)) {
                result = rmvMember(other) || result;
            }
            return result;
        }
        return rmvMember(uuid);
    }

    private boolean rmvMember(UUID uuid) {
        for (Plot current : this.getConnectedPlots()) {
            if (current.getMembers().remove(uuid)) {
                DBFunc.removeMember(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Export the plot as a schematic to the configured output directory.
     *
     * @return
     */
    public void export(final RunnableVal<Boolean> whenDone) {
        SchematicHandler.manager.getCompoundTag(this, new RunnableVal<CompoundTag>() {
            @Override public void run(final CompoundTag value) {
                if (value == null) {
                    if (whenDone != null) {
                        whenDone.value = false;
                        TaskManager.runTask(whenDone);
                    }
                } else {
                    TaskManager.runTaskAsync(() -> {
                        String name = Plot.this.id + "," + Plot.this.area + ',' + MainUtil
                            .getName(Plot.this.owner);
                        boolean result = SchematicHandler.manager.save(value,
                            Settings.Paths.SCHEMATICS + File.separator + name + ".schem");
                        if (whenDone != null) {
                            whenDone.value = result;
                            TaskManager.runTask(whenDone);
                        }
                    });
                }
            }
        });
    }

    /**
     * Upload the plot as a schematic to the configured web interface.
     *
     * @param whenDone value will be null if uploading fails
     */
    public void upload(final RunnableVal<URL> whenDone) {
        SchematicHandler.manager.getCompoundTag(this, new RunnableVal<CompoundTag>() {
            @Override public void run(CompoundTag value) {
                SchematicHandler.manager.upload(value, null, null, whenDone);
            }
        });
    }

    /**
     * Upload this plot as a world file<br>
     * - The mca files are each 512x512, so depending on the plot size it may also download adjacent plots<br>
     * - Works best when (plot width + road width) % 512 == 0<br>
     *
     * @param whenDone
     * @see WorldUtil
     */
    public void uploadWorld(RunnableVal<URL> whenDone) {
        WorldUtil.IMP.upload(this, null, null, whenDone);
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Plot other = (Plot) obj;
        return this.hashCode() == other.hashCode() && this.id.equals(other.id)
            && this.area == other.area;
    }

    /**
     * Gets the plot hashcode<br>
     * Note: The hashcode is unique if:<br>
     * - Plots are in the same world<br>
     * - The x,z coordinates are between Short.MIN_VALUE and Short.MAX_VALUE<br>
     *
     * @return integer.
     */
    @Override public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * Gets the flags specific to this plot<br>
     * - Does not take default flags into account<br>
     *
     * @return
     */
    public HashMap<Flag<?>, Object> getFlags() {
        return this.getSettings().flags;
    }

    /**
     * Sets a flag for this plot.
     *
     * @param flags
     */
    public void setFlags(HashMap<Flag<?>, Object> flags) {
        FlagManager.setPlotFlags(this, flags);
    }

    /**
     * Gets the plot alias.
     * - Returns an empty string if no alias is set
     *
     * @return The plot alias
     */
    public String getAlias() {
        if (this.settings == null) {
            return "";
        }
        return this.settings.getAlias();
    }

    /**
     * Sets the plot alias.
     *
     * @param alias The alias
     */
    public void setAlias(String alias) {
        for (Plot current : this.getConnectedPlots()) {
            String name = this.getSettings().getAlias();
            if (alias == null) {
                alias = "";
            }
            if (name.equals(alias)) {
                return;
            }
            current.getSettings().setAlias(alias);
            DBFunc.setAlias(current, alias);
        }
    }

    /**
     * Sets the raw merge data<br>
     * - Updates DB<br>
     * - Does not modify terrain<br>
     *
     * @param direction
     * @param value
     */
    public void setMerged(Direction direction, boolean value) {
        if (this.getSettings().setMerged(direction, value)) {
            if (value) {
                Plot other = this.getRelative(direction).getBasePlot(false);
                if (!other.equals(this.getBasePlot(false))) {
                    Plot base = other.id.y < this.id.y
                        || other.id.y == this.id.y && other.id.x < this.id.x ? other : this.origin;
                    this.origin.origin = base;
                    other.origin = base;
                    this.origin = base;
                    connected_cache = null;
                }
            } else {
                if (this.origin != null) {
                    this.origin.origin = null;
                    this.origin = null;
                }
                connected_cache = null;
            }
            DBFunc.setMerged(this, this.getSettings().getMerged());
            regions_cache = null;
        }
    }

    /**
     * Gets the merged array.
     *
     * @return boolean [ north, east, south, west ]
     */
    public boolean[] getMerged() {
        return this.getSettings().getMerged();
    }

    /**
     * Sets the raw merge data<br>
     * - Updates DB<br>
     * - Does not modify terrain<br>
     * Gets if the plot is merged in a direction<br>
     * ----------<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * ----------<br>
     * Note: Diagonal merging (4-7) must be done by merging the corresponding plots.
     *
     * @param merged
     */
    public void setMerged(boolean[] merged) {
        this.getSettings().setMerged(merged);
        DBFunc.setMerged(this, merged);
        clearCache();
    }

    public void clearCache() {
        connected_cache = null;
        regions_cache = null;
        if (this.origin != null) {
            this.origin.origin = null;
            this.origin = null;
        }
    }

    /**
     * Gets the set home location or 0,0,0 if no location is set<br>
     * - Does not take the default home location into account
     *
     * @return
     * @see #getHome()
     */
    public BlockLoc getPosition() {
        return this.getSettings().getPosition();
    }

    /**
     * Check if a plot can be claimed by the provided player.
     *
     * @param player the claiming player
     * @return
     */
    public boolean canClaim(@Nullable PlotPlayer player) {
        PlotCluster cluster = this.getCluster();
        if (cluster != null && player != null) {
            if (!cluster.isAdded(player.getUUID()) && !Permissions
                .hasPermission(player, "plots.admin.command.claim")) {
                return false;
            }
        }
        return this.guessOwner() == null && !isMerged();
    }

    /**
     * Guess the owner of a plot either by the value in memory, or the sign data<br>
     * Note: Recovering from sign information is useful if e.g. PlotMe conversion wasn't successful
     *
     * @return UUID
     */
    public UUID guessOwner() {
        if (this.hasOwner()) {
            return this.owner;
        }
        if (!this.area.ALLOW_SIGNS) {
            return null;
        }
        try {
            final Location location = this.getManager().getSignLoc(this);
            String[] lines = TaskManager.IMP.sync(new RunnableVal<String[]>() {
                @Override public void run(String[] value) {
                    ChunkManager.manager
                        .loadChunk(location.getWorld(), location.getBlockVector2(), false);
                    this.value = WorldUtil.IMP.getSign(location);
                }
            });
            if (lines == null) {
                return null;
            }
            loop:
            for (int i = 4; i > 0; i--) {
                String caption = Captions.valueOf("OWNER_SIGN_LINE_" + i).getTranslated();
                int index = caption.indexOf("%plr%");
                if (index < 0) {
                    continue;
                }
                String line = lines[i - 1];
                if (line.length() <= index) {
                    return null;
                }
                String name = line.substring(index);
                if (name.isEmpty()) {
                    return null;
                }
                UUID owner = UUIDHandler.getUUID(name, null);
                if (owner != null) {
                    this.owner = owner;
                    break;
                }
                if (lines[i - 1].length() == 15) {
                    BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                    for (Entry<StringWrapper, UUID> entry : map.entrySet()) {
                        String key = entry.getKey().value;
                        if (key.length() > name.length() && key.startsWith(name)) {
                            this.owner = entry.getValue();
                            break loop;
                        }
                    }
                }
                this.owner = UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                break;
            }
            if (this.hasOwner()) {
                this.create();
            }
            return this.owner;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Remove the south road section of a plot<br>
     * - Used when a plot is merged<br>
     */
    public void removeRoadSouth() {
        if (this.area.TYPE != 0 && this.area.TERRAIN > 1) {
            if (this.area.TERRAIN == 3) {
                return;
            }
            Plot other = this.getRelative(Direction.SOUTH);
            Location bot = other.getBottomAbs();
            Location top = this.getTopAbs();
            Location pos1 = new Location(this.getWorldName(), bot.getX(), 0, top.getZ());
            Location pos2 = new Location(this.getWorldName(), top.getX(), MAX_HEIGHT, bot.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            this.getManager().removeRoadSouth(this);
        }
    }

    /**
     * Auto merge a plot in a specific direction.
     *
     * @param dir the direction to merge
     * @param max the max number of merges to do
     * @param uuid the UUID it is allowed to merge with
     * @param removeRoads whether to remove roads
     * @return true if a merge takes place
     */
    public boolean autoMerge(Direction dir, int max, UUID uuid, boolean removeRoads) {
        //Ignore merging if there is no owner for the plot
        if (this.owner == null) {
            return false;
        }
        //Call the merge event
        if (!EventUtil.manager.callMerge(this, dir.getIndex(), max)) {
            return false;
        }
        Set<Plot> connected = this.getConnectedPlots();
        HashSet<PlotId> merged =
            connected.stream().map(Plot::getId).collect(Collectors.toCollection(HashSet::new));
        ArrayDeque<Plot> frontier = new ArrayDeque<>(connected);
        Plot current;
        boolean toReturn = false;
        HashSet<Plot> visited = new HashSet<>();
        while ((current = frontier.poll()) != null && max >= 0) {
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            Set<Plot> plots;
            if ((dir == Direction.ALL || dir == Direction.NORTH) && !getMerged(Direction.NORTH)) {
                Plot other = current.getRelative(Direction.NORTH);
                if (other != null && other.isOwner(uuid) && (
                    other.getBasePlot(false).equals(current.getBasePlot(false))
                        || (plots = other.getConnectedPlots()).size() <= max && frontier
                        .addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.getManager().finishPlotMerge(ids);
                    }
                }
            }
            if (max >= 0 && (dir == Direction.ALL || dir == Direction.EAST) && !current
                .getMerged(Direction.EAST)) {
                Plot other = current.getRelative(Direction.EAST);
                if (other != null && other.isOwner(uuid) && (
                    other.getBasePlot(false).equals(current.getBasePlot(false))
                        || (plots = other.getConnectedPlots()).size() <= max && frontier
                        .addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.getManager().finishPlotMerge(ids);
                    }
                }
            }
            if (max >= 0 && (dir == Direction.ALL || dir == Direction.SOUTH) && !getMerged(
                Direction.SOUTH)) {
                Plot other = current.getRelative(Direction.SOUTH);
                if (other != null && other.isOwner(uuid) && (
                    other.getBasePlot(false).equals(current.getBasePlot(false))
                        || (plots = other.getConnectedPlots()).size() <= max && frontier
                        .addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.getManager().finishPlotMerge(ids);
                    }
                }
            }
            if (max >= 0 && (dir == Direction.ALL || dir == Direction.WEST) && !getMerged(
                Direction.WEST)) {
                Plot other = current.getRelative(Direction.WEST);
                if (other != null && other.isOwner(uuid) && (
                    other.getBasePlot(false).equals(current.getBasePlot(false))
                        || (plots = other.getConnectedPlots()).size() <= max && frontier
                        .addAll(plots) && (max -= plots.size()) != -1)) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;

                    if (removeRoads) {
                        ArrayList<PlotId> ids = new ArrayList<>();
                        ids.add(current.getId());
                        ids.add(other.getId());
                        this.getManager().finishPlotMerge(ids);
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Merge the plot settings<br>
     * - Used when a plot is merged<br>
     *
     * @param plot
     */
    public void mergeData(Plot plot) {
        HashMap<Flag<?>, Object> flags1 = this.getFlags();
        HashMap<Flag<?>, Object> flags2 = plot.getFlags();
        if ((!flags1.isEmpty() || !flags2.isEmpty()) && !flags1.equals(flags2)) {
            boolean greater = flags1.size() > flags2.size();
            if (greater) {
                flags1.putAll(flags2);
            } else {
                flags2.putAll(flags1);
            }
            HashMap<Flag<?>, Object> net = (greater ? flags1 : flags2);
            this.setFlags(net);
            plot.setFlags(net);
        }
        if (!this.getAlias().isEmpty()) {
            plot.setAlias(this.getAlias());
        } else if (!plot.getAlias().isEmpty()) {
            this.setAlias(plot.getAlias());
        }
        for (UUID uuid : this.getTrusted()) {
            plot.addTrusted(uuid);
        }
        for (UUID uuid : plot.getTrusted()) {
            this.addTrusted(uuid);
        }
        for (UUID uuid : this.getMembers()) {
            plot.addMember(uuid);
        }
        for (UUID uuid : plot.getMembers()) {
            this.addMember(uuid);
        }

        for (UUID uuid : this.getDenied()) {
            plot.addDenied(uuid);
        }
        for (UUID uuid : plot.getDenied()) {
            this.addDenied(uuid);
        }
    }

    /**
     * Remove the SE road (only effects terrain)
     */
    public void removeRoadSouthEast() {
        if (this.area.TYPE != 0 && this.area.TERRAIN > 1) {
            if (this.area.TERRAIN == 3) {
                return;
            }
            Plot other = this.getRelative(1, 1);
            Location pos1 = this.getTopAbs().add(1, 0, 1);
            Location pos2 = other.getBottomAbs().subtract(1, 0, 1);
            pos1.setY(0);
            pos2.setY(MAX_HEIGHT);
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            this.area.getPlotManager().removeRoadSouthEast(this);
        }
    }

    /**
     * Gets the plot in a relative location<br>
     * Note: May be null if the partial plot area does not include the relative location
     *
     * @param x
     * @param y
     * @return Plot
     */
    public Plot getRelative(int x, int y) {
        return this.area.getPlotAbs(this.id.getRelative(x, y));
    }

    public Plot getRelative(PlotArea area, int x, int y) {
        return area.getPlotAbs(this.id.getRelative(x, y));
    }

    /**
     * Gets the plot in a relative direction<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * Note: May be null if the partial plot area does not include the relative location
     *
     * @param direction
     * @return
     */
    @Deprecated public Plot getRelative(int direction) {
        return this.area.getPlotAbs(this.id.getRelative(direction));
    }

    /**
     * Gets the plot in a relative direction
     * Note: May be null if the partial plot area does not include the relative location
     *
     * @param direction
     * @return the plot relative to this one
     */
    public Plot getRelative(Direction direction) {
        return this.area.getPlotAbs(this.id.getRelative(direction));
    }

    /**
     * Gets a set of plots connected (and including) this plot<br>
     * - This result is cached globally
     *
     * @return a Set of Plots connected to this Plot
     */
    public Set<Plot> getConnectedPlots() {
        if (this.settings == null) {
            return Collections.singleton(this);
        }
        if (!this.isMerged()) {
            return Collections.singleton(this);
        }
        if (connected_cache != null && connected_cache.contains(this)) {
            return connected_cache;
        }
        regions_cache = null;

        HashSet<Plot> tmpSet = new HashSet<>();
        tmpSet.add(this);
        Plot tmp;
        HashSet<Object> queuecache = new HashSet<>();
        ArrayDeque<Plot> frontier = new ArrayDeque<>();
        if (this.getMerged(Direction.NORTH)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.NORTH));
            if (!tmp.getMerged(Direction.SOUTH)) {
                // invalid merge
                PlotSquared.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(this.owner)) {
                    tmp.getSettings().setMerged(Direction.SOUTH, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.NORTH, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (this.getMerged(Direction.EAST)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.EAST));
            if (!tmp.getMerged(Direction.WEST)) {
                // invalid merge
                PlotSquared.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(this.owner)) {
                    tmp.getSettings().setMerged(Direction.WEST, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.EAST, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (this.getMerged(Direction.SOUTH)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.SOUTH));
            if (!tmp.getMerged(Direction.NORTH)) {
                // invalid merge
                PlotSquared.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(this.owner)) {
                    tmp.getSettings().setMerged(Direction.NORTH, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.SOUTH, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (this.getMerged(Direction.WEST)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.WEST));
            if (!tmp.getMerged(Direction.EAST)) {
                // invalid merge
                PlotSquared.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(this.owner)) {
                    tmp.getSettings().setMerged(Direction.EAST, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.WEST, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        Plot current;
        while ((current = frontier.poll()) != null) {
            if (current.owner == null || current.settings == null) {
                // Invalid plot
                // merged onto unclaimed plot
                PlotSquared
                    .debug("Ignoring invalid merged plot: " + current + " | " + current.owner);
                continue;
            }
            tmpSet.add(current);
            queuecache.remove(current);
            if (current.getMerged(Direction.NORTH)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.NORTH));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (current.getMerged(Direction.EAST)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.EAST));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (current.getMerged(Direction.SOUTH)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.SOUTH));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (current.getMerged(Direction.WEST)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.WEST));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
        }
        connected_cache = tmpSet;
        return tmpSet;
    }

    /**
     * This will combine each plot into effective rectangular regions<br>
     * - This result is cached globally<br>
     * - Useful for handling non rectangular shapes
     *
     * @return
     */
    @NotNull public Set<CuboidRegion> getRegions() {
        if (regions_cache != null && connected_cache != null && connected_cache.contains(this)) {
            return regions_cache;
        }
        if (!this.isMerged()) {
            Location pos1 = this.getBottomAbs();
            Location pos2 = this.getTopAbs();
            connected_cache = Sets.newHashSet(this);
            CuboidRegion rg = new CuboidRegion(pos1.getBlockVector3(), pos2.getBlockVector3());
            regions_cache = Collections.singleton(rg);
            return regions_cache;
        }
        Set<Plot> plots = this.getConnectedPlots();
        Set<CuboidRegion> regions = regions_cache = new HashSet<>();
        Set<PlotId> visited = new HashSet<>();
        for (Plot current : plots) {
            if (visited.contains(current.getId())) {
                continue;
            }
            boolean merge = true;
            PlotId bot = new PlotId(current.getId().x, current.getId().y);
            PlotId top = new PlotId(current.getId().x, current.getId().y);
            while (merge) {
                merge = false;
                ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x, bot.y - 1),
                    new PlotId(top.x, bot.y - 1));
                boolean tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(Direction.SOUTH) || visited
                        .contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.y--;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(top.x + 1, bot.y),
                    new PlotId(top.x + 1, top.y));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(Direction.WEST) || visited
                        .contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.x++;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x, top.y + 1),
                    new PlotId(top.x, top.y + 1));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(Direction.NORTH) || visited
                        .contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.y++;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x - 1, bot.y),
                    new PlotId(bot.x - 1, top.y));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(Direction.EAST) || visited
                        .contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.x--;
                }
            }
            Location gtopabs = this.area.getPlotAbs(top).getTopAbs();
            Location gbotabs = this.area.getPlotAbs(bot).getBottomAbs();
            visited.addAll(MainUtil.getPlotSelectionIds(bot, top));
            for (int x = bot.x; x <= top.x; x++) {
                Plot plot = this.area.getPlotAbs(new PlotId(x, top.y));
                if (plot.getMerged(Direction.SOUTH)) {
                    // south wedge
                    Location toploc = plot.getExtendedTopAbs();
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    BlockVector3 pos1 = BlockVector3.at(botabs.getX(), 0, topabs.getZ() + 1);
                    BlockVector3 pos2 = BlockVector3.at(topabs.getX(), Plot.MAX_HEIGHT - 1, toploc.getZ());
                    regions.add(new CuboidRegion(pos1, pos2));
                    if (plot.getMerged(Direction.SOUTHEAST)) {
                        pos1 = BlockVector3.at(topabs.getX() + 1, 0, topabs.getZ() + 1);
                        pos2 = BlockVector3.at(toploc.getX(), Plot.MAX_HEIGHT - 1, toploc.getZ());
                        regions.add(
                            new CuboidRegion(pos1, pos2));
                        // intersection
                    }
                }
            }

            for (int y = bot.y; y <= top.y; y++) {
                Plot plot = this.area.getPlotAbs(new PlotId(top.x, y));
                if (plot.getMerged(Direction.EAST)) {
                    // east wedge
                    Location toploc = plot.getExtendedTopAbs();
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    BlockVector3 pos1 = BlockVector3.at(topabs.getX() + 1, 0, botabs.getZ());
                    BlockVector3 pos2 = BlockVector3.at(toploc.getX(), Plot.MAX_HEIGHT - 1, topabs.getZ());
                    regions.add(new CuboidRegion(pos1, pos2));
                    if (plot.getMerged(Direction.SOUTHEAST)) {
                        pos1 = BlockVector3.at(topabs.getX() + 1, 0, topabs.getZ() + 1);
                        pos2 = BlockVector3.at(toploc.getX(), Plot.MAX_HEIGHT - 1, toploc.getZ());
                        regions.add(
                            new CuboidRegion(pos1, pos2));
                        // intersection
                    }
                }
            }
            BlockVector3 pos1 = BlockVector3.at(gbotabs.getX(), 0, gbotabs.getZ());
            BlockVector3 pos2 = BlockVector3.at(gtopabs.getX(), Plot.MAX_HEIGHT - 1, gtopabs.getZ());
            regions.add(
                new CuboidRegion(pos1, pos2));
        }
        return regions;
    }

    /**
     * Attempt to find the largest rectangular region in a plot (as plots can form non rectangular shapes)
     *
     * @return
     */
    public CuboidRegion getLargestRegion() {
        Set<CuboidRegion> regions = this.getRegions();
        CuboidRegion max = null;
        double area = Double.NEGATIVE_INFINITY;
        for (CuboidRegion region : regions) {
            double current =
                (region.getMaximumPoint().getX() - (double) region.getMinimumPoint().getX() + 1) * (region.getMaximumPoint().getZ() - (double) region.getMinimumPoint().getZ() + 1);
            if (current > area) {
                max = region;
                area = current;
            }
        }
        return max;
    }

    /**
     * Do the plot entry tasks for each player in the plot<br>
     * - Usually called when the plot state changes (unclaimed/claimed/flag change etc)
     */
    public void reEnter() {
        TaskManager.runTaskLater(() -> {
            for (PlotPlayer pp : Plot.this.getPlayersInPlot()) {
                PlotListener.plotExit(pp, Plot.this);
                PlotListener.plotEntry(pp, Plot.this);
            }
        }, 1);
    }

    /**
     * Gets all the corners of the plot (supports non-rectangular shapes).
     *
     * @return A list of the plot corners
     */
    public List<Location> getAllCorners() {
        Area area = new Area();
        for (CuboidRegion region : this.getRegions()) {
            Rectangle2D rect = new Rectangle2D.Double(region.getMinimumPoint().getX() - 0.6, region.getMinimumPoint().getZ() - 0.6,
                region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1.2, region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1.2);
            Area rectArea = new Area(rect);
            area.add(rectArea);
        }
        List<Location> locs = new ArrayList<>();
        double[] coords = new double[6];
        for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
            int type = pi.currentSegment(coords);
            int x = (int) MathMan.inverseRound(coords[0]);
            int z = (int) MathMan.inverseRound(coords[1]);
            if (type != 4) {
                locs.add(new Location(this.getWorldName(), x, 0, z));
            }
        }
        return locs;
    }

    /**
     * Teleport a player to a plot and send them the teleport message.
     *
     * @param player the player
     * @return if the teleport succeeded
     */
    public boolean teleportPlayer(final PlotPlayer player) {
        Plot plot = this.getBasePlot(false);
        boolean result = EventUtil.manager.callTeleport(player, player.getLocation(), plot);
        if (result) {
            final Location location;
            if (this.area.HOME_ALLOW_NONMEMBER || plot.isAdded(player.getUUID())) {
                location = this.getHome();
            } else {
                location = this.getDefaultHome(false);
            }
            if (Settings.Teleport.DELAY == 0 || Permissions
                .hasPermission(player, "plots.teleport.delay.bypass")) {
                MainUtil.sendMessage(player, Captions.TELEPORTED_TO_PLOT);
                player.teleport(location);
                return true;
            }
            MainUtil
                .sendMessage(player, Captions.TELEPORT_IN_SECONDS, Settings.Teleport.DELAY + "");
            final String name = player.getName();
            TaskManager.TELEPORT_QUEUE.add(name);
            TaskManager.runTaskLater(() -> {
                if (!TaskManager.TELEPORT_QUEUE.contains(name)) {
                    MainUtil.sendMessage(player, Captions.TELEPORT_FAILED);
                    return;
                }
                TaskManager.TELEPORT_QUEUE.remove(name);
                if (player.isOnline()) {
                    MainUtil.sendMessage(player, Captions.TELEPORTED_TO_PLOT);
                    player.teleport(location);
                }
            }, Settings.Teleport.DELAY * 20);
            return true;
        }
        return false;
    }

    // PlotCubed start
    public boolean teleportToWarp(String name, PlotPlayer player) {
        PlotWarp found = null;

        found = getWarpByName(name);

        if (found == null) {
            return false;
        }

        player.teleport(found.getLocation(this));

        return true;
    }
    // PlotCubed end

    public boolean isOnline() {
        if (this.owner == null) {
            return false;
        }
        if (!isMerged()) {
            return UUIDHandler.getPlayer(this.owner) != null;
        }
        for (Plot current : getConnectedPlots()) {
            if (current.hasOwner() && UUIDHandler.getPlayer(current.owner) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a component for a plot to the provided blocks<br>
     * - E.g. floor, wall, border etc.<br>
     * - The available components depend on the generator being used<br>
     *
     * @param component
     * @param blocks
     * @return
     */
    public boolean setComponent(String component, Pattern blocks) {
        if (StringMan
            .isEqualToAny(component, getManager().getPlotComponents(this.getId()))) {
            EventUtil.manager.callComponentSet(this, component);
        }
        return this.getManager().setComponent(this.getId(), component, blocks);
    }

    public int getDistanceFromOrigin() {
        Location bot = getManager().getPlotBottomLocAbs(id);
        Location top = getManager().getPlotTopLocAbs(id);
        return Math.max(Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ())),
            Math.max(Math.abs(top.getX()), Math.abs(top.getZ())));
    }

    /**
     * Expands the world border to include this plot if it is beyond the current border.
     */
    public void updateWorldBorder() {
        int border = this.area.getBorder();
        if (border == Integer.MAX_VALUE) {
            return;
        }
        int max = getDistanceFromOrigin();
        if (max > border) {
            this.area.setMeta("worldBorder", max);
        }
    }

    /**
     * Merges two plots. <br>- Assumes plots are directly next to each other <br> - saves to DB
     *
     * @param lesserPlot
     * @param removeRoads
     */
    public void mergePlot(Plot lesserPlot, boolean removeRoads) {
        Plot greaterPlot = this;
        lesserPlot.removeSign();
        if (lesserPlot.getId().x == greaterPlot.getId().x) {
            if (lesserPlot.getId().y > greaterPlot.getId().y) {
                Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.getMerged(Direction.SOUTH)) {
                lesserPlot.clearRatings();
                greaterPlot.clearRatings();
                lesserPlot.setMerged(Direction.SOUTH, true);
                greaterPlot.setMerged(Direction.NORTH, true);
                lesserPlot.mergeData(greaterPlot);
                if (removeRoads) {
                    //lesserPlot.removeSign();
                    lesserPlot.removeRoadSouth();
                    Plot diagonal = greaterPlot.getRelative(Direction.EAST);
                    if (diagonal.getMerged(Direction.NORTHWEST)) {
                        lesserPlot.removeRoadSouthEast();
                    }
                    Plot below = greaterPlot.getRelative(Direction.WEST);
                    if (below.getMerged(Direction.NORTHEAST)) {
                        below.getRelative(Direction.NORTH).removeRoadSouthEast();
                    }
                }
            }
        } else {
            if (lesserPlot.getId().x > greaterPlot.getId().x) {
                Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.getMerged(Direction.EAST)) {
                lesserPlot.clearRatings();
                greaterPlot.clearRatings();
                lesserPlot.setMerged(Direction.EAST, true);
                greaterPlot.setMerged(Direction.WEST, true);
                lesserPlot.mergeData(greaterPlot);
                if (removeRoads) {
                    //lesserPlot.removeSign();
                    Plot diagonal = greaterPlot.getRelative(Direction.SOUTH);
                    if (diagonal.getMerged(Direction.NORTHWEST)) {
                        lesserPlot.removeRoadSouthEast();
                    }
                    lesserPlot.removeRoadEast();
                }
                Plot below = greaterPlot.getRelative(Direction.NORTH);
                if (below.getMerged(Direction.SOUTHWEST)) {
                    below.getRelative(Direction.WEST).removeRoadSouthEast();
                }
            }
        }

    }

    /**
     * Moves a plot physically, as well as the corresponding settings.
     *
     * @param destination Plot moved to
     * @param whenDone    task when done
     * @param allowSwap   whether to swap plots
     * @return success
     */
    public boolean move(final Plot destination, final Runnable whenDone, boolean allowSwap) {
        final PlotId offset = new PlotId(destination.getId().x - this.getId().x,
            destination.getId().y - this.getId().y);
        Location db = destination.getBottomAbs();
        Location ob = this.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (this.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        boolean occupied = false;
        Set<Plot> plots = this.getConnectedPlots();
        for (Plot plot : plots) {
            Plot other = plot.getRelative(destination.getArea(), offset.x, offset.y);
            if (other.hasOwner()) {
                if (!allowSwap) {
                    TaskManager.runTaskLater(whenDone, 1);
                    return false;
                }
                occupied = true;
            } else {
                plot.removeSign();
            }
        }
        // world border
        destination.updateWorldBorder();
        final ArrayDeque<CuboidRegion> regions = new ArrayDeque<>(this.getRegions());
        // move / swap data
        final PlotArea originArea = getArea();
        for (Plot plot : plots) {
            Plot other = plot.getRelative(destination.getArea(), offset.x, offset.y);
            plot.swapData(other, null);
        }
        // copy terrain
        if (occupied) {
            Runnable swap = new Runnable() {
                @Override public void run() {
                    if (regions.isEmpty()) {
                        TaskManager.runTask(whenDone);
                        return;
                    }
                    CuboidRegion region = regions.poll();
                    Location[] corners = MainUtil.getCorners(getWorldName(), region);
                    Location pos1 = corners[0];
                    Location pos2 = corners[1];
                    Location pos3 = pos1.clone().add(offsetX, 0, offsetZ);
                    Location pos4 = pos2.clone().add(offsetX, 0, offsetZ);
                    pos3.setWorld(destination.getWorldName());
                    pos4.setWorld(destination.getWorldName());
                    ChunkManager.manager.swap(pos1, pos2, pos3, pos4, this);
                }
            };
            swap.run();
        } else {
            Runnable move = new Runnable() {
                @Override public void run() {
                    if (regions.isEmpty()) {
                        Plot plot = destination.getRelative(0, 0);
                        for (Plot current : plot.getConnectedPlots()) {
                            getManager().claimPlot(current);
                            Plot originPlot = originArea.getPlotAbs(
                                new PlotId(current.id.x - offset.x, current.id.y - offset.y));
                            originPlot.getManager().unClaimPlot(originPlot, null);
                        }
                        plot.setSign();
                        TaskManager.runTask(whenDone);
                        return;
                    }
                    final Runnable task = this;
                    CuboidRegion region = regions.poll();
                    Location[] corners = MainUtil.getCorners(getWorldName(), region);
                    final Location pos1 = corners[0];
                    final Location pos2 = corners[1];
                    Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                    newPos.setWorld(destination.getWorldName());
                    ChunkManager.manager.copyRegion(pos1, pos2, newPos,
                        () -> ChunkManager.manager.regenerateRegion(pos1, pos2, false, task));
                }
            };
            move.run();
        }
        return true;
    }

    /**
     * Copy a plot to a location, both physically and the settings
     *
     * @param destination
     * @param whenDone
     * @return
     */
    public boolean copy(final Plot destination, final Runnable whenDone) {
        PlotId offset = new PlotId(destination.getId().x - this.getId().x,
            destination.getId().y - this.getId().y);
        Location db = destination.getBottomAbs();
        Location ob = this.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (this.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        Set<Plot> plots = this.getConnectedPlots();
        for (Plot plot : plots) {
            Plot other = plot.getRelative(destination.getArea(), offset.x, offset.y);
            if (other.hasOwner()) {
                TaskManager.runTaskLater(whenDone, 1);
                return false;
            }
        }
        // world border
        destination.updateWorldBorder();
        // copy data
        for (Plot plot : plots) {
            Plot other = plot.getRelative(destination.getArea(), offset.x, offset.y);
            other.create(plot.getOwner(), false);
            if (!plot.getFlags().isEmpty()) {
                other.getSettings().flags = plot.getFlags();
                DBFunc.setFlags(other, plot.getFlags());
            }
            if (plot.isMerged()) {
                other.setMerged(plot.getMerged());
            }
            if (plot.members != null && !plot.members.isEmpty()) {
                other.members = plot.members;
                for (UUID member : plot.members) {
                    DBFunc.setMember(other, member);
                }
            }
            if (plot.trusted != null && !plot.trusted.isEmpty()) {
                other.trusted = plot.trusted;
                for (UUID trusted : plot.trusted) {
                    DBFunc.setTrusted(other, trusted);
                }
            }
            if (plot.denied != null && !plot.denied.isEmpty()) {
                other.denied = plot.denied;
                for (UUID denied : plot.denied) {
                    DBFunc.setDenied(other, denied);
                }
            }
        }
        // copy terrain
        final ArrayDeque<CuboidRegion> regions = new ArrayDeque<>(this.getRegions());
        Runnable run = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    for (Plot current : getConnectedPlots()) {
                        destination.getManager().claimPlot(destination);
                    }
                    destination.setSign();
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion region = regions.poll();
                Location[] corners = MainUtil.getCorners(getWorldName(), region);
                Location pos1 = corners[0];
                Location pos2 = corners[1];
                Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.getWorldName());
                ChunkManager.manager.copyRegion(pos1, pos2, newPos, this);
            }
        };
        run.run();
        return true;
    }

    public boolean hasFlag(Flag<?> flag) {
        return getFlags().containsKey(flag);
    }

    public boolean removeComment(PlotComment comment) {
        return getSettings().removeComment(comment);
    }

    public void removeComments(List<PlotComment> comments) {
        getSettings().removeComments(comments);
    }

    public List<PlotComment> getComments(String inbox) {
        return getSettings().getComments(inbox);
    }

    public void addComment(PlotComment comment) {
        getSettings().addComment(comment);
    }

    public void setComments(List<PlotComment> list) {
        getSettings().setComments(list);
    }

    public boolean getMerged(Direction direction) {
        return getMerged(direction.getIndex());
    }
}
