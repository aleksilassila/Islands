package me.aleksilassila.islands.generation;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.biomes.Biomes;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class IslandGeneration {

    private final Islands islands;
    public Biomes biomes;
    public List<CopyTask> queue = new ArrayList<>();

    public IslandGeneration(Islands islands) {
        this.islands = islands;
        this.biomes = new Biomes(islands.sourceWorld, islands.plugin);
    }

    public void addToQueue(CopyTask task) {
        popFromQueue(task.UUID);
        queue.add(task);
    }

    @Nullable
    public CopyTask popFromQueue(String UUID) {
        for (CopyTask task : queue) {
            if (task.UUID.equals(UUID)) {
                queue.remove(task);

                return task;
            }
        }

        return null;
    }

    class CopyTask extends BukkitRunnable {
        private final String UUID;
        private final int startX;
        private final int startY;
        private final int startZ;
        private final int targetX;
        private final int targetY;
        private final int targetZ;
        private final int islandSize;
        private int index;

        public CopyTask(String UUID, int startX, int startY, int startZ, int targetX, int targetY, int targetZ, int islandSize) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.islandSize = islandSize;
            this.UUID = UUID;

            this.index = 0;
        }

        @Override
        public void run() {
            for (int y = startY; y < startY + islandSize; y++) {
                int relativeX = index / islandSize;
                int relativeZ = index - relativeX * islandSize;

                Block sourceBlock = islands.sourceWorld.getBlockAt(startX + relativeX, y, startZ + relativeZ);

                //WIP
    //            if (Math.random() < - ((8 * (y - sourceY)) / (double) islandSize) + 2) {
    //                continue;
    //            }

                Block target = islands.world.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ);
                if (isBlockInIslandShape(relativeX, y - startY, relativeZ, islandSize)) {
                    target.setBlockData(sourceBlock.getBlockData());
                } else {
                    target.setType(Material.AIR);
                }

                target.setBiome(sourceBlock.getBiome());
            }

            if (index >= islandSize * islandSize) {
                // Update lighting
                islands.world.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

                popFromQueue(this.UUID);

                if (queue.size() > 0) {
                    queue.get(0).runTaskTimer(islands.plugin, 0,1);
                }

                this.cancel();
            }

            index++;
        }
    }

    public boolean copyIsland(String UUID, Biome biome, int islandSize, int targetX, int targetY, int targetZ) {
        List<Location> locations = biomes.availableLocations.get(biome);

        if (locations == null) {
            return false;
        }

        if (locations.size() == 0) {
            return false;
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

        CopyTask task = new CopyTask(UUID, startX, startY, startZ, targetX, targetY, targetZ, islandSize);

        // Copy blocks
        if (queue.size() == 0) {
            task.runTaskTimer(islands.plugin, 0, 1);
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

    public boolean isBlockInIslandCircle(int relativeX, int relativeZ, int islandSize) {
        return (Math.pow(relativeX - islandSize / 2.0, 2) + Math.pow(relativeZ - islandSize / 2.0, 2))
                <= Math.pow(islandSize / 2.0, 2);
    }
}
