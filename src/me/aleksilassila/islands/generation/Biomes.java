package me.aleksilassila.islands.generation;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.*;

public class Biomes {
    private final Main plugin;

    private final World world;
    public HashMap<Biome, List<Location>> availableLocations;
    private final int biggestIslandSize;

    final int biomeSearchJumpBlocks;
    final int biomeSearchSize;
    final int maxLocationsPerBiome;
    final List<String> biomeBlacklist;

    public Biomes(World world, Main plugin) {
        this.world = world;
        this.biggestIslandSize = plugin.getConfig().getInt("island.BIG");
        this.plugin = plugin;

        this.biomeSearchJumpBlocks = plugin.getConfig().getInt("generation.searchJump");
        this.biomeSearchSize = plugin.getConfig().getInt("generation.searchArea");
        this.maxLocationsPerBiome = plugin.getConfig().getInt("generation.maxVariationsPerBiome");
        this.biomeBlacklist = plugin.getConfig().getStringList("biomeBlacklist");

        this.availableLocations = new HashMap<>();

        // Generate biomes and save them to config
        if (plugin.getBiomesConfig().getString("seed") == null || !plugin.getBiomesConfig().getString("seed").equals(String.valueOf(plugin.islandsSourceWorld.getSeed()))) {
            generateAndSaveBiomes();
        } else { // Load existing biomes from config
            loadBiomesFromConfig();
        }
    }

    private void loadBiomesFromConfig() {
        // Loop biomes
        for (String key : plugin.getBiomesConfig().getKeys(false)) {
            Biome biome = getTargetBiome(key);
            if (biome != null) {
                List<Location> locations = new ArrayList<>();

                // Loop locations inside a biome
                for (String coordinatesIndex : plugin.getBiomesConfig().getConfigurationSection(key).getKeys(false)) {
                    List<String> locationStrings = plugin.getBiomesConfig().getStringList(key + "." + coordinatesIndex);

                    if (locationStrings.size() != 3) continue;

                    Location location = new Location(
                            plugin.islandsSourceWorld,
                            Integer.parseInt(locationStrings.get(0)),
                            Integer.parseInt(locationStrings.get(1)),
                            Integer.parseInt(locationStrings.get(2))
                    );

                    locations.add(location);
                }


                this.availableLocations.put(biome, locations);
            }
        }
    }

    private void generateAndSaveBiomes() {
        plugin.clearBiomesConfig();
        this.availableLocations = generateIslandLocations(biggestIslandSize);

        plugin.getBiomesConfig().set("seed", String.valueOf(plugin.islandsSourceWorld.getSeed()));

        for (Biome biome : availableLocations.keySet()) {
            List<Location> locationsList = availableLocations.get(biome);

            int index = 0;
            for (Location location : locationsList) {
                List<String> stringsList = new ArrayList<>();

                stringsList.add(String.valueOf(location.getBlockX()));
                stringsList.add(String.valueOf(location.getBlockY()));
                stringsList.add(String.valueOf(location.getBlockZ()));

                plugin.getBiomesConfig().set(biome.toString() + "." + index, stringsList);
                index++;
            }

        }

        plugin.saveBiomesConfig();
    }

    @Nullable
    private static Biome getTargetBiome(String biome) {
         Biome targetBiome = null;

         for (Biome b : Biome.values()) {
             if (b.name().equalsIgnoreCase(biome)) {
                 targetBiome = b;
             }
         }

         return targetBiome;
    }

    private boolean isBlacklisted(Biome biome) {
        for (String biomeName : biomeBlacklist) {
            if (biomeName.equalsIgnoreCase(biome.name())) return true;
        }

        return false;
    }

    @NotNull
    public HashMap<Biome, List<Location>> generateIslandLocations(int maxIslandSize) {
        HashMap<Biome, List<Location>> locations = new HashMap<>();
        List<int[]> usedPositions = new ArrayList<int[]>();

        plugin.getLogger().info("Generating biomes...");

        for (int x = 0; x < biomeSearchSize - maxIslandSize; x += biomeSearchJumpBlocks) {
            zLoop: for (int z = 0; z < biomeSearchSize - maxIslandSize; z += biomeSearchJumpBlocks) {
                for (int[] pos : usedPositions) {
                    if (pos[0] <= x && x <= pos[0] + maxIslandSize && pos[1] <= z && z <= pos[1] + maxIslandSize) {
                        z += maxIslandSize;
                        continue zLoop;
                    }
                }

                Biome biome = getBiome(x, z);

                if (isBlacklisted(biome)
                        || (locations.containsKey(biome) && locations.get(biome).size() >= maxLocationsPerBiome)) {
                    continue;
                }

                if (isSuitableLocation(x, z, maxIslandSize, biome)) {
                    Location location = new Location(world, x, 0, z);

                    if (locations.containsKey(biome)) {
                        locations.get(biome).add(location);
                    } else {
                        List<Location> list = new ArrayList<>();
                        list.add(location);
                        locations.put(biome, list);
                    }

                    usedPositions.add(new int[]{x, z});
                    z += maxIslandSize;
                }

            }
        }

        Set<String> biomes = new HashSet<>();

        for (Biome biome : locations.keySet()) {
            biomes.add(biome.name());
        }

        plugin.getLogger().info("Locations generated for " + locations.size() + " biomes: " + String.join(", ", biomes));

        return locations;
    }

    Biome getBiome(int x, int z) {
        return world.getBiome(x, 180, z);
    }

    boolean isSuitableLocation(int xCorner, int zCorner, int rectSize, Biome biome) {
        for (int x = 0; x < rectSize; x += biomeSearchJumpBlocks) {
            for (int z = 0; z < rectSize; z += biomeSearchJumpBlocks) {
                if (getBiome(xCorner + x, zCorner + z) != biome) {
                    return false;
                }
            }
        }
        return true;
    }
}
