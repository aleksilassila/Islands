package me.aleksilassila.islands.generation;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.ChatUtils;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandGeneration {

    private final Islands islands;
    public Biomes biomes;
    public List<CopyTask> queue = new ArrayList<>();
    private int buildDelay;
    private int rowsBuiltPerDelay = 1;

    public IslandGeneration(Islands islands) {
        this.islands = islands;
        this.biomes = new Biomes(islands.sourceWorld, islands.plugin);

        double delay = islands.plugin.getConfig().getDouble("generation.generationDelayInTicks");

        if (delay < 1.0) {
            this.buildDelay = 1;
            this.rowsBuiltPerDelay = (int) Math.round(1 / delay);
        } else {
            this.buildDelay = (int) delay;
        }
    }

    public void addToQueue(CopyTask task) {
        popFromQueue(task.player.getUniqueId().toString());
        queue.add(task);

        task.player.sendMessage(Messages.info.QUEUE_STATUS(queue.size()));
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

        private int index;

        private boolean clearingArea;
        private int clearingIndex;

        private final int xIndex;
        private final int zIndex;


        public CopyTask(Player player, int startX, int startY, int startZ, int targetX, int targetY, int targetZ, int islandSize, boolean shouldClearArea, int xIndex, int zIndex) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.islandSize = islandSize;
            this.player = player;
            this.clearingArea = shouldClearArea;
            this.clearingIndex = 0;
            this.xIndex = xIndex;
            this.zIndex = zIndex;

            this.index = 0;
        }

        @Override
        public void run() {
            if (clearingArea) {
                for (int count = 0; count < rowsBuiltPerDelay; count++) {
                    for (int y = startY; y < startY + islands.grid.islandSpacing; y++) {
                        int relativeX = clearingIndex / islands.grid.islandSpacing;
                        int relativeZ = clearingIndex - relativeX * islands.grid.islandSpacing;

                        Block target = islands.plugin.islandsWorld.getBlockAt(
                                xIndex * islands.grid.islandSpacing + relativeX,
                                targetY + (y - startY),
                                zIndex * islands.grid.islandSpacing + relativeZ
                        );

                        target.setType(Material.AIR);
                        target.setBiome(Biome.PLAINS);
                    }

                    if (clearingIndex >= islands.grid.islandSpacing * islands.grid.islandSpacing) {
                        player.sendMessage(Messages.success.CLEARING_DONE);

                        clearingArea = false;
                        break;
                    } else if (clearingIndex == islands.grid.islandSpacing * islands.grid.islandSpacing / 4) {
                        player.sendMessage(Messages.info.CLEARING_STATUS(25));
                    } else if (clearingIndex == islands.grid.islandSpacing * islands.grid.islandSpacing / 2) {
                        player.sendMessage(Messages.info.CLEARING_STATUS(50));
                    } else if (clearingIndex == islands.grid.islandSpacing * islands.grid.islandSpacing / 4 * 3) {
                        player.sendMessage(Messages.info.CLEARING_STATUS(75));
                    }

                    clearingIndex++;
                }

                return;
            }

            loop:
            for (int count = 0; count < rowsBuiltPerDelay; count++) {
                for (int y = startY; y < startY + islandSize; y++) {
                    int relativeX = index / islandSize;
                    int relativeZ = index - relativeX * islandSize;

                    Block sourceBlock = islands.sourceWorld.getBlockAt(startX + relativeX, y, startZ + relativeZ);

                    //WIP
                    //            if (Math.random() < - ((8 * (y - sourceY)) / (double) islandSize) + 2) {
                    //                continue;
                    //            }

                    Block target = islands.plugin.islandsWorld.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ);
                    if (isBlockInIslandShape(relativeX, y - startY, relativeZ, islandSize)) {
                        target.setBlockData(sourceBlock.getBlockData());
                    } else {
                        target.setType(Material.AIR);
                    }

                    target.setBiome(sourceBlock.getBiome());
                }

                if (index >= islandSize * islandSize) {
                    // Update lighting
                    islands.plugin.islandsWorld.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

                    player.sendMessage(Messages.success.GENERATION_DONE);
                    queue.remove(this);

                    if (queue.size() > 0) {
                        CopyTask nextTask = queue.get(0);
                        nextTask.runTaskTimer(islands.plugin, 0, buildDelay);
                        nextTask.player.sendMessage(Messages.info.GENERATION_STARTED(nextTask.islandSize * nextTask.islandSize / 20.0));
                    }

                    this.cancel();
                    break loop;
                } else if (index == islandSize * islandSize / 4) {
                    player.sendMessage(Messages.info.GENERATION_STATUS(25));
                } else if (index == islandSize * islandSize / 2) {
                    player.sendMessage(Messages.info.GENERATION_STATUS(50));
                } else if (index == islandSize * islandSize / 4 * 3) {
                    player.sendMessage(Messages.info.GENERATION_STATUS(75));
                }

                index++;
            }
        }
    }

    public boolean copyIsland(Player player, Biome biome, int islandSize, int targetX, int targetY, int targetZ, boolean shouldClearArea, int xIndex, int zIndex) throws IllegalArgumentException {
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

            Material material = islands.sourceWorld.getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
            if (material == Material.STONE || material == Material.SANDSTONE || material == Material.WATER) {
                break;
            }

            centerY--;
        }

        int startX = sourceLocation.getBlockX();
        int startY = centerY - islandSize / 2;
        int startZ = sourceLocation.getBlockZ();

        CopyTask task = new CopyTask(player, startX, startY, startZ, targetX, targetY, targetZ, islandSize, shouldClearArea, xIndex, zIndex);

        if (queue.size() == 0) {
            task.runTaskTimer(islands.plugin, 0, buildDelay);
        }

        addToQueue(task);

        return true;
    }

    public boolean isBlockInIslandShape(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + (islandSize / Math.pow(y, 2) + 1.3) * Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    public boolean isBlockInIslandSphere(int x, int y, int z, int islandSize) {
        return (Math.pow(x - islandSize / 2.0, 2) + Math.pow(y - islandSize / 2.0, 2) + Math.pow(z - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }

    public boolean isBlockInIslandCylinder(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }
}
