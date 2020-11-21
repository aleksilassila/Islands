package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public enum IslandGeneration {
    INSTANCE;

    private final Islands plugin;

    public List<Task> queue = new ArrayList<>();
    private final int buildDelay;
    private int rowsBuiltPerDelay = 1;
    private final int rowsClearedPerDelay;

    private final Map<Material, Material> replacementMap = new HashMap<>();

    IslandGeneration() {
        this.plugin = Islands.instance;

        double delay = plugin.getConfig().getDouble("generation.generationDelayInTicks");

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

    public boolean copyIsland(Player player, Biome biome, int islandSize, Vector target, boolean shouldClearArea, String islandId, Shape shape, boolean randomBiome) throws IllegalArgumentException {
        Location sourceLocation;

        // get location
        if (randomBiome) {
            sourceLocation = Biomes.INSTANCE.getRandomLocation(biome, islandSize);
        } else {
            List<Location> locations = Biomes.INSTANCE.availableLocations.get(biome);

            if (locations == null)
                throw new IllegalArgumentException();

            if (locations.size() == 0)
                throw new IllegalArgumentException();

            if (!canAddQueueItem(player))
                return false;

            sourceLocation = locations.get(new Random().nextInt(locations.size()));
        }

        // Get island center y. Center block will be in the middle the first block
        // that is not burnable
        int centerY = 100;
        while (true) {
            int centerX = (int) (sourceLocation.getBlockX() + ((double) islandSize) / 2.0);
            int centerZ = (int) (sourceLocation.getBlockZ() + ((double) islandSize) / 2.0);

            Material material = Islands.islandsSourceWorld.getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
            if (shape != null && !material.isAir() && !material.isBurnable()) {
                break;
            } else if (Arrays.asList(Material.WATER, Material.SANDSTONE, Material.STONE).contains(material)) {
                break;
            }

            centerY--;
        }

        int startX = sourceLocation.getBlockX();
        int startY = centerY - islandSize / 2;
        int startZ = sourceLocation.getBlockZ();

        CopyTask task = new CopyTask(player, new Vector(startX, startY, startZ), target, islandSize, shouldClearArea, islandId, shape);

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
            for (int index = 1; index < queue.size(); index++) {
                if (!queue.get(index).getPlayer().getUniqueId().equals(player.getUniqueId()))
                    return index;
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

        private final int startX;
        private final int startY;
        private final int startZ;
        private final int targetX;
        private final int targetY;
        private final int targetZ;

        private final int islandSize;
        private final int islandHeight;

        private int index;

        private boolean shouldDoClearing;
        private int clearingIndex;

        private final int xIndex;
        private final int zIndex;

        private final String islandId;

        private final Shape shape;

        public CopyTask(Player player, Vector start, Vector target, int islandSize, boolean shouldDoClearing, String islandId, Shape shape) {
            this.startX = start.getBlockX();

            this.startY = shape != null ? start.getBlockY() - (shape.getHeight() - islandSize / 2) : start.getBlockY();

            this.startZ = start.getBlockZ();
            this.targetX = target.getBlockX();
            this.targetY = target.getBlockY();
            this.targetZ = target.getBlockZ();

            this.islandSize = islandSize;
            this.islandHeight = shape != null ? shape.getHeight() + islandSize / 2 : islandSize;

            this.player = player;

            this.shouldDoClearing = shouldDoClearing;
            this.clearingIndex = 0;
            this.xIndex = Integer.parseInt(islandId.split("x")[0]);
            this.zIndex = Integer.parseInt(islandId.split("x")[1]);

            this.islandId = islandId;

            this.shape = shape;

            this.index = 0;
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
            if (shouldDoClearing) { // Clear the area first if necessary
                for (int count = 0; count < rowsClearedPerDelay; count++) {
                    // Relative coordinates: relative to the target location.
                    // They tell where the block is in the target plot, so the coords are in range 0<=x<=islandSpacing
                    int relativeX = clearingIndex / IslandsConfig.INSTANCE.islandSpacing;
                    int relativeZ = clearingIndex - relativeX * IslandsConfig.INSTANCE.islandSpacing;

                    int realX = xIndex * IslandsConfig.INSTANCE.islandSpacing + relativeX;
                    int realZ = zIndex * IslandsConfig.INSTANCE.islandSpacing + relativeZ;

                    boolean skipDelay = true;

                    for (int y = startY + IslandsConfig.INSTANCE.islandSpacing; y >= startY; y--) {
                        Block target = Islands.islandsWorld.getBlockAt(
                                realX,
                                targetY + (y - startY),
                                realZ
                        );

                        if (!target.isEmpty()) { // If there's block there, clear it
                            target.setType(Material.AIR);
                            skipDelay = false; // Don't skip the delay between iterations, normally true
                        }
                        target.setBiome(Biome.PLAINS); // Clear biome
                    }

                    if (skipDelay) count--;

                    if (clearingIndex >= IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing) {
                        Messages.send(player, "success.CLEARING_DONE");

                        shouldDoClearing = false;
                        break;
                    } else if (clearingIndex == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 4) {
                        Messages.send(player, "info.CLEARING_STATUS", 25);
                    } else if (clearingIndex == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 2) {
                        Messages.send(player, "info.CLEARING_STATUS", 50);
                    } else if (clearingIndex == IslandsConfig.INSTANCE.islandSpacing * IslandsConfig.INSTANCE.islandSpacing / 4 * 3) {
                        Messages.send(player, "info.CLEARING_STATUS", 75);
                    }

                    clearingIndex++;
                }

                return;
            }

            // Paste the blocks
            for (int count = 0; count < rowsBuiltPerDelay; count++) {
                for (int y = startY; y < startY + islandHeight; y++) {
                    // Relative coordinates: relative to the target location.
                    // They tell where the block is in the target plot, so the coords are in range 0<=x<=islandSpacing
                    int relativeX = index / islandSize;
                    int relativeZ = index - relativeX * islandSize;

                    Block sourceBlock = Islands.islandsSourceWorld.getBlockAt(startX + relativeX, y, startZ + relativeZ);

                    // Check if block should be replaced according to config.yml
                    if (replacementMap.containsKey(sourceBlock.getType())) {
                        Material material = replacementMap.get(sourceBlock.getType());
                        sourceBlock.setBlockData(material.createBlockData());
                    }

                    Block target = Islands.islandsWorld.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ);

                    if (shape == null) { // If no shape, calculate default shape with isBlockInIslandShape function
                        if (isBlockInIslandShape(relativeX, y - startY, relativeZ, islandSize)) {
                            target.setBlockData(sourceBlock.getBlockData());
                        } else {
                            target.setType(Material.AIR);
                        }
                    } else {
                        if (y - startY > shape.getHeight() - 1) { // If current block is above the schematic shape, calculate default shape with isBlockInIslandShape function
                            if (isBlockInIslandShape(relativeX, y - startY - (islandHeight - islandSize), relativeZ, islandSize)) {
                                target.setBlockData(sourceBlock.getBlockData());
                            } else {
                                target.setType(Material.AIR);
                            }
                        } else { // If block y is below shape height, use the shape as a mask
                            if (!shape.isBlockAir(relativeX, y - startY, relativeZ)) {
                                target.setBlockData(sourceBlock.getBlockData());
                            } else {
                                target.setType(Material.AIR);
                            }
                        }
                    }

                    target.setBiome(sourceBlock.getBiome());
                }

                // If done
                if (index >= islandSize * islandSize) {
                    // Update lighting
                    Islands.islandsWorld.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

                    player.sendMessage(Messages.get("success.GENERATION_DONE"));
                    queue.remove(this);

                    if (queue.size() > 0) {
                        Task nextTask = queue.get(0);
                        nextTask.runTaskTimer(plugin, 0, buildDelay);
                    }

                    this.cancel();
                    break;
                } else if (index == islandSize * islandSize / 4) {
                    player.sendMessage(Messages.get("info.GENERATION_STATUS", 25));
                } else if (index == islandSize * islandSize / 2) {
                    player.sendMessage(Messages.get("info.GENERATION_STATUS", 50));
                } else if (index == islandSize * islandSize / 4 * 3) {
                    player.sendMessage(Messages.get("info.GENERATION_STATUS", 75));
                }

                index++;
            }
        }
    }

    // Check if the block is inside the egg-shape of the island, the blocks should be in range is 0<=x<=islandSize
    public static boolean isBlockInIslandShape(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + (islandSize / Math.pow(y, 2) + 1.3) * Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    // Check if the block is inside sphere with diameter of islandSize, the blocks should be in range is 0<=x<=islandSize
    public static boolean isBlockInIslandSphere(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    // Check if the block is inside cylinder with diameter of islandSize, ignoring height (y), the blocks should be in range is 0<=x<=islandSize
    public static boolean isBlockInIslandCylinder(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }
}
