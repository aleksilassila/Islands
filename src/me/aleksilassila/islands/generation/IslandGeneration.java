package me.aleksilassila.islands.generation;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class IslandGeneration {

    private final Islands plugin;
    public Biomes biomes;
    public List<CopyTask> queue = new ArrayList<>();
    private final int buildDelay;
    private int rowsBuiltPerDelay = 1;

    private final Map<Material, Material> replacementMap;

    public IslandGeneration(Islands plugin) {
        this.plugin = plugin;
        this.biomes = new Biomes(plugin, plugin.islandsSourceWorld);

        double delay = plugin.getConfig().getDouble("generation.generationDelayInTicks");

        if (delay < 1.0) {
            this.buildDelay = 1;
            this.rowsBuiltPerDelay = (int) Math.round(1 / delay);
        } else {
            this.buildDelay = (int) delay;
        }

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

    public void addToQueue(CopyTask task) {
        popFromQueue(task.player.getUniqueId().toString());
        queue.add(task);

        task.player.sendMessage(Messages.get("info.QUEUE_STATUS", queue.size() - 1));
    }

    @Nullable
    public CopyTask popFromQueue(String UUID) {
        int index = 0;
        for (CopyTask task : queue) {
            if (index != 0 && task.player.getUniqueId().toString().equals(UUID)) {
                queue.remove(task);

                return task;
            }
            index++;
        }

        return null;
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

        private boolean shouldClearArea;
        private int clearingIndex;

        private final int xIndex;
        private final int zIndex;

        private final Shape shape;

        public CopyTask(Player player, int startX, int startY, int startZ, int targetX, int targetY, int targetZ, int islandSize, boolean shouldClearArea, int xIndex, int zIndex, Shape shape) {
            this.startX = startX;

            this.startY = shape != null ? startY - (shape.getHeight() - islandSize / 2) : startY;

            this.startZ = startZ;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;

            this.islandSize = islandSize;
            this.islandHeight = shape != null ? shape.getHeight() + islandSize / 2 : islandSize;

            this.player = player;

            this.shouldClearArea = shouldClearArea;
            this.clearingIndex = 0;
            this.xIndex = xIndex;
            this.zIndex = zIndex;

            this.shape = shape;

            this.index = 0;
        }

        @Override
        public void run() {
            if (shouldClearArea) {
                for (int count = 0; count < rowsBuiltPerDelay; count++) {
                    for (int y = startY + plugin.layout.islandSpacing; y >= startY; y--) {
                        int relativeX = clearingIndex / plugin.layout.islandSpacing;
                        int relativeZ = clearingIndex - relativeX * plugin.layout.islandSpacing;

                        Block target = plugin.islandsWorld.getBlockAt(
                                xIndex * plugin.layout.islandSpacing + relativeX,
                                targetY + (y - startY),
                                zIndex * plugin.layout.islandSpacing + relativeZ
                        );

                        target.setType(Material.AIR);
                        target.setBiome(Biome.PLAINS);
                    }

                    if (clearingIndex >= plugin.layout.islandSpacing * plugin.layout.islandSpacing) {
                        player.sendMessage(Messages.get("success.CLEARING_DONE"));

                        shouldClearArea = false;
                        break;
                    } else if (clearingIndex == plugin.layout.islandSpacing * plugin.layout.islandSpacing / 4) {
                        player.sendMessage(Messages.get("info.CLEARING_STATUS", 25));
                    } else if (clearingIndex == plugin.layout.islandSpacing * plugin.layout.islandSpacing / 2) {
                        player.sendMessage(Messages.get("info.CLEARING_STATUS", 50));
                    } else if (clearingIndex == plugin.layout.islandSpacing * plugin.layout.islandSpacing / 4 * 3) {
                        player.sendMessage(Messages.get("info.CLEARING_STATUS", 75));
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

    public boolean copyIsland(Player player, Biome biome, int islandSize, int targetX, int targetY, int targetZ, boolean shouldClearArea, int xIndex, int zIndex, Shape shape) throws IllegalArgumentException {
        if (queue.size() > 0 && queue.get(0).player.getUniqueId().toString().equals(player.getUniqueId().toString())) {
            return false;
        }

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

        CopyTask task = new CopyTask(player, startX, startY, startZ, targetX, targetY, targetZ, islandSize, shouldClearArea, xIndex, zIndex, shape);

        if (queue.size() == 0) {
            task.runTaskTimer(plugin, 0, buildDelay);
        }

        addToQueue(task);

        return true;
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
