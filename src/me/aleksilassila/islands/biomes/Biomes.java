package me.aleksilassila.islands.biomes;

import me.aleksilassila.islands.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.*;

public class Biomes {
    private World world;
    public HashMap<Biome, List<Location>> availableLocations;
    private int biggestIslandSize = 80;

    int biomeSearchJumpBlocks;
    //int biomeSearchSize = 5000;
    int biomeSearchSize;

    public Biomes(World world, Main plugin) {
        this.world = world;
        this.biggestIslandSize = plugin.getConfig().getInt("island.BIG");

        this.biomeSearchJumpBlocks = plugin.getConfig().getInt("generation.searchJump");
        this.biomeSearchSize = plugin.getConfig().getInt("generation.searchArea");

        this.availableLocations = getAllAvailableIslandLocations();
    }

    public HashMap<Biome, List<Location>> getAllAvailableIslandLocations() {
        Set<Biome> availableBiomes = getAllBiomes();
        HashMap<Biome, List<Location>> availableLocations = new HashMap<Biome, List<Location>>();

        for (Biome biome : availableBiomes) {
            Bukkit.getLogger().info("Generating island positions for " + biome.name());
            availableLocations.put(biome, getPossibleIslandLocations(biome, biggestIslandSize));
        }

        return availableLocations;
    }

    private Set<Biome> getAllBiomes() {
        Set<Biome> biomes = new HashSet<Biome>();

        for (int x = 0; x < biomeSearchSize; x = x + (biomeSearchJumpBlocks * 4)) {
            for (int z = 0; z < biomeSearchSize; z = z + (biomeSearchJumpBlocks * 4)) {
                Biome currentBiome = getBiome(x, z);

                if (!biomes.contains(currentBiome)) {
                    Bukkit.getLogger().info("Biome available: " + currentBiome.name());
                    biomes.add(currentBiome);
                }
            }
        }

        return biomes;
    }

    public List<Location> getPossibleIslandLocations(Biome biome, int islandSize) {
        List<Location> locations = new ArrayList<Location>();
        List<int[]> jumpInThesePositions = new ArrayList<int[]>();

        loop:
        for (int x = 0; x < biomeSearchSize - islandSize; x += biomeSearchJumpBlocks) {
            for (int z = 0; z < biomeSearchSize - islandSize; z += biomeSearchJumpBlocks) {
                boolean jump = false;

                for (int[] pos : jumpInThesePositions) {
                    if (pos[0] <= x && x <= pos[0] + islandSize && pos[1] <= z && z <= pos[1] + islandSize) {
                        z += islandSize;
                        jump = true;
                        break;
                    }
                }

                if (jump) { continue; }

                if (isRectInsideBiome(x, z, islandSize, biome)) {
                    locations.add(new Location(world, x, 180, z));
                    jumpInThesePositions.add(new int[]{x, z});
                    z += islandSize;

                    if (locations.size() >= 10) {
                        break loop;
                    }
                }
            }
        }

        return locations;
    }

    Biome getBiome(int x, int z) {
        return world.getBiome(x, 180, z);
    }

    boolean isRectInsideBiome(int xCorner, int zCorner, int rectSize, Biome biome) {
        for (int x = 0; x < rectSize; x++) {
            for (int z = 0; z < rectSize; z++) {
                if (getBiome(xCorner + x, zCorner + z) != biome) {
                    return false;
                }
            }
        }
        return true;
    }
}
