package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class IslandGeneration {

    private final Islands plugin;
    private final World islandsWorld;

    public Biomes biomes;
    public List<CopyTask> queue = new ArrayList<>();
    private final int buildDelay;
    private int rowsBuiltPerDelay = 1;
    private int rowsClearedPerDelay = 2;

    private final Map<Material, Material> replacementMap;

    public IslandGeneration(Islands plugin) {
        this.plugin = plugin;
        this.islandsWorld = plugin.islandsWorld;
        this.biomes = new Biomes(plugin, plugin.islandsSourceWorld);

        double delay = plugin.getConfig().getDouble("generation.generationDelayInTicks");

        if (delay < 1.0) {
            this.buildDelay = 1;
            this.rowsBuiltPerDelay = (int) Math.round(1 / delay);
        } else {
            this.buildDelay = (int) delay;
        }
        this.rowsClearedPerDelay = rowsBuiltPerDelay * plugin.getConfig().getInt("generation.clearSpeedMultiplier");

        replacementMap = new HashMap<>();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("replaceOnGeneration");

        if (section != null) {
            for (String material : section.getKeys(false)) {
                Material materialToReplace = Material.getMaterial(material.toUpperCase());
                Material newMaterial = plugin.getConfig().getString("replaceOnGeneration." + material) != null
                        ? Material.getMaterial(plugin.getConfig().getString("replaceOnGeneration." + material).toUpperCase())
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

    public boolean copyIsland(Player player, Biome biome, int islandSize, Vector target, boolean shouldClearArea, int xIndex, int zIndex, Shape shape) throws IllegalArgumentException {
        List<Location> locations = biomes.availableLocations.get(biome);

        if (locations == null) {
            throw new IllegalArgumentException();
        }

        if (locations.size() == 0) {
            throw new IllegalArgumentException();
        }

        Location sourceLocation = locations.get(new Random().nextInt(locations.size()));

        int centerY = 100;
        while (true) {
            int centerX = (int) (sourceLocation.getBlockX() + ((double) islandSize) / 2.0);
            int centerZ = (int) (sourceLocation.getBlockZ() + ((double) islandSize) / 2.0);

            Material material = plugin.islandsSourceWorld.getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
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

        CopyTask task = new CopyTask(player, new Vector(startX, startY, startZ), target, islandSize, shouldClearArea, xIndex, zIndex, shape);

        if (queue.size() == 0) {
            task.runTaskTimer(plugin, 0, buildDelay);
        }

        addToQueue(task);

        return true;
    }

    public void addToQueue(CopyTask task) {
        if (queueContainsPlayer(task.player) && !task.player.hasPermission(Permissions.bypass.queueLimit)) {
            removeFromQueue(task.player);
        }


        if (task.player.hasPermission(Permissions.bypass.queue)) {
            int index = getBypassIndex(task.player);
            queue.add(index, task);
            Messages.send(task.player, "info.QUEUE_STATUS", index);
        } else {
            queue.add(task);
            Messages.send(task.player, "info.QUEUE_STATUS", queue.size() - 1);
        }
    }

    public void removeFromQueue(Player player) {
        int index = 0;
        for (CopyTask task : queue) {
            if (index != 0 && task.player.getUniqueId().equals(player.getUniqueId())) {
                queue.remove(task);

                return;
            }
            index++;
        }
    }

    public int getBypassIndex(Player player) {
        for (int index = 0; index < queue.size(); index++) {
            if (!queue.get(index).player.getUniqueId().equals(player.getUniqueId())) {
                return index;
            }
        }

        return Math.max(queue.size() - 1, 0);
    }

    private boolean queueContainsPlayer(Player player) {
        for (CopyTask item : queue) {
            if (item.player.getUniqueId().equals(player.getUniqueId())) return true;
        }

        return false;
    }

    class CopyTask extends BukkitRunnable {
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

        private final Shape shape;

        public CopyTask(Player player, Vector start, Vector target, int islandSize, boolean shouldDoClearing, int xIndex, int zIndex, Shape shape) {
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
            this.xIndex = xIndex;
            this.zIndex = zIndex;

            this.shape = shape;

            this.index = 0;
        }

        @Override
        public void run() {
            if (shouldDoClearing) {
                for (int count = 0; count < rowsClearedPerDelay; count++) {
                    int relativeX = clearingIndex / plugin.layout.islandSpacing;
                    int relativeZ = clearingIndex - relativeX * plugin.layout.islandSpacing;

                    int realX = xIndex * plugin.layout.islandSpacing + relativeX;
                    int realZ = zIndex * plugin.layout.islandSpacing + relativeZ;

                    boolean skipDelay = true;

                    for (int y = startY + plugin.layout.islandSpacing; y >= startY; y--) {
                        Block target = plugin.islandsWorld.getBlockAt(
                                realX,
                                targetY + (y - startY),
                                realZ
                        );

                        if (!target.isEmpty()) {
                            target.setType(Material.AIR);
                            skipDelay = false;
                        }
                        target.setBiome(Biome.PLAINS);
                    }

                    if (skipDelay) count--;

                    if (clearingIndex >= plugin.layout.islandSpacing * plugin.layout.islandSpacing) {
                        Messages.send(player, "success.CLEARING_DONE");

                        shouldDoClearing = false;
                        break;
                    } else if (clearingIndex == plugin.layout.islandSpacing * plugin.layout.islandSpacing / 4) {
                        Messages.send(player, "info.CLEARING_STATUS", 25);
                    } else if (clearingIndex == plugin.layout.islandSpacing * plugin.layout.islandSpacing / 2) {
                        Messages.send(player, "info.CLEARING_STATUS", 50);
                    } else if (clearingIndex == plugin.layout.islandSpacing * plugin.layout.islandSpacing / 4 * 3) {
                        Messages.send(player, "info.CLEARING_STATUS", 75);
                    }

                    clearingIndex++;
                }

                return;
            }

            for (int count = 0; count < rowsBuiltPerDelay; count++) {
                for (int y = startY; y < startY + islandHeight; y++) {
                    int relativeX = index / islandSize;
                    int relativeZ = index - relativeX * islandSize;

                    Block sourceBlock = plugin.islandsSourceWorld.getBlockAt(startX + relativeX, y, startZ + relativeZ);

                    if (replacementMap.containsKey(sourceBlock.getType())) {
                        Material material = replacementMap.get(sourceBlock.getType());
                        sourceBlock.setBlockData(material.createBlockData());
                    }

                    Block target = plugin.islandsWorld.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ);

                    if (shape == null) {
                        if (isBlockInIslandShape(relativeX, y - startY, relativeZ, islandSize)) {
                            target.setBlockData(sourceBlock.getBlockData());
                        } else {
                            target.setType(Material.AIR);
                        }
                    } else {
                        if (y - startY > shape.getHeight() - 1) {
                            if (isBlockInIslandShape(relativeX, y - startY - (islandHeight - islandSize), relativeZ, islandSize)) {
                                target.setBlockData(sourceBlock.getBlockData());
                            } else {
                                target.setType(Material.AIR);
                            }
                        } else {
                            if (!shape.isBlockAir(relativeX, y - startY, relativeZ)) {
                                target.setBlockData(sourceBlock.getBlockData());
                            } else {
                                target.setType(Material.AIR);
                            }
                        }
                    }

                    target.setBiome(sourceBlock.getBiome());
                }

                if (index >= islandSize * islandSize) {
                    // Update lighting
                    plugin.islandsWorld.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

                    player.sendMessage(Messages.get("success.GENERATION_DONE"));
                    queue.remove(this);

                    if (queue.size() > 0) {
                        CopyTask nextTask = queue.get(0);
                        nextTask.runTaskTimer(plugin, 0, buildDelay);
                        nextTask.player.sendMessage(Messages.get("info.GENERATION_STARTED", nextTask.islandSize * nextTask.islandSize / 20.0));
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

    public static boolean isBlockInIslandShape(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + (islandSize / Math.pow(y, 2) + 1.3) * Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    public static boolean isBlockInIslandSphere(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    public static boolean isBlockInIslandCylinder(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }
}
