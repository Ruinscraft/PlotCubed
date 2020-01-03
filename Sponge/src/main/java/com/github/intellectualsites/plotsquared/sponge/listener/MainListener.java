package com.github.intellectualsites.plotsquared.sponge.listener;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.sponge.SpongeMain;
import com.github.intellectualsites.plotsquared.sponge.object.SpongePlayer;
import com.github.intellectualsites.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.living.Ambient;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.entity.BreedEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.ExplosionEvent.Detonate;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@SuppressWarnings("Guava") public class MainListener {

    /*
     * TODO:
     *  - Anything marked with a TODO below
     *  - BlockPhysicsEvent
     *  - BlockFormEvent
     *  - BlockFadeEvent
     *  - BlockFromToEvent
     *  - BlockDamageEvent
     *  - Structure (tree etc)
     *  - ChunkPreGenerateEvent
     *  - PlayerIgniteBlockEvent
     *  - PlayerBucketEmptyEvent
     *  - PlayerBucketFillEvent
     *  - VehicleCreateEvent
     *  - HangingPlaceEvent
     *  - HangingBreakEvent
     *  - EntityChangeBlockEvent
     *  - PVP
     *  - block dispense
     *  - PVE
     *  - VehicleDestroy
     *  - Projectile
     *  - enderman harvest
     */

    @Listener public void onChat(MessageEvent event) {
        // TODO
        if (event.isMessageCancelled())
            return;

        Player player = SpongeUtil.getCause(event.getCause(), Player.class);
        if (player == null) {
            return;
        }
        String world = player.getWorld().getName();
        if (!PlotSquared.get().hasPlotArea(world)) {
            return;
        }
        PlotArea plotworld = PlotSquared.get().getPlotAreaByString(world);
        PlotPlayer plr = SpongeUtil.getPlayer(player);
        if (!plotworld.PLOT_CHAT && (plr.getMeta("chat") == null || !(Boolean) plr
            .getMeta("chat"))) {
            return;
        }
        Location loc = SpongeUtil.getLocation(player);
        Plot plot = loc.getPlot();
        if (plot == null) {
            return;
        }
        Text message = event.getMessage();

        // TODO use display name rather than username
        //  - Getting displayname currently causes NPE, so wait until sponge fixes that

        String sender = player.getName();
        PlotId id = plot.getId();
        String newMessage = StringMan
            .replaceAll(C.PLOT_CHAT_FORMAT.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender);
        //        String forcedMessage = StringMan.replaceAll(C.PLOT_CHAT_FORCED.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender);
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer user = entry.getValue();
            String toSend;
            if (plot.equals(user.getLocation().getPlot())) {
                toSend = newMessage;
            } else if (Permissions.hasPermission(user, C.PERMISSION_COMMANDS_CHAT)) {
                ((SpongePlayer) user).player.sendMessage(message);
                continue;
            } else {
                continue;
            }
            String[] split = (toSend + " ").split("%msg%");
            List<Text> components = new ArrayList<>();
            Text prefix = null;
            for (String part : split) {
                if (prefix != null) {
                    components.add(prefix);
                } else {
                    prefix = message;
                }
                components.add(SpongeUtil.getText(part));
            }
            ((SpongePlayer) user).player.sendMessage(Text.join(components));
        }
        //event.setMessage(null);
    }

    @Listener public void onBreedEntity(BreedEntityEvent.Breed event) {
        Location loc = SpongeUtil.getLocation(event.getTargetEntity());
        String world = loc.getWorld();
        PlotArea plotworld = PlotSquared.get().getPlotAreaByString(world);
        if (plotworld == null) {
            return;
        }
        Plot plot = loc.getPlot();
        if (plot == null) {
            if (loc.isPlotRoad()) {
                event.setCancelled(true);
            }
            return;
        }
        if (!plotworld.SPAWN_BREEDING) {
            event.setCancelled(true);
        }
    }

    @Listener public void onSpawnEntity(SpawnEntityEvent event) {
        event.filterEntities(entity -> {
            if (entity instanceof Player) {
                return true;
            }
            Location loc = SpongeUtil.getLocation(entity);
            Plot plot = loc.getPlot();
            if (plot == null) {
                return !loc.isPlotRoad();
            }
            //        Player player = this.<Player> getCause(event.getCause());
            // TODO selectively cancel depending on spawn reason
            // - Not sure if possible to get spawn reason (since there are no callbacks)
            //        if (player != null && !plotworld.SPAWN_EGGS) {
            //            return false;
            //            return true;
            //        }

            if (entity.getType() == EntityTypes.ITEM) {
                return plot.getFlag(Flags.ITEM_DROP).or(true);
            }
            int[] mobs = null;
            if (entity instanceof Living) {
                if (!loc.getPlotArea().MOB_SPAWNING) {
                    return false;
                }
                com.google.common.base.Optional<Integer> mobCap = plot.getFlag(Flags.MOB_CAP);
                if (mobCap.isPresent()) {
                    Integer cap = mobCap.get();
                    if (cap == 0) {
                        return false;
                    }
                    mobs = plot.countEntities();
                    if (mobs[3] >= cap) {
                        return false;
                    }
                }
                if (entity instanceof Ambient || entity instanceof Animal) {
                    com.google.common.base.Optional<Integer> animalFlag =
                        plot.getFlag(Flags.ANIMAL_CAP);
                    if (animalFlag.isPresent()) {
                        int cap = animalFlag.get();
                        if (cap == 0) {
                            return false;
                        }
                        if (mobs == null) {
                            mobs = plot.countEntities();
                        }
                        if (mobs[1] >= cap) {
                            return false;
                        }
                    }
                } else if (entity instanceof Monster) {
                    com.google.common.base.Optional<Integer> monsterFlag =
                        plot.getFlag(Flags.HOSTILE_CAP);
                    if (monsterFlag.isPresent()) {
                        int cap = monsterFlag.get();
                        if (cap == 0) {
                            return false;
                        }
                        if (mobs == null) {
                            mobs = plot.countEntities();
                        }
                        if (mobs[2] >= cap) {
                            return false;
                        }
                    }
                }
                return true;
            } else if (entity instanceof Minecart || entity instanceof Boat) {
                com.google.common.base.Optional<Integer> vehicleFlag =
                    plot.getFlag(Flags.VEHICLE_CAP);
                if (vehicleFlag.isPresent()) {
                    int cap = vehicleFlag.get();
                    if (cap == 0) {
                        return false;
                    }
                    mobs = plot.countEntities();
                    if (mobs[4] >= cap) {
                        return false;
                    }
                }
            }
            com.google.common.base.Optional<Integer> entityCap = plot.getFlag(Flags.ENTITY_CAP);
            if (entityCap.isPresent()) {
                Integer cap = entityCap.get();
                if (cap == 0) {
                    return false;
                }
                if (mobs == null) {
                    mobs = plot.countEntities();
                }
                if (mobs[0] >= cap) {
                    return false;
                }
            }
            if (entity instanceof Explosive) {
                entity.setCreator(plot.owner);
            }
            return true;
        });
    }

    public void onNotifyNeighborBlock(NotifyNeighborBlockEvent event) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        //        SpongeUtil.printCause("physics", event.getCause());
        //        PlotArea area = plotloc.getPlotArea();
        //        event.filterDirections(new Predicate<Direction>() {
        //            
        //            @Override
        //            public boolean test(Direction dir) {
        //                if (cancelled.get()) {
        //                    return true;
        //                }
        //                org.spongepowered.api.world.Location<World> loc = relatives.get(dir);
        //                Location plotloc = SpongeUtil.getLocation(loc.getExtent().getPluginName(), loc);
        //                if (area == null) {
        //                    return true;
        //                }
        //                plot = area.get
        //                Plot plot = plotloc.getPlot();
        //                if (plot == null) {
        //                    if (MainUtil.isPlotAreaAbs(plotloc)) {
        //                        cancelled.set(true);
        //                        return false;
        //                    }
        //                    cancelled.set(true);
        //                    return true;
        //                }
        //                org.spongepowered.api.world.Location<World> relative = loc.getRelative(dir);
        //                Location relLoc = SpongeUtil.getLocation(relative.getExtent().getPluginName(), relative);
        //                if (plot.equals(MainUtil.getPlot(relLoc))) {
        //                    return true;
        //                }
        //                return false;
        //            }
        //        });
    }

    @Listener public void onInteract(InteractBlockEvent event) {
        Player player = SpongeUtil.getCause(event.getCause(), Player.class);
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        BlockSnapshot block = event.getTargetBlock();
        if (block == null) {
            return;
        }
        Optional<org.spongepowered.api.world.Location<World>> bloc = block.getLocation();
        if (!bloc.isPresent()) {
            return;
        }
        Location loc = SpongeUtil.getLocation(player.getWorld().getName(), bloc.get());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(loc);
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD, true)) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!plot.hasOwner()) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_UNOWNED);
            event.setCancelled(true);
            return;
        }
        if (plot.isAdded(pp.getUUID()) || Permissions
            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER)) {
            return;
        } else {
            com.google.common.base.Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);
            org.spongepowered.api.world.Location l = SpongeUtil.getLocation(loc);
            if (flag.isPresent() && flag.get().contains(SpongeUtil.getPlotBlock(l.getBlock()))) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_OTHER);
            event.setCancelled(true);
            return;
        }
    }

    @Listener public void onExplosion(ExplosionEvent e) {
        if (e instanceof ExplosionEvent.Detonate) {
            ExplosionEvent.Detonate event = (Detonate) e;
            World world = event.getTargetWorld();
            String worldName = world.getName();
            if (!PlotSquared.get().hasPlotArea(worldName)) {
                return;
            }
            Optional<Explosive> source = event.getExplosion().getSourceExplosive();
            if (!source.isPresent()) {
                event.setCancelled(true);
                return;
            }
            Explosive tnt = source.get();
            UUID creator = tnt.getCreator().orElse(null);
            Location current = SpongeUtil.getLocation(tnt);
            Plot currentPlot = current.getPlot();
            if (currentPlot == null) {
                if (current.isPlotArea()) {
                    event.setCancelled(true);
                }
                return;
            }
            if (creator != null) {
                if (!currentPlot.isAdded(creator)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (!currentPlot.getFlag(Flags.EXPLOSION).or(false)) {
                event.setCancelled(true);
                return;
            }

            event.getAffectedLocations().removeIf(worldLocation -> currentPlot.equals(
                SpongeUtil.getLocation(worldLocation.getExtent().getName(), worldLocation)
                    .getPlot()));
            event.filterEntities(
                entity -> currentPlot.equals(SpongeUtil.getLocation(entity).getPlot()));
        }
    }

    public void onChangeBlock(ChangeBlockEvent event) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Transaction<BlockSnapshot> first = transactions.get(0);
        BlockSnapshot original = first.getOriginal();
        Optional<World> world = SpongeMain.THIS.getServer().getWorld(original.getWorldUniqueId());
        String worldName = world.get().getName();
        Location loc = SpongeUtil.getLocation(worldName, original.getPosition());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(loc);
        if (plot == null) {
            if (!loc.isPlotArea()) {
                return;
            }
            event.setCancelled(true);
            return;
        }
        event.filter(loc1 -> !SpongeUtil.getLocation(worldName, loc1).isPlotRoad());
    }

    @Listener public void onBlockBreak(ChangeBlockEvent.Decay event) {
        onChangeBlock(event);
    }

    @Listener public void onBlockBreak(ChangeBlockEvent.Grow event) {
        onChangeBlock(event);
    }

    @Listener public void onBlockBreak(ChangeBlockEvent.Modify event) {
        onChangeBlock(event);
    }

    @Listener public void onBlockBreak(ChangeBlockEvent.Break event) {
        Player player = SpongeUtil.getCause(event.getCause(), Player.class);
        if (player == null) {
            //SpongeUtil.printCause("break", event.getCause());
            return;
        }
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Transaction<BlockSnapshot> first = transactions.get(0);
        BlockSnapshot original = first.getOriginal();
        Optional<World> world = SpongeMain.THIS.getServer().getWorld(original.getWorldUniqueId());
        String worldName = world.get().getName();
        Location loc = SpongeUtil.getLocation(worldName, original.getPosition());
        Plot plot = loc.getPlot();
        if (plot == null) {
            if (!loc.isPlotArea()) {
                return;
            }
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
                MainUtil.sendMessage(pp, C.PERMISSION_ADMIN_DESTROY_ROAD);
                event.setCancelled(true);
                return;
            }
        } else if (transactions.size() == 1) {
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                event.setCancelled(true);
                return;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                return;
            } else {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                com.google.common.base.Optional<HashSet<PlotBlock>> destroy =
                    plot.getFlag(Flags.BREAK);
                BlockState state = original.getState();
                if (!destroy.isPresent() || !destroy.get()
                    .contains(SpongeUtil.getPlotBlock(state))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        event.filter(l -> {
            Location loc1 = SpongeUtil.getLocation(worldName, l);
            PlotArea area = loc1.getPlotArea();
            if (area == null) {
                return true;
            }
            Plot plot1 = area.getPlot(loc1);
            if (plot1 == null) {
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD, true);
            }
            if (!plot1.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED, true)) {
                    return false;
                }
                return true;
            }
            if (!plot1.isAdded(pp.getUUID()) && !Permissions
                .hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER, true)) {
                com.google.common.base.Optional<HashSet<PlotBlock>> destroy =
                    plot1.getFlag(Flags.BREAK);
                BlockState state = l.getBlock();
                if (destroy.isPresent() && destroy.get().contains(SpongeUtil.getPlotBlock(state))) {
                    return true;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                return false;
            }
            return true;
        });
    }

    @Listener public void onBlockPlace(ChangeBlockEvent.Pre event) {
        Player player = SpongeUtil.getCause(event.getCause(), Player.class);
        if (player == null) {
            return;
        }
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        List<org.spongepowered.api.world.Location<World>> locs = event.getLocations();
        org.spongepowered.api.world.Location<World> first = locs.get(0);
        String worldName = first.getExtent().getName();
        Location loc = SpongeUtil.getLocation(worldName, first.getPosition());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(loc);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (plot.hasOwner()) {
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            } else {
                com.google.common.base.Optional<HashSet<PlotBlock>> place =
                    plot.getFlag(Flags.PLACE);
                BlockState state = first.getBlock();
                if (!place.isPresent() || !place.get().contains(SpongeUtil.getPlotBlock(state))) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
        } else {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
            event.setCancelled(true);
            return;
        }
    }

    @Listener public void onBlockPlace(ChangeBlockEvent.Place event) {
        Player player = SpongeUtil.getCause(event.getCause(), Player.class);
        if (player == null) {
            return;
        }
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Transaction<BlockSnapshot> first = transactions.get(0);
        BlockSnapshot pos = first.getOriginal();
        Optional<World> world = SpongeMain.THIS.getServer().getWorld(pos.getWorldUniqueId());
        String worldName = world.get().getName();
        Location loc = SpongeUtil.getLocation(worldName, pos.getPosition());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(loc);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD, true)) {
                event.setCancelled(true);
                return;
            }
        } else if (transactions.size() == 1) {
            if (plot.hasOwner()) {
                if (plot.isAdded(pp.getUUID()) || Permissions
                    .hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    return;
                } else {
                    com.google.common.base.Optional<HashSet<PlotBlock>> place =
                        plot.getFlag(Flags.PLACE);
                    BlockState state = pos.getState();
                    if (!place.isPresent() || !place.get()
                        .contains(SpongeUtil.getPlotBlock(state))) {
                        MainUtil
                            .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                        event.setCancelled(true);
                        return;
                    }
                }
            } else {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                event.setCancelled(true);
                return;
            }
        } else {
            event.filter(new Predicate<org.spongepowered.api.world.Location<World>>() {
                @Override public boolean test(org.spongepowered.api.world.Location<World> l) {
                    Location loc = SpongeUtil.getLocation(worldName, l);
                    Plot plot = loc.getPlot();
                    if (plot == null) {
                        return loc.getPlotArea() == null || Permissions
                            .hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD, true);
                    }
                    if (!plot.hasOwner()) {
                        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED, true)) {
                            return true;
                        }
                        return false;
                    }
                    if (plot.isAdded(pp.getUUID()) || Permissions
                        .hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER, true)) {
                        return true;
                    } else {
                        com.google.common.base.Optional<HashSet<PlotBlock>> place =
                            plot.getFlag(Flags.PLACE);
                        BlockState state = l.getBlock();
                        if (place.isPresent() && place.get()
                            .contains(SpongeUtil.getPlotBlock(state))) {
                            return true;
                        }
                        MainUtil
                            .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                        return false;
                    }
                }
            });
        }
    }

    @Listener public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        SpongeUtil.getPlayer(player).unregister();
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        // Now
        String name = pp.getName();
        StringWrapper sw = new StringWrapper(name);
        UUID uuid = pp.getUUID();
        UUIDHandler.add(sw, uuid);

        Location loc = pp.getLocation();
        PlotArea area = loc.getPlotArea();
        Plot plot;
        if (area != null) {
            plot = area.getPlot(loc);
            if (plot != null) {
                PlotListener.plotEntry(pp, plot);
            }
        } else {
            plot = null;
        }
        // Delayed

        // Async
        TaskManager.runTaskLaterAsync(() -> EventUtil.manager.doJoinTask(pp), 20);
    }

    @Listener public void onQuit(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        pp.unregister();
    }

    @Listener public void onMove(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) {
            return;
        }
        org.spongepowered.api.world.Location<World> from = event.getFromTransform().getLocation();
        org.spongepowered.api.world.Location<World> to = event.getToTransform().getLocation();
        int x2;
        if (MathMan.roundInt(from.getX()) != (x2 = MathMan.roundInt(to.getX()))) {
            Player player = (Player) event.getTargetEntity();
            PlotPlayer pp = SpongeUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = SpongeUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlotAbs(loc);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !PlotListener.plotExit(pp, lastPlot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(SpongeUtil.getLocation(from).getPlot())) {
                        player.setLocation(from);
                    } else {
                        player.setLocation(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                ForceFieldListener.handleForcefield(player, pp, now);
                return;
            } else if (!PlotListener.plotEntry(pp, now)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                player.setLocation(from);
                event.setCancelled(true);
                return;
            }
            Integer border = area.getBorder();
            if (x2 > border) {
                to.sub(x2 - border + 4, 0, 0);
                player.setLocation(to);
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            } else if (x2 < -border) {
                to.add(border - x2 + 4, 0, 0);
                player.setLocation(to);
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            }
            return;
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            Player player = (Player) event.getTargetEntity();
            PlotPlayer pp = SpongeUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = SpongeUtil.getLocation(to);
            pp.setMeta(PlotPlayer.META_LOCATION, loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlotAbs(loc);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !PlotListener.plotExit(pp, lastPlot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(SpongeUtil.getLocation(from).getPlot())) {
                        player.setLocation(from);
                    } else {
                        player.setLocation(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                ForceFieldListener.handleForcefield(player, pp, now);
                return;
            } else if (!PlotListener.plotEntry(pp, now)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                player.setLocation(from);
                event.setCancelled(true);
                return;
            }
            Integer border = area.getBorder();
            if (z2 > border) {
                to.add(0, 0, z2 - border - 4);
                player.setLocation(to);
                MainUtil.sendMessage(pp, C.BORDER);
            } else if (z2 < -border) {
                to.add(0, 0, border - z2 + 4);
                player.setLocation(to);
                MainUtil.sendMessage(pp, C.BORDER);
            }
        }
    }
}
