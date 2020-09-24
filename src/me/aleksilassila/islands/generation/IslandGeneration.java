package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.biomes.Biomes;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class IslandGeneration {

    private Islands islands;
    public Biomes biomes;

    public IslandGeneration(Islands islands) {
        this.islands = islands;
        this.biomes = new Biomes(islands.sourceWorld, islands.plugin);
    }

    class CopyTask extends BukkitRunnable {
        private final int startX;
        private final int startY;
        private final int startZ;
        private final int targetX;
        private final int targetY;
        private final int targetZ;
        private final int islandSize;
        private int index;

        public CopyTask(int startX, int startY, int startZ, int targetX, int targetY, int targetZ, int islandSize) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.islandSize = islandSize;

            Bukkit.getLogger().info("Starting in " + startX + ", " + startY + ", " + startZ + " to " + targetX + ", " + targetY + ", " + targetZ);


            this.index = 0;
        }

        @Override
        public void run() {
            for (int y = startY; y < startY + islandSize; y++) {
                int relativeX = index / islandSize;
                int relativeZ = index - relativeX * islandSize;

                BlockData sourceData = islands.sourceWorld.getBlockAt(startX + relativeX, y, startZ + relativeZ).getBlockData();

                //WIP
    //            if (Math.random() < - ((8 * (y - sourceY)) / (double) islandSize) + 2) {
    //                continue;
    //            }

                if (isBlockInIslandShape(relativeX, y - startY, relativeZ, islandSize)) {
                    islands.world.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ).setBlockData(sourceData);
                } else {
                    islands.world.getBlockAt(targetX + relativeX, targetY + (y - startY), targetZ + relativeZ).setType(Material.AIR);
                }
            }

            if (index >= islandSize * islandSize) {
                this.cancel();
            }

            index++;
        }
    }

    public boolean copyIsland(Biome biome, int islandSize, int targetX, int targetY, int targetZ) {
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

        // Copy blocks
        new CopyTask(startX, startY, startZ, targetX, targetY, targetZ, islandSize).runTaskTimer(islands.plugin, 0, 1);

        Bukkit.getLogger().info("Updating biomes...");
        for (int x = targetX - 10; x < targetX + islandSize + 10; x++) {
            for (int z = targetZ - 10; z < targetZ + islandSize + 10; z++) {
                for (int y = 0; y < islands.world.getMaxHeight(); y++) {
                    islands.world.getBlockAt(x, y, z).setBiome(biome);
                }
            }
        }

        // Update lighting
        islands.world.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

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
