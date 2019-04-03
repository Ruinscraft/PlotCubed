package com.github.intellectualsites.plotsquared.bukkit;

import com.github.intellectualsites.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.bukkit.listeners.*;
import com.github.intellectualsites.plotsquared.bukkit.titles.DefaultTitle_111;
import com.github.intellectualsites.plotsquared.bukkit.util.*;
import com.github.intellectualsites.plotsquared.bukkit.util.block.BukkitLocalQueue;
import com.github.intellectualsites.plotsquared.bukkit.uuid.*;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.IPlotMain;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.generator.HybridGen;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.object.chat.PlainChatManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.PlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotArea;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SingleWorldGenerator;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.util.block.QueueProvider;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Capability;
import lombok.Getter;
import lombok.NonNull;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import static com.github.intellectualsites.plotsquared.plot.util.ReflectionUtils.getRefClass;

public final class BukkitMain extends JavaPlugin implements Listener, IPlotMain {

    @Getter private static WorldEdit worldEdit;

    static {
        try {
            Settings.load(new File("plugins/PlotSquared/config/settings.yml"));
        } catch (Throwable ignored) {}
    }

    private final LegacyMappings legacyMappings = new BukkitLegacyMappings();
    private final BlockRegistry<Material> blockRegistry =
        new BukkitBlockRegistry(Material.values());
    private int[] version;
    @Getter private String pluginName;
    @Getter private SingleWorldListener singleWorldListener;
    private Method methodUnloadChunk0;
    private boolean methodUnloadSetup = false;
    private boolean metricsStarted;

    @Override public int[] getServerVersion() {
        if (this.version == null) {
            try {
                this.version = new int[3];
                String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
                this.version[0] = Integer.parseInt(split[0]);
                this.version[1] = Integer.parseInt(split[1]);
                if (split.length == 3) {
                    this.version[2] = Integer.parseInt(split[2]);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                PlotSquared.debug(StringMan.getString(Bukkit.getBukkitVersion()));
                PlotSquared.debug(
                    StringMan.getString(Bukkit.getBukkitVersion().split("-")[0].split("\\.")));
                return new int[] {1, 13, 0};
            }
        }
        return this.version;
    }

    @Override public String getServerImplementation() {
        return Bukkit.getVersion();
    }

    private void init() {
        try {
            PluginManager manager = Bukkit.getPluginManager();
            System.out.println("[P2] Force loading WorldEdit");
            Plugin plugin = manager.getPlugin("WorldEdit");
            if (!manager.isPluginEnabled("WorldEdit")) {
                manager.enablePlugin(WorldEditPlugin.getPlugin(WorldEditPlugin.class));
            }
            System.out.println("[P2] Testing platform capabilities");
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS);
        } catch (final Throwable throwable) {
            throw new IllegalStateException(
                    "Failed to force load WorldEdit. Road schematics will fail to generate",
                    throwable);
        }
    }

    @Override public void onEnable() {


        this.pluginName = getDescription().getName();
        getServer().getName();

        PlotPlayer.registerConverter(Player.class, BukkitUtil::getPlayer);

        if (Bukkit.getVersion().contains("git-Spigot")) {
            // Uses System.out.println because the logger isn't initialized yet
            System.out
                .println("[P2] ========================== USE PAPER ==========================");
            System.out.println("[P2] Paper offers a more complete API for us to work with");
            System.out.println("[P2] and we may come to rely on it in the future.");
            System.out.println("[P2] It is also recommended out of a performance standpoint as");
            System.out
                .println("[P2] it contains many improvements missing from Spigot and Bukkit.");
            System.out.println("[P2] DOWNLOAD: https://papermc.io/downloads");
            System.out.println("[P2] GUIDE: https://www.spigotmc.org/threads/21726/");
            System.out.println("[P2] NOTE: This is only a recommendation");
            System.out.println("[P2]       both Spigot and CraftBukkit are still supported.");
            System.out
                .println("[P2] ===============================================================");
        }

        new PlotSquared(this, "Bukkit");

        // Check for updates
        if (PlotSquared.get().getUpdateUtility() != null) {
            final UpdateUtility updateUtility = PlotSquared.get().getUpdateUtility();
            updateUtility.checkForUpdate(this.getPluginVersionString(), ((updateDescription, throwable) -> {
                Bukkit.getScheduler().runTask(BukkitMain.this, () -> {
                    getLogger().info("-------- PlotSquared Update Check --------");
                    if (throwable != null) {
                        getLogger().severe(String.format("Could not check for update. Reason: %s",
                            throwable.getMessage()));
                    } else {
                        if (updateDescription == null) {
                            getLogger().info("You appear to be running the latest version of PlotSquared. Congratulations!");
                        } else {
                            getLogger().info("There appears to be a PlotSquared update available!");
                            getLogger().info(String.format("You are running version %s,"
                                + " the newest available version is %s", getPluginVersionString(), updateDescription.getVersion()));
                            getLogger().info(String.format("Update URL: %s", updateDescription.getUrl()));
                        }
                    }
                    getLogger().info("-------- PlotSquared Update Check --------");
                });
            }));
        } else {
            getLogger().warning("Update checking disabled. Skipping.");
        }

        if (Settings.Enabled_Components.METRICS) {
            this.startMetrics();
        } else {
            PlotSquared.log(Captions.CONSOLE_PLEASE_ENABLE_METRICS.f(getPluginName()));
        }
        if (Settings.Enabled_Components.WORLDS) {
            TaskManager.IMP.taskRepeat(this::unload, 20);
            try {
                singleWorldListener = new SingleWorldListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unload() {
        if (!this.methodUnloadSetup) {
            this.methodUnloadSetup = true;
            try {
                ReflectionUtils.RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
                this.methodUnloadChunk0 = classCraftWorld.getRealClass()
                    .getDeclaredMethod("unloadChunk0", int.class, int.class, boolean.class);
                this.methodUnloadChunk0.setAccessible(true);
            } catch (Throwable event) {
                event.printStackTrace();
            }
        }
        final PlotAreaManager manager = PlotSquared.get().getPlotAreaManager();
        if (manager instanceof SinglePlotAreaManager) {
            long start = System.currentTimeMillis();
            final SinglePlotArea area = ((SinglePlotAreaManager) manager).getArea();

            outer:
            for (final World world : Bukkit.getWorlds()) {
                final String name = world.getName();
                final char char0 = name.charAt(0);
                if (!Character.isDigit(char0) && char0 != '-') {
                    continue;
                }

                if (!world.getPlayers().isEmpty()) {
                    continue;
                }

                PlotId id;
                try {
                    id = PlotId.fromString(name);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                final Plot plot = area.getOwnedPlot(id);
                if (plot != null) {
                    if (!MainUtil.isServerOwned(plot) || PlotPlayer.wrap(plot.getOwner()) == null) {
                        if (world.getKeepSpawnInMemory()) {
                            world.setKeepSpawnInMemory(false);
                            return;
                        }
                        final Chunk[] chunks = world.getLoadedChunks();
                        if (chunks.length == 0) {
                            if (!Bukkit.unloadWorld(world, true)) {
                                PlotSquared.debug("Failed to unload " + world.getName());
                            }
                            return;
                        } else {
                            int index = 0;
                            do {
                                final Chunk chunkI = chunks[index++];
                                boolean result;
                                if (methodUnloadChunk0 != null) {
                                    try {
                                        result = (boolean) methodUnloadChunk0
                                            .invoke(world, chunkI.getX(), chunkI.getZ(), true);
                                    } catch (Throwable e) {
                                        methodUnloadChunk0 = null;
                                        e.printStackTrace();
                                        continue outer;
                                    }
                                } else {
                                    result = world
                                        .unloadChunk(chunkI.getX(), chunkI.getZ(), true, false);
                                }
                                if (!result) {
                                    continue outer;
                                }
                            } while (index < chunks.length
                                && System.currentTimeMillis() - start < 5);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override public void onDisable() {
        PlotSquared.get().disable();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override public void log(@NonNull String message) {
        try {
            message = Captions.color(message);
            if (!Settings.Chat.CONSOLE_COLOR) {
                message = ChatColor.stripColor(message);
            }
            this.getServer().getConsoleSender().sendMessage(message);
        } catch (final Throwable ignored) {
            System.out.println(ConsoleColors.fromString(message));
        }
    }

    @Override public void shutdown() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    @Override public int[] getPluginVersion() {
        String ver = getDescription().getVersion();
        if (ver.contains("-")) {
            ver = ver.split("-")[0];
        }
        String[] split = ver.split("\\.");
        return new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
            Integer.parseInt(split[2])};
    }

    @Override public String getPluginVersionString() {
        return getDescription().getVersion();
    }

    @Override public void registerCommands() {
        final BukkitCommand bukkitCommand = new BukkitCommand();
        final PluginCommand plotCommand = getCommand("plots");
        plotCommand.setExecutor(bukkitCommand);
        plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
        plotCommand.setTabCompleter(bukkitCommand);
    }

    @Override public File getDirectory() {
        return getDataFolder();
    }

    @Override public File getWorldContainer() {
        return Bukkit.getWorldContainer();
    }

    @Override public TaskManager getTaskManager() {
        return new BukkitTaskManager(this);
    }

    @Override @SuppressWarnings("deprecation") public void runEntityTask() {
        PlotSquared.log(Captions.PREFIX + "KillAllEntities started.");
        TaskManager.runTaskRepeat(() -> PlotSquared.get().forEachPlotArea(plotArea -> {
            final World world = Bukkit.getWorld(plotArea.worldname);
            try {
                if (world == null) {
                    return;
                }
                List<Entity> entities = world.getEntities();
                Iterator<Entity> iterator = entities.iterator();
                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    switch (entity.getType()) {
                        case EGG:
                        case COMPLEX_PART:
                        case FISHING_HOOK:
                        case ENDER_SIGNAL:
                        case LINGERING_POTION:
                        case AREA_EFFECT_CLOUD:
                        case EXPERIENCE_ORB:
                        case LEASH_HITCH:
                        case FIREWORK:
                        case WEATHER:
                        case LIGHTNING:
                        case WITHER_SKULL:
                        case UNKNOWN:
                        case PLAYER:
                            // non moving / unmovable
                            continue;
                        case THROWN_EXP_BOTTLE:
                        case SPLASH_POTION:
                        case SNOWBALL:
                        case SHULKER_BULLET:
                        case SPECTRAL_ARROW:
                        case TIPPED_ARROW:
                        case ENDER_PEARL:
                        case ARROW:
                        case LLAMA_SPIT:
                        case TRIDENT:
                            // managed elsewhere | projectile
                            continue;
                        case ITEM_FRAME:
                        case PAINTING:
                            // Not vehicles
                            continue;
                        case ARMOR_STAND:
                            // Temporarily classify as vehicle
                        case MINECART:
                        case MINECART_CHEST:
                        case MINECART_COMMAND:
                        case MINECART_FURNACE:
                        case MINECART_HOPPER:
                        case MINECART_MOB_SPAWNER:
                        case ENDER_CRYSTAL:
                        case MINECART_TNT:
                        case BOAT:
                            if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                                com.github.intellectualsites.plotsquared.plot.object.Location
                                    location = BukkitUtil.getLocation(entity.getLocation());
                                Plot plot = location.getPlot();
                                if (plot == null) {
                                    if (location.isPlotArea()) {
                                        if (entity.hasMetadata("ps-tmp-teleport")) {
                                            continue;
                                        }
                                        iterator.remove();
                                        entity.remove();
                                    }
                                    continue;
                                }
                                List<MetadataValue> meta = entity.getMetadata("plot");
                                if (meta.isEmpty()) {
                                    continue;
                                }
                                Plot origin = (Plot) meta.get(0).value();
                                if (!plot.equals(origin.getBasePlot(false))) {
                                    if (entity.hasMetadata("ps-tmp-teleport")) {
                                        continue;
                                    }
                                    iterator.remove();
                                    entity.remove();
                                }
                            }
                            continue;
                        case SMALL_FIREBALL:
                        case FIREBALL:
                        case DRAGON_FIREBALL:
                        case DROPPED_ITEM:
                            if (Settings.Enabled_Components.KILL_ROAD_ITEMS && plotArea
                                .getOwnedPlotAbs(BukkitUtil.getLocation(entity.getLocation()))
                                == null) {
                                entity.remove();
                            }
                            // dropped item
                            continue;
                        case PRIMED_TNT:
                        case FALLING_BLOCK:
                            // managed elsewhere
                            continue;
                        case SHULKER:
                            if (Settings.Enabled_Components.KILL_ROAD_MOBS) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                List<MetadataValue> meta = entity.getMetadata("plot");
                                if (meta != null && !meta.isEmpty()) {
                                    if (livingEntity.isLeashed()) {
                                        continue;
                                    }
                                    List<MetadataValue> keep = entity.getMetadata("keep");
                                    if (keep != null && !keep.isEmpty()) {
                                        continue;
                                    }

                                    PlotId originalPlotId = (PlotId) meta.get(0).value();
                                    if (originalPlotId != null) {
                                        com.github.intellectualsites.plotsquared.plot.object.Location
                                            pLoc = BukkitUtil.getLocation(entity.getLocation());
                                        PlotArea area = pLoc.getPlotArea();
                                        if (area != null) {
                                            PlotId currentPlotId = PlotId.of(area.getPlotAbs(pLoc));
                                            if (!originalPlotId.equals(currentPlotId) && (
                                                currentPlotId == null || !area
                                                    .getPlot(originalPlotId)
                                                    .equals(area.getPlot(currentPlotId)))) {
                                                if (entity.hasMetadata("ps-tmp-teleport")) {
                                                    continue;
                                                }
                                                iterator.remove();
                                                entity.remove();
                                            }
                                        }
                                    }
                                } else {
                                    //This is to apply the metadata to already spawned shulkers (see EntitySpawnListener.java)
                                    com.github.intellectualsites.plotsquared.plot.object.Location
                                        pLoc = BukkitUtil.getLocation(entity.getLocation());
                                    PlotArea area = pLoc.getPlotArea();
                                    if (area != null) {
                                        PlotId currentPlotId = PlotId.of(area.getPlotAbs(pLoc));
                                        if (currentPlotId != null) {
                                            entity.setMetadata("plot", new FixedMetadataValue(
                                                (Plugin) PlotSquared.get().IMP, currentPlotId));
                                        }
                                    }
                                }
                            }
                            continue;
                        case LLAMA:
                        case DONKEY:
                        case MULE:
                        case ZOMBIE_HORSE:
                        case SKELETON_HORSE:
                        case HUSK:
                        case ELDER_GUARDIAN:
                        case WITHER_SKELETON:
                        case STRAY:
                        case ZOMBIE_VILLAGER:
                        case EVOKER:
                        case EVOKER_FANGS:
                        case VEX:
                        case VINDICATOR:
                        case POLAR_BEAR:
                        case BAT:
                        case BLAZE:
                        case CAVE_SPIDER:
                        case CHICKEN:
                        case COW:
                        case CREEPER:
                        case ENDERMAN:
                        case ENDERMITE:
                        case ENDER_DRAGON:
                        case GHAST:
                        case GIANT:
                        case GUARDIAN:
                        case HORSE:
                        case IRON_GOLEM:
                        case MAGMA_CUBE:
                        case MUSHROOM_COW:
                        case OCELOT:
                        case PIG:
                        case PIG_ZOMBIE:
                        case RABBIT:
                        case SHEEP:
                        case SILVERFISH:
                        case SKELETON:
                        case SLIME:
                        case SNOWMAN:
                        case SPIDER:
                        case SQUID:
                        case VILLAGER:
                        case WITCH:
                        case WITHER:
                        case WOLF:
                        case ZOMBIE:
                        case PARROT:
                        case SALMON:
                        case DOLPHIN:
                        case TROPICAL_FISH:
                        case DROWNED:
                        case COD:
                        case TURTLE:
                        case PUFFERFISH:
                        case PHANTOM:
                        case ILLUSIONER:
                        default: {
                            if (Settings.Enabled_Components.KILL_ROAD_MOBS) {
                                Location location = entity.getLocation();
                                if (BukkitUtil.getLocation(location).isPlotRoad()) {
                                    if (entity instanceof LivingEntity) {
                                        LivingEntity livingEntity = (LivingEntity) entity;
                                        if (!livingEntity.isLeashed() || !entity
                                            .hasMetadata("keep")) {
                                            Entity passenger = entity.getPassenger();
                                            if (!(passenger instanceof Player) && entity
                                                .getMetadata("keep").isEmpty()) {
                                                if (entity.hasMetadata("ps-tmp-teleport")) {
                                                    continue;
                                                }
                                                iterator.remove();
                                                entity.remove();
                                                continue;
                                            }
                                        }
                                    } else {
                                        Entity passenger = entity.getPassenger();
                                        if (!(passenger instanceof Player) && entity
                                            .getMetadata("keep").isEmpty()) {
                                            if (entity.hasMetadata("ps-tmp-teleport")) {
                                                continue;
                                            }
                                            iterator.remove();
                                            entity.remove();
                                            continue;
                                        }
                                    }
                                }
                            }
                            continue;
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }), 20);
    }

    @Override @Nullable
    public final ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        final IndependentPlotGenerator result;
        if (id != null && id.equalsIgnoreCase("single")) {
            result = new SingleWorldGenerator();
        } else {
            result = PlotSquared.get().IMP.getDefaultGenerator();
            if (!PlotSquared.get().setupPlotWorld(worldName, id, result)) {
                return null;
            }
        }
        return (ChunkGenerator) result.specify(worldName);
    }

    @Override public void registerPlayerEvents() {
        final PlayerEvents main = new PlayerEvents();
        getServer().getPluginManager().registerEvents(main, this);
        getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
    }

    @Override public void registerPlotPlusEvents() {
        PlotPlusListener.startRunnable(this);
        getServer().getPluginManager().registerEvents(new PlotPlusListener(), this);
    }

    @Override public void registerForceFieldEvents() {
    }

    @Override public boolean initWorldEdit() {
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = WorldEdit.getInstance();
            return true;
        }
        return false;
    }

    @Override public EconHandler getEconomyHandler() {
        try {
            BukkitEconHandler econ = new BukkitEconHandler();
            if (econ.init()) {
                return econ;
            }
        } catch (Throwable ignored) {
            PlotSquared.debug("No economy detected!");
        }
        return null;
    }

    @Override public QueueProvider initBlockQueue() {
        try {
            new SendChunk();
            MainUtil.canSendChunk = true;
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            PlotSquared.debug(
                SendChunk.class + " does not support " + StringMan.getString(getServerVersion()));
            MainUtil.canSendChunk = false;
        }
        return QueueProvider.of(BukkitLocalQueue.class, BukkitLocalQueue.class);
    }

    @Override public WorldUtil initWorldUtil() {
        return new BukkitUtil();
    }

    @Override @Nullable public GeneratorWrapper<?> getGenerator(@NonNull final String world,
        @Nullable final String name) {
        if (name == null) {
            return null;
        }
        final Plugin genPlugin = Bukkit.getPluginManager().getPlugin(name);
        if (genPlugin != null && genPlugin.isEnabled()) {
            ChunkGenerator gen = genPlugin.getDefaultWorldGenerator(world, "");
            if (gen instanceof GeneratorWrapper<?>) {
                return (GeneratorWrapper<?>) gen;
            }
            return new BukkitPlotGenerator(world, gen);
        } else {
            return new BukkitPlotGenerator(PlotSquared.get().IMP.getDefaultGenerator());
        }
    }

    @Override public HybridUtils initHybridUtils() {
        return new BukkitHybridUtils();
    }

    @Override public SetupUtils initSetupUtils() {
        return new BukkitSetupUtils();
    }

    @Override public void startMetrics() {
        if (this.metricsStarted) {
            return;
        }
        System.setProperty("bstats.relocatecheck",
            "false"); // We do not want to relocate the package...
        Metrics metrics = new Metrics(this);// bstats
        PlotSquared.log(Captions.PREFIX + "&6Metrics enabled.");
        this.metricsStarted = true;
    }

    @Override public ChunkManager initChunkManager() {
        return new BukkitChunkManager();
    }

    @Override public EventUtil initEventUtil() {
        return new BukkitEventUtil();
    }

    @Override public void unregister(@NonNull final PlotPlayer player) {
        BukkitUtil.removePlayer(player.getName());
    }

    @Override public void registerChunkProcessor() {
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
    }

    @Override public void registerWorldEvents() {
        getServer().getPluginManager().registerEvents(new WorldEvents(), this);
    }

    @Override public IndependentPlotGenerator getDefaultGenerator() {
        return new HybridGen();
    }

    @Override public InventoryUtil initInventoryUtil() {
        return new BukkitInventoryUtil();
    }

    @Override public UUIDHandlerImplementation initUUIDHandler() {
        boolean checkVersion = false;
        try {
            OfflinePlayer.class.getDeclaredMethod("getUniqueId");
            checkVersion = true;
        } catch (Throwable ignore) {
        }
        final UUIDWrapper wrapper;
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                wrapper = new LowerOfflineUUIDWrapper();
            } else {
                wrapper = new OfflineUUIDWrapper();
            }
            Settings.UUID.OFFLINE = true;
        } else if (checkVersion) {
            wrapper = new DefaultUUIDWrapper();
            Settings.UUID.OFFLINE = false;
        } else {
            if (Settings.UUID.FORCE_LOWERCASE) {
                wrapper = new LowerOfflineUUIDWrapper();
            } else {
                wrapper = new OfflineUUIDWrapper();
            }
            Settings.UUID.OFFLINE = true;
        }
        if (!checkVersion) {
            PlotSquared.log(Captions.PREFIX
                + " &c[WARN] Titles are disabled - please update your version of Bukkit to support this feature.");
            Settings.TITLES = false;
        } else {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_111();
            if (wrapper instanceof DefaultUUIDWrapper
                || wrapper.getClass() == OfflineUUIDWrapper.class && !Bukkit.getOnlineMode()) {
                Settings.UUID.NATIVE_UUID_PROVIDER = true;
            }
        }
        if (Settings.UUID.OFFLINE) {
            PlotSquared.log(Captions.PREFIX + " &6" + getPluginName()
                + " is using Offline Mode UUIDs either because of user preference, or because you are using an old version of "
                + "Bukkit");
        } else {
            PlotSquared.log(Captions.PREFIX + " &6" + getPluginName() + " is using online UUIDs");
        }
        if (Settings.UUID.USE_SQLUUIDHANDLER) {
            return new SQLUUIDHandler(wrapper);
        } else {
            return new FileUUIDHandler(wrapper);
        }
    }

    @Override public void setGenerator(@NonNull final String worldName) {
        World world = BukkitUtil.getWorld(worldName);
        if (world == null) {
            // create world
            ConfigurationSection worldConfig =
                PlotSquared.get().worlds.getConfigurationSection("worlds." + worldName);
            String manager = worldConfig.getString("generator.plugin", getPluginName());
            SetupObject setup = new SetupObject();
            setup.plotManager = manager;
            setup.setupGenerator = worldConfig.getString("generator.init", manager);
            setup.type = worldConfig.getInt("generator.type");
            setup.terrain = worldConfig.getInt("generator.terrain");
            setup.step = new ConfigurationNode[0];
            setup.world = worldName;
            SetupUtils.manager.setupWorld(setup);
            world = Bukkit.getWorld(worldName);
        } else {
            try {
                if (!PlotSquared.get().hasPlotArea(worldName)) {
                    SetGenCB.setGenerator(BukkitUtil.getWorld(worldName));
                }
            } catch (Exception e) {
                PlotSquared.log("Failed to reload world: " + world + " | " + e.getMessage());
                Bukkit.getServer().unloadWorld(world, false);
                return;
            }
        }
        ChunkGenerator gen = world.getGenerator();
        if (gen instanceof BukkitPlotGenerator) {
            PlotSquared.get().loadWorld(worldName, (BukkitPlotGenerator) gen);
        } else if (gen != null) {
            PlotSquared.get().loadWorld(worldName, new BukkitPlotGenerator(worldName, gen));
        } else if (PlotSquared.get().worlds.contains("worlds." + worldName)) {
            PlotSquared.get().loadWorld(worldName, null);
        }
    }

    @Override public SchematicHandler initSchematicHandler() {
        return new BukkitSchematicHandler();
    }

    @Override public AbstractTitle initTitleManager() {
        // Already initialized in UUID handler
        return AbstractTitle.TITLE_CLASS;
    }

    @Override @Nullable public PlotPlayer wrapPlayer(final Object player) {
        if (player instanceof Player) {
            return BukkitUtil.getPlayer((Player) player);
        }
        if (player instanceof OfflinePlayer) {
            return BukkitUtil.getPlayer((OfflinePlayer) player);
        }
        if (player instanceof String) {
            return UUIDHandler.getPlayer((String) player);
        }
        if (player instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) player);
        }
        return null;
    }

    @Override public String getNMSPackage() {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override public ChatManager<?> initChatManager() {
        if (Settings.Chat.INTERACTIVE) {
            return new BukkitChatManager();
        } else {
            return new PlainChatManager();
        }
    }

    @Override public GeneratorWrapper<?> wrapPlotGenerator(@Nullable final String world,
        @NonNull final IndependentPlotGenerator generator) {
        return new BukkitPlotGenerator(generator);
    }

    @Override public List<String> getPluginIds() {
        final List<String> names = new ArrayList<>();
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            names.add(plugin.getName() + ';' + plugin.getDescription().getVersion() + ':' + plugin
                .isEnabled());
        }
        return names;
    }

    @Override public BlockRegistry<Material> getBlockRegistry() {
        return this.blockRegistry;
    }

    @Override public LegacyMappings getLegacyMappings() {
        return this.legacyMappings;
    }
}
