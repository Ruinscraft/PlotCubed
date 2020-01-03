package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.world.BlockUtil;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Locale;

@SuppressWarnings("WeakerAccess") public abstract class ClassicPlotWorld extends SquarePlotWorld {

    public int ROAD_HEIGHT = 62;
    public int PLOT_HEIGHT = 62;
    public int WALL_HEIGHT = 62;
    public BlockBucket MAIN_BLOCK = new BlockBucket(BlockTypes.STONE);
    // new BlockState[] {BlockUtil.get("stone")};
    public BlockBucket TOP_BLOCK = new BlockBucket(BlockTypes.GRASS_BLOCK);
    //new BlockState[] {BlockUtil.get("grass")};
    public BlockBucket WALL_BLOCK = new BlockBucket(BlockTypes.STONE_SLAB);
    // BlockUtil.get((short) 44, (byte) 0);
    public BlockBucket CLAIMED_WALL_BLOCK = new BlockBucket(BlockTypes.SANDSTONE_SLAB);
    // BlockUtil.get((short) 44, (byte) 1);
    public BlockBucket WALL_FILLING = new BlockBucket(BlockTypes.STONE);
    //BlockUtil.get((short) 1, (byte) 0);
    public BlockBucket ROAD_BLOCK = new BlockBucket(BlockTypes.QUARTZ_BLOCK);
    // BlockUtil.get((short) 155, (byte) 0);
    public boolean PLOT_BEDROCK = true;

    public ClassicPlotWorld(String worldName, String id,
        @NotNull IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }

    /**
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP.
     * <p>
     * <p>Set the last boolean to false if you do not check a specific config node to be set while using the setup
     * command - this may be useful if a config value can be changed at a later date, and has no impact on the actual
     * world generation</p>
     */
    @NotNull @Override public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
            new ConfigurationNode("plot.height", this.PLOT_HEIGHT, "Plot height",
                Configuration.INTEGER),
            new ConfigurationNode("plot.size", this.PLOT_WIDTH, "Plot width",
                Configuration.INTEGER),
            new ConfigurationNode("plot.filling", this.MAIN_BLOCK, "Plot block",
                Configuration.BLOCK_BUCKET),
            new ConfigurationNode("plot.floor", this.TOP_BLOCK, "Plot floor block",
                Configuration.BLOCK_BUCKET),
            new ConfigurationNode("wall.block", this.WALL_BLOCK, "Top wall block",
                Configuration.BLOCK_BUCKET),
            new ConfigurationNode("wall.block_claimed", this.CLAIMED_WALL_BLOCK,
                "Wall block (claimed)", Configuration.BLOCK_BUCKET),
            new ConfigurationNode("road.width", this.ROAD_WIDTH, "Road width",
                Configuration.INTEGER),
            new ConfigurationNode("road.height", this.ROAD_HEIGHT, "Road height",
                Configuration.INTEGER),
            new ConfigurationNode("road.block", this.ROAD_BLOCK, "Road block",
                Configuration.BLOCK_BUCKET),
            new ConfigurationNode("wall.filling", this.WALL_FILLING, "Wall filling block",
                Configuration.BLOCK_BUCKET),
            new ConfigurationNode("wall.height", this.WALL_HEIGHT, "Wall height",
                Configuration.INTEGER),
            new ConfigurationNode("plot.bedrock", this.PLOT_BEDROCK, "Plot bedrock generation",
                Configuration.BOOLEAN)};
    }

    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override public void loadConfiguration(ConfigurationSection config) {
        super.loadConfiguration(config);
        this.PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        this.PLOT_HEIGHT = Math.min(255, config.getInt("plot.height"));
        this.MAIN_BLOCK = new BlockBucket(config.getString("plot.filling"));
        this.TOP_BLOCK = new BlockBucket(config.getString("plot.floor"));
        this.WALL_BLOCK = new BlockBucket(config.getString("wall.block"));
        this.ROAD_HEIGHT = Math.min(255, config.getInt("road.height"));
        this.ROAD_BLOCK = new BlockBucket(config.getString("road.block"));
        this.WALL_FILLING =
            new BlockBucket(config.getString("wall.filling"));
        this.WALL_HEIGHT = Math.min(254, config.getInt("wall.height"));
        this.CLAIMED_WALL_BLOCK =
            new BlockBucket(config.getString("wall.block_claimed"));

        // Dump world settings
        if (Settings.DEBUG) {
            PlotSquared.debug(String
                .format("- Dumping settings for ClassicPlotWorld with name %s", this.worldname));
            final Field[] fields = this.getClass().getFields();
            for (final Field field : fields) {
                final String name = field.getName().toLowerCase(Locale.ENGLISH);
                if (name.equalsIgnoreCase("g_sch")) {
                    continue;
                }
                Object value;
                try {
                    final boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    value = field.get(this);
                    field.setAccessible(accessible);
                } catch (final IllegalAccessException e) {
                    value = String.format("Failed to parse: %s", e.getMessage());
                }
                PlotSquared.debug(String.format("-- %s = %s", name, value));
            }
        }
    }
}
