package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.biomes.Biomes;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

import java.util.List;
import java.util.Random;

public class IslandGeneration {

    private Islands islands;
    public Biomes biomes;

    public IslandGeneration(Islands islands) {
        this.islands = islands;
        this.biomes = new Biomes(islands.sourceWorld, islands.plugin);
    }

    private void copyBlocks(int sourceX, int sourceY, int sourceZ, int targetX, int targetY, int targetZ, int relativeX, int relativeZ, int islandSize) {
        for (int y = sourceY; y < sourceY + islandSize; y++) {
            BlockData sourceData = islands.sourceWorld.getBlockAt(sourceX, y, sourceZ).getBlockData();
//WIP
//            if (Math.random() < - ((8 * (y - sourceY)) / (double) islandSize) + 2) {
//                continue;
//            }

            if (isBlockInIslandShape(relativeX, y - sourceY, relativeZ, islandSize)) {
                islands.world.getBlockAt(targetX, targetY + (y - sourceY), targetZ).setBlockData(sourceData);
            } else {
                islands.world.getBlockAt(targetX, targetY + (y - sourceY), targetZ).setType(Material.AIR);
            }
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

        for (int x = 0; x < islandSize; x++) {
            for (int z = 0; z < islandSize; z++) {
                copyBlocks(startX + x, startY, startZ + z, targetX + x, targetY, targetZ + z, x, z, islandSize);
            }
        }

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
