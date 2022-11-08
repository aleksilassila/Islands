package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Config;
import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import me.aleksilassila.islands.utils.FastNoiseLite;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import me.aleksilassila.islands.world.IslandsWorld;
import me.aleksilassila.islands.world.SourceWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

// Eh sorry about this one, it's a mess
public class IslandGeneration {
    private final Islands islands;
    private final Plugin plugin;
    private final Config config;

    private final SourceWorld sourceWorld;
    private final IslandsWorld islandsWorld;

    public List<Task> queue = new ArrayList<>();
    private final int buildDelay;
    private int rowsBuiltPerDelay = 1;
    private final int rowsClearedPerDelay;
    public boolean proceduralShapes;

    static FastNoiseLite generalShape;
    static FastNoiseLite stalactite;

    public IslandGeneration(Islands islands) {
        this.plugin = islands.plugin;
        this.islands = islands;
        this.config = islands.config;

        this.sourceWorld = islands.sourceWorld;
        this.islandsWorld = islands.islandsWorld;

        if (config.generationDelay < 1.0) {
            this.buildDelay = 1;
            this.rowsBuiltPerDelay = (int) Math.round(1 / config.generationDelay);
        } else {
            this.buildDelay = (int) config.generationDelay;
        }

        this.rowsClearedPerDelay = rowsBuiltPerDelay * plugin.getConfig().getInt("generation.clearSpeedMultiplier", 1);

    }


    public boolean copyIsland(Player player, Entry updatedIsland, boolean shouldClearArea, boolean noShape, int oldSize) {
        HashMap<Biome, List<Location>> availableLocations = sourceWorld.getAvailableLocations();

        if (availableLocations.getOrDefault(updatedIsland.biome, new ArrayList<>()).size() == 0)
            throw new IllegalArgumentException();

        if (!canAddQueueItem(player))
            return false;

        Location sourceLocation = availableLocations.get(updatedIsland.biome).
                get(new Random().nextInt(availableLocations.get(updatedIsland.biome).size()));

        // Get island center y. Center block will be in the middle the first block
        // that is not burnable
        int centerY = 100;
        while (true) {
            int centerX = (int) (sourceLocation.getBlockX() + ((double) updatedIsland.size) / 2.0);
            int centerZ = (int) (sourceLocation.getBlockZ() + ((double) updatedIsland.size) / 2.0);

            Material material = sourceWorld.getWorld().getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
            if (!material.isAir() && !material.isBurnable())
                if (material != Material.MUSHROOM_STEM
                        && material != Material.BROWN_MUSHROOM_BLOCK
                        && material != Material.RED_MUSHROOM_BLOCK) {
                    break;
                }

            centerY--;
        }

        sourceLocation.setY(centerY);

        CopyTask task = new CopyTask(player, sourceLocation, updatedIsland, true, shouldClearArea, !noShape, oldSize);

        if (queue.size() == 0) {
            task.runTaskTimer(plugin, 0, buildDelay);
        }

        addToQueue(task);

        return true;
    }

    public boolean clearIsland(Player player, Entry island) {
        if (!canAddQueueItem(player))
            return false;

        CopyTask task = new CopyTask(player, island);

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
        public synchronized BukkitTask runTaskTimer(org.bukkit.plugin.Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
            Messages.send(getPlayer(), "info.GENERATION_STARTED");

            return super.runTaskTimer(plugin, delay, period);
        }
    }

    class CopyTask extends Task {
        private final Player player;
        private final Entry island;

        CopyLocation copyLocation;
        CopyLocation clearLocation;

        private boolean clear;
        private boolean paste;
        private int index = 0;

        private final int radius;
        private final int clearSize;

        private boolean useShapes = false;

        private int[][] randomPositions = null;

        // Noise offsets
        private int o1;
        private int o2;

        public CopyTask(Player player, Location sourceLocation, Entry island, boolean paste, boolean clear, boolean useShapes, int clearSize) {
            int[][] corners = islands.islandsConfig.getIslandCorner(island.xIndex, island.zIndex, island.size);
            int[][] clearCorners = islands.islandsConfig.getIslandCorner(island.xIndex, island.zIndex, clearSize);

            this.copyLocation = new CopyLocation(corners[0][0],
                    island.y,
                    corners[0][1],
                    sourceLocation.getBlockX() - island.size / 2,
                    sourceLocation.getBlockY() - island.size / 2,
                    sourceLocation.getBlockZ() - island.size / 2);

            this.clearLocation = new CopyLocation(clearCorners[0][0],
                    island.y,
                    clearCorners[0][1],
                    sourceLocation.getBlockX() - clearSize / 2,
                    sourceLocation.getBlockY() - clearSize / 2,
                    sourceLocation.getBlockZ() - clearSize / 2);

            System.out.println(clearCorners[0][0] + ", " + clearCorners[0][1]);

            this.player = player;
            this.island = island;
            this.radius = island.size / 2;
            this.clearSize = clearSize;

            this.clear = clear;
            this.paste = paste;

            this.useShapes = proceduralShapes && useShapes;

            // Stalactite positions + one in the middle
            int[][] items = Utils.randomStalactitePositions(island.size, config.stalactiteSpacing);
            this.randomPositions = new int[items.length + 1][2];
            System.arraycopy(items, 0, randomPositions, 0, items.length);
            randomPositions[randomPositions.length - 1] = new int[]{radius, radius};

            Random r = new Random();
            o1 = r.nextInt(50000);
            o2 = r.nextInt(50000);
        }

        // For clear command only
        public CopyTask(Player player, Entry island) {
            int[][] corners = islands.islandsConfig.getIslandCorner(island.xIndex, island.zIndex, island.size);
            this.clearLocation = new CopyLocation(corners[0][0],
                    island.y,
                    corners[0][1],
                    0, 0, 0);

            this.player = player;
            this.island = island;
            this.radius = island.size / 2;
            this.clearSize = island.size;

            this.clear = true;
            this.paste = false;
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
            int maxYAdd = (int) (island.size / 2d + 4 * 0.7 + config.stalactiteHeight);
            int maxYAddClear = (int) (clearSize / 2d + 4 * 0.7 + config.stalactiteHeight);

            if (clear) { // Clear the area first if necessary
                for (int count = 0; count < rowsClearedPerDelay; count++) {
                    int x = index % clearSize;
                    int z = index / clearSize;

                    boolean skipDelay = true;
                    for (int y = -maxYAddClear; y <= clearSize; y++) {
                        CopyLocation l = clearLocation.add(x, y, z);
                        Block ib = l.getTargetBlock();

                        if (!ib.getType().isAir()) { // If there's block there, clear it
                            ib.setType(Material.AIR);
                            skipDelay = false; // Don't skip the delay between iterations, normally true
                        }
                    }
                    clearBiome(clearLocation.add(x, 0, z).getTargetBlock(), Biome.PLAINS);

                    if (skipDelay) count--;

                    if (z >= clearSize) {
                        Messages.send(player, "success.CLEARING_DONE");

                        index = 0;
                        clear = false;

                        break;
                    } else if (index == config.islandSpacing * config.islandSpacing / 4) {
                        Messages.send(player, "info.CLEARING_STATUS", 25);
                    } else if (index == config.islandSpacing * config.islandSpacing / 2) {
                        Messages.send(player, "info.CLEARING_STATUS", 50);
                    } else if (index == config.islandSpacing * config.islandSpacing / 4 * 3) {
                        Messages.send(player, "info.CLEARING_STATUS", 75);
                    }

                    index++;
                }

                return;
            }

            if (!paste) {
                queue.remove(this);

                if (queue.size() > 0) {
                    Task nextTask = queue.get(0);
                    nextTask.runTaskTimer(plugin, 0, buildDelay);
                }

                this.cancel();
                return;
            }

            // Paste the blocks
            for (int count = 0; count < rowsBuiltPerDelay; count++) {
                int x = index % island.size;
                int z = index / island.size;

                for (int y = island.size; y >= -maxYAdd; y--) {
                    CopyLocation l = copyLocation.add(x, y, z);
                    Block ib = l.getTargetBlock();
                    Block sb = l.getSourceBlock();

                    BlockData data = sb.getBlockData();

                    // Check if block should be replaced according to config.yml
                    if (config.blockReplacements.containsKey(sb.getType())) {
                        data = config.blockReplacements.get(sb.getType()).createBlockData();
                    }

                    double yAdd = getShapeNoise(islandsWorld.getWorld(), x, z, randomPositions, island.size);

                    if (!useShapes || (y > radius)) {
                        if (isBlockInIslandShape(x, y, z, island.size))
                            ib.setBlockData(data);
                        else ib.setType(Material.AIR);
                    } else if (radius - y < yAdd) {
                        ib.setBlockData(data);
                        if (data.getMaterial().isAir()) // Remove floating stalactite here
                            if (y <= 0 && radius - y - yAdd < 8) break;
                    } else ib.setType(Material.AIR);

                    ib.setBiome(sb.getBiome());
                }

                // Extend biomes up and down
                Biome upBiome = copyLocation.add(x, island.size, z).getSourceBlock().getBiome();
                Biome downBiome = copyLocation.add(x, -maxYAdd, z).getSourceBlock().getBiome();
                Block ib = copyLocation.add(x, 0, z).getTargetBlock();
                for (int y = ib.getY() + island.size; y < 256; y++)
                    islandsWorld.getWorld().setBiome(ib.getX(), y, ib.getZ(), upBiome);
                for (int y = ib.getY() - maxYAdd; y > 0; y--)
                    islandsWorld.getWorld().setBiome(ib.getX(), y, ib.getZ(), downBiome);


                // If done
                if (index >= island.size * island.size) {
                    // Update lighting
                    islandsWorld.getWorld().getChunkAt(copyLocation.getBlockX() + radius, copyLocation.getBlockZ() + radius);

                    player.sendMessage(Messages.get("success.GENERATION_DONE"));

                    paste = false;
                    break;
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

        void clearBiome(Block block, Biome biome) {
            for (int y = 0; y < 256; y++) {
                block.getWorld().setBiome(block.getX(), y, block.getZ(), biome);
            }
        }

        private static final double curvature = 5;

        double getShapeNoise(World world, int x, int z, int[][] positions, int size) {
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
            double generalDetails = generalShape.GetNoise(x + o1, z + o2) * 4;

            double dist = config.stalactiteSpacing;
            for (int[] pos : positions) {
                double d = Math.sqrt(Math.pow(x - pos[0], 2) + Math.pow(z - pos[1], 2));
                dist = Math.min(d / config.stalactiteSpacing, dist);
            }

            double fineDetails = config.stalactiteHeight * (1 / Math.pow(dist + 1, 2)) * (1 + stalactite.GetNoise(x + o1, z + o2));

            return base + Math.pow(factor, 0.2) * generalDetails + Math.pow(factor, 0.1) * fineDetails;
        }
    }


    /**
     * Check if the block is inside the egg-shape (not sphere!!) of the island,
     * the blocks should be in range is 0<=x<=islandSize
     *
     * @param x          x coordinate relative to the position of the island.
     * @param y          y coordinate relative to the position of the island.
     * @param z          z coordinate relative to the position of the island.
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
     * @param x          x coordinate relative to the position of the island.
     * @param y          y coordinate relative to the position of the island.
     * @param z          z coordinate relative to the position of the island.
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
     * @param relativeX  x coordinate relative to the position of the island.
     * @param relativeZ  z coordinate relative to the position of the island.
     * @param islandSize Size of the island (diameter of the cylinder)
     * @return true if the block is inside
     */
    public static boolean isBlockInIslandCylinder(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    class CopyLocation extends Location {
        private final World source = sourceWorld.getWorld();
        private final World target = islandsWorld.getWorld();
        double sx, sy, sz;

        /**
         * @param ix islandsWorld x
         * @param sx islandsSourceWorld x
         */
        public CopyLocation(double ix, double iy, double iz, double sx, double sy, double sz) {
            super(islandsWorld.getWorld(), ix, iy, iz);
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }

        @Override
        public CopyLocation add(double x, double y, double z) {
            return new CopyLocation(getX() + x, getY() + y, getZ() + z, sx + x, sy + y, sz + z);
        }

        public Block getTargetBlock() {
            return target.getBlockAt(this);
        }

        public Block getSourceBlock() {
            return source.getBlockAt((int) Math.round(sx), (int) Math.round(sy), (int) Math.round(sz));
        }
    }
}
