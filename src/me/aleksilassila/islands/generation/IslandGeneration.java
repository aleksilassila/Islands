package me.aleksilassila.islands.generation;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
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

        CuboidRegion sourceRegion = new CuboidRegion(
                new BukkitWorld(islands.sourceWorld),
                BlockVector3.at(
                        sourceLocation.getBlockX(),
                        centerY + islandSize / 2,
                        sourceLocation.getBlockZ()
                ),
                BlockVector3.at(
                        sourceLocation.getBlockX() + islandSize,
                        centerY - islandSize / 2,
                        sourceLocation.getBlockZ() + islandSize
                )
        );

        int startX = sourceLocation.getBlockX();
        int startY = centerY - islandSize / 2;
        int startZ = sourceLocation.getBlockZ();


        Bukkit.getLogger().info("Started copying to (" + targetX + ", " + targetY + ", " + targetZ + ")");
        for (BlockVector3 point : sourceRegion) {
            BlockData sourceData = islands.sourceWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).getBlockData();

            // Relative coordinates: first block (0,0,0), second (1,0,0) etc.
            int relativeX = point.getBlockX() - startX;
            int relativeY = point.getBlockY() - startY;
            int relativeZ = point.getBlockZ() - startZ;

            if (isBlockInShape(relativeX, relativeY, relativeZ, islandSize)){
                islands.world.getBlockAt(targetX + relativeX, targetY + relativeY, targetZ + relativeZ).setBlockData(sourceData);
            } else {
                islands.world.getBlockAt(targetX + relativeX, targetY + relativeY, targetZ + relativeZ).setType(Material.AIR);
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

    public boolean isBlockInShape(int x, int y, int z, int islandSize) {
        if (y > islandSize / 2) {
            if ((Math.pow(x - islandSize / 2, 2) + 0.8 * Math.pow(y - islandSize / 2, 2) + Math.pow(z - islandSize / 2, 2)) <= Math.pow(islandSize / 2, 2)) {
                return true;
            }
        } else {
            if ((Math.pow(x - islandSize / 2, 2) + ((islandSize * 2)/(Math.pow(y, 2) * 0.2 + islandSize / 8.0) + 1) * Math.pow(y - islandSize / 1.7, 2) + Math.pow(z - islandSize / 2, 2)) <= Math.pow(islandSize / 2, 2)) {
                return true;
            }
        }

        return false;
    }
}
