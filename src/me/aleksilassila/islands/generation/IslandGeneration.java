package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.FastNoiseLite;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

// Eh sorry about this one, its a mess
public enum IslandGeneration {
    INSTANCE;

    private final Islands plugin;

    public List<Task> queue = new ArrayList<>();
    private final int buildDelay;
    private int rowsBuiltPerDelay = 1;
    private final int rowsClearedPerDelay;
    public boolean proceduralShapes;

    private final Map<Material, Material> replacementMap = new HashMap<>();

    IslandGeneration() {
        this.plugin = Islands.instance;

        double delay = plugin.getConfig().getDouble("generation.generationDelayInTicks");
        proceduralShapes = plugin.getConfig().getBoolean("useProceduralShapes", false);

        if (delay < 1.0) {
            this.buildDelay = 1;
            this.rowsBuiltPerDelay = (int) Math.round(1 / delay);
        } else {
            this.buildDelay = (int) delay;
        }

        this.rowsClearedPerDelay = rowsBuiltPerDelay * plugin.getConfig().getInt("generation.clearSpeedMultiplier", 1);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("replaceOnGeneration");

        // Initialize block replacement according to config.yml
        if (section != null) {
            for (String material : section.getKeys(false)) {
                Material materialToReplace = Material.getMaterial(material.toUpperCase());
                Material newMaterial = plugin.getConfig().getString("replaceOnGeneration." + material) != null
                        ? Material.getMaterial(section.getString(material).toUpperCase())
                        : null;

                if (materialToReplace != null && newMaterial != null) {
                    replacementMap.put(materialToReplace, newMaterial);
                    plugin.getLogger().info("Replacing " + materialToReplace.name() + " with " + newMaterial.name());
                } else {
                    if (materialToReplace == null) {
                        plugin.getLogger().warning("Material not found: " + material);
                    }

                    if (newMaterial == null) {
                        plugin.getLogger().warning("Material not found: " + plugin.getConfig().getString("replaceOnGeneration." + material));
                    }
                }
            }
        }
    }

    public boolean copyIsland(Player player, IslandsConfig.Entry updatedIsland, boolean shouldClearArea) {
        if (Biomes.INSTANCE.availableLocations.getOrDefault(updatedIsland.biome, new ArrayList<>()).size() == 0)
            throw new IllegalArgumentException();

        if (!canAddQueueItem(player))
            return false;

        Location sourceLocation = Biomes.INSTANCE.availableLocations.get(updatedIsland.biome).
                get(new Random().nextInt(Biomes.INSTANCE.availableLocations.get(updatedIsland.biome).size()));

        // Get island center y. Center block will be in the middle the first block
        // that is not burnable
        int centerY = 100;
        while (true) {
            int centerX = (int) (sourceLocation.getBlockX() + ((double) updatedIsland.size) / 2.0);
            int centerZ = (int) (sourceLocation.getBlockZ() + ((double) updatedIsland.size) / 2.0);

            Material material = Islands.islandsSourceWorld.getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
            if (!material.isAir() && !material.isBurnable())
                break;

            centerY--;
        }

        sourceLocation.setY(centerY);

        CopyTask task = new CopyTask(player, sourceLocation, updatedIsland, true, shouldClearArea, true);

        if (queue.size() == 0) {
            task.runTaskTimer(plugin, 0, buildDelay);
        }

        addToQueue(task);

        return true;
    }

    public boolean clearIsland(Player player, String islandId) {
        if (!canAddQueueItem(player))
            return false;

        ClearTask task = new ClearTask(player, islandId);

        if (queue.size() == 0) {
            task.runTaskTimer(plugin, 0, buildDelay);
        }

        addToQueue(task);
        return true;
    }

    public void addToQueue(Task task) {
        if (queueContainsPlayer(task.getPlayer()) && !task.getPlayer().hasPermission(Permissions.bypass.queueLimit)) {
            removeFromQueue(task.getPlayer());
        }

        if (task.getPlayer().hasPermission(Permissions.bypass.queue)) {
            int index = getBypassIndex(task.getPlayer());
            queue.add(index, task);
            if (queue.size() > 1) {
                Messages.send(task.getPlayer(), "info.QUEUE_STATUS", index);
            }
        } else {
            queue.add(task);
            if (queue.size() > 1) {
                Messages.send(task.getPlayer(), "info.QUEUE_STATUS", queue.size() - 1);
            }
        }
    }

    public boolean canAddQueueItem(Player player) {
        if (queue.size() == 0) return true;
        return !queue.get(0).getPlayer().equals(player) || player.hasPermission(Permissions.bypass.queueLimit);
    }

    public void removeFromQueue(Player player) {
        int index = 0;
        for (Task task : queue) {
            if (index != 0 && task.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                queue.remove(task);

                return;
            }
            index++;
        }
    }

    public int getBypassIndex(Player player) {
        if (queue.size() < 2) return queue.size();
        else {
            for (int i = 1; i < queue.size(); i++) {
                if (!queue.get(i).getPlayer().getUniqueId().equals(player.getUniqueId()))
                    return i;
            }

            return queue.size();
        }
    }

    private boolean queueContainsPlayer(Player player) {
        for (Task item : queue) {
            if (item.getPlayer().getUniqueId().equals(player.getUniqueId())) return true;
        }

        return false;
    }

    public abstract static class Task extends BukkitRunnable {
        public abstract Player getPlayer();
        public abstract String getIslandId();

        @Override
        public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
            Messages.send(getPlayer(), "info.GENERATION_STARTED");

            return super.runTaskTimer(plugin, delay, period);
        }
    }

    class ClearTask extends Task {
        private final Player player;
        private final int xIndex;
        private final int zIndex;
        private final String islandId;

        private int index = 0;

        public ClearTask(Player player, String islandId) {
            this.player = player;
            this.xIndex = Integer.parseInt(islandId.split("x")[0]);
            this.zIndex = Integer.parseInt(islandId.split("x")[1]);
            this.islandId = islandId;
        }


        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public String getIslandId() {
            return islandId;
        }

        @Override
        public void run() {
            for (int count = 0; count < rowsClearedPerDelay; count++) {
                int relativeX = index / IslandsConfig.INSTANCE.islandSpacing;
                int relativeZ = index - relativeX * IslandsConfig.INSTANCE.islandSpacing;

                int realX = xIndex * IslandsConfig.INSTANCE.islandSpacing + relativeX;
                int realZ = zIndex * IslandsConfig.INSTANCE.islandSpacing + relativeZ;

                boolean skipDelay = true;

                for (int y = 256; y >= 0; y--) {
                    Block target = Islands.islandsWorld.getBlockAt(
                            realX,
                            y,
                            realZ
                    );

                    if (!target.isEmpty()) {
                        target.setType(Material.AIR);
                        skipDelay = false;
                    }
                    target.setBiome(Biome.PLAINS);
                }

                if (skipDelay) count--;

                if (index >= IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing) {
                    Messages.send(player, "success.CLEARING_DONE");

                    queue.remove(this);

                    if (queue.size() > 0) {
                        Task nextTask = queue.get(0);
                        nextTask.runTaskTimer(plugin, 0, buildDelay);
                    }

                    this.cancel();
                    break;
                } else if (index == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 4) {
                    Messages.send(player, "info.CLEARING_STATUS", 25);
                } else if (index == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 2) {
                    Messages.send(player, "info.CLEARING_STATUS", 50);
                } else if (index == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 4 * 3) {
                    Messages.send(player, "info.CLEARING_STATUS", 75);
                }

                index++;
            }
        }
    }

    class CopyTask extends Task {
        private final Player player;
        private final IslandsConfig.Entry island;

        CopyLocation l;

        private boolean clear;
        private boolean paste;
        private int index = 0;

        private final boolean useShapes;

        private final int[][] randomPositions;

        public CopyTask(Player player, Location sourceLocation, IslandsConfig.Entry island, boolean paste, boolean clear, boolean useShapes) {
            int[][] corners = IslandsConfig.getIslandCorner(island.xIndex, island.zIndex, island.size);
            this.l = new CopyLocation(corners[0][0],
                    island.y,
                    corners[0][1],
                    sourceLocation.getBlockX() - island.size / 2,
                    sourceLocation.getBlockY() - island.size / 2,
                    sourceLocation.getBlockZ() - island.size / 2);

            this.player = player;
            this.island = island;

            this.clear = clear;
            this.paste = paste;

            this.useShapes = useShapes;

            this.randomPositions = Utils.randomStalactitePositions(island.size, density);
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public String getIslandId() {
            return island.islandId;
        }

        @Override
        public void run() {
            int radius = island.size / 2;
            if (clear) { // Clear the area first if necessary
                for (int count = 0; count < rowsClearedPerDelay; count++) {
                    int x = index % island.size;
                    int z = index / island.size;

                    boolean skipDelay = true;
                    for (int y = 1; y <= island.size; y--) {
                        CopyLocation nl = l.add(x, y, z);

                        if (!nl.getTarget().isAir()) { // If there's block there, clear it
                            nl.setTarget(Material.AIR);
                            skipDelay = false; // Don't skip the delay between iterations, normally true
                        }
                        nl.setTarget(Biome.PLAINS); // Clear biome
                    }

                    if (skipDelay) count--;

                    if (z >= island.size) {
                        Messages.send(player, "success.CLEARING_DONE");

                        index = 0;
                        clear = false;
                        break;
                    } else if (index == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 4) {
                        Messages.send(player, "info.CLEARING_STATUS", 25);
                    } else if (index == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 2) {
                        Messages.send(player, "info.CLEARING_STATUS", 50);
                    } else if (index == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 4 * 3) {
                        Messages.send(player, "info.CLEARING_STATUS", 75);
                    }

                    index++;
                }

                return;
            }

            // Paste the blocks
            if (!paste) return;
            for (int count = 0; count < rowsBuiltPerDelay; count++) {
                int x = index % island.size;
                int z = index / island.size;

                for (int y = (int) Math.round(1 - modifyNoise(0.8, island.size)); y <= island.size; y++) {
                    CopyLocation nl = this.l.add(x, y, z);

                    BlockData data = nl.getSourceData();

                    // Check if block should be replaced according to config.yml
                    if (replacementMap.containsKey(nl.getSource())) {
                        data = replacementMap.get(nl.getSource()).createBlockData();
                    }

                    double yAdd = getShapeNoise(Islands.islandsWorld, x, z, randomPositions, island.size);

                    if ((y > radius && isBlockInIslandShape(x, y, z, island.size)) ||
                            (radius - y < yAdd && y <= radius)) {
                        nl.setTarget(data);
                    } else {
                        nl.setTarget(Material.AIR);
                    }

                    nl.copyBiome();
                }

                // If done
                if (index >= island.size * island.size) {
                    // Update lighting
                    Islands.islandsWorld.getChunkAt(l.getBlockX() + radius, l.getBlockZ() + radius);

                    player.sendMessage(Messages.get("success.GENERATION_DONE"));
                    queue.remove(this);

                    if (queue.size() > 0) {
                        Task nextTask = queue.get(0);
                        nextTask.runTaskTimer(plugin, 0, buildDelay);
                    }

                    this.cancel();
                    return;
                } else if (index == island.size * island.size / 4) {
                    player.sendMessage(Messages.get("info.GENERATION_STATUS", 25));
                } else if (index == island.size * island.size / 2) {
                    player.sendMessage(Messages.get("info.GENERATION_STATUS", 50));
                } else if (index == island.size * island.size / 4 * 3) {
                    player.sendMessage(Messages.get("info.GENERATION_STATUS", 75));
                }

                index++;
            }
        }
    }

    /**
     * Check if the block is inside the egg-shape (not sphere!!) of the island,
     * the blocks should be in range is 0<=x<=islandSize
     *
     * @param x x coordinate relative to the position of the island.
     * @param y y coordinate relative to the position of the island.
     * @param z z coordinate relative to the position of the island.
     * @param islandSize Size of the island (diameter of the sphere)
     * @return true if the block is inside
     */
    public static boolean isBlockInIslandShape(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + (islandSize / Math.pow(y, 2) + 1.3) * Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    /**
     * Check if the block is inside sphere with diameter of islandSize,
     * the blocks should be in range is 0<=x<=islandSize
     *
     * @param x x coordinate relative to the position of the island.
     * @param y y coordinate relative to the position of the island.
     * @param z z coordinate relative to the position of the island.
     * @param islandSize Size of the island (diameter of the sphere)
     * @return true if the block is inside
     */
    public static boolean isBlockInIslandSphere(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    //

    /**
     * Check if the block is inside cylinder with diameter of islandSize,
     * ignoring height (y), the blocks should be in range is 0<=x<=islandSize
     *
     * @param relativeX x coordinate relative to the position of the island.
     * @param relativeZ z coordinate relative to the position of the island.
     * @param islandSize Size of the island (diameter of the cylinder)
     * @return true if the block is inside
     */
    public static boolean isBlockInIslandCylinder(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    class CopyLocation extends Location {
        double tx, ty, tz;
        public CopyLocation(double x, double y, double z, double tx, double ty, double tz) {
            super(Islands.islandsWorld, x, y, z);
            this.tx = tx;
            this.ty = ty;
            this.tz = tz;
        }

        @Override
        public CopyLocation add(double x, double y, double z) {
            return new CopyLocation(getX() + x, getY() + y, getZ() + z, tx + x, ty + y, tz + z);
        }

        public Material getSource() {
            return Islands.islandsSourceWorld.getBlockAt((int) Math.round(tx), (int) Math.round(ty), (int) Math.round(tz)).getType();
        }

        public BlockData getSourceData() {
            return Islands.islandsSourceWorld.getBlockAt((int) Math.round(tx), (int) Math.round(ty), (int) Math.round(tz)).getBlockData();
        }

        public Material getTarget() {
            return Islands.islandsWorld.getBlockAt(this).getType();
        }

        public int getHighestY() {
            int y = 254;
            while (y > 0) {
                Material material = Islands.islandsSourceWorld.getBlockAt(getBlockX(), y, getBlockZ()).getBlockData().getMaterial();
                if (!material.isAir() && !material.isBurnable()) break;
                y--;
            }

            return y;
        }

        public void setTarget(Material material) {
            Islands.islandsWorld.getBlockAt(this).setType(material);
        }

        public void setTarget(BlockData data) {
            Islands.islandsWorld.getBlockAt(this).setBlockData(data);
        }

        public void setTarget(Biome biome) {
            Islands.islandsWorld.getBlockAt(this).setBiome(biome);
        }

        public void copyBiome() {
            Islands.islandsWorld.getBlockAt(this).setBiome(
                    Islands.islandsSourceWorld.getBlockAt((int) Math.round(tx), (int) Math.round(ty), (int) Math.round(tz)).getBiome());
        }
    }

    private static final double curvature = 5;
    private static final int density = 13;

    static FastNoiseLite generalShape;
    static FastNoiseLite stalactite;
    static double getShapeNoise(World world, int x, int z, int[][] positions, int size)  {
        double factor = Math.max(0, 1 - Math.sqrt((Math.pow(x - size / 2d, 2) + Math.pow(z - size / 2d, 2)) / (Math.pow(size, 2) / 4d)));
        if (factor <= 0) return 0;

        if (generalShape == null) { // Randomize the general shape
            generalShape = new FastNoiseLite((int) Math.round(world.getSeed() / (double) Long.MAX_VALUE * Integer.MAX_VALUE)); // *Troll face*
            generalShape.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
            generalShape.SetFrequency(0.09f);
            generalShape.SetFractalOctaves(2);
        }

        if (stalactite == null) { // Randomize stalactite
            stalactite = new FastNoiseLite((int) Math.round(world.getSeed() / (double) Long.MAX_VALUE * Integer.MAX_VALUE)); // *Troll face*
            stalactite.SetNoiseType(FastNoiseLite.NoiseType.ValueCubic);
            stalactite.SetFrequency(0.2f);
            stalactite.SetFractalOctaves(2);
            stalactite.SetFractalGain(0.2f);
        }

        double base = size / 2d * (0.5 * Math.pow(factor, 2.5 / curvature) + 0.5 * Math.pow(factor, curvature / 1.4));
        double details1 = generalShape.GetNoise(x, z) * 4;

        float spacing = size / (float) (density + 1);

        double dist = spacing;
        for (int[] pos : positions) {
            double d = Math.sqrt(Math.pow(x - pos[0], 2) + Math.pow(z - pos[1], 2));
            dist = Math.min(d / spacing, dist);
        }

        double details2 = 8 * (1 / Math.pow(dist + 1, 2)) * (1 + stalactite.GetNoise(x, z));

        return base + Math.pow(factor, 0.2) * details1 + Math.pow(factor, 0.1) * details2;
    }

    static private double modifyNoise(double value, int islandSize) {
        return value * (islandSize / 6d);
    }
}
