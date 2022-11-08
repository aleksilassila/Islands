package me.aleksilassila.islands.world;

import me.aleksilassila.islands.Islands;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SourceWorld extends AbstractWorld {
    private final File biomesCacheFile = new File(getWorld().getWorldFolder(), "biomeCache.yml");

    private HashMap<Biome, List<Location>> availableLocations;

    public SourceWorld(Islands islands) {
        super(islands);
        this.availableLocations = getAvailableLocations(); // Generate biome cache on startup
    }

    @Override
    String getWorldName() {
        return "islandsSource";
    }

    @Override
    World createWorld(boolean exists) {
        WorldCreator wc = new WorldCreator("islandsSource");
        World world;

        if (exists) {
            plugin.getLogger().info("Islands source world set to islandsSource");
            world = wc.createWorld();
        } else {
            plugin.getLogger().info("No islands source world found. Creating one...");

            wc.environment(World.Environment.NORMAL);
            wc.type(WorldType.NORMAL);
            wc.generateStructures(false);
            world = wc.createWorld();
            world.setDifficulty(Difficulty.PEACEFUL);
        }

        return world;
    }

    @NotNull
    public HashMap<Biome, List<Location>> getAvailableLocations() {
        if (availableLocations == null) {
            // Load biomes from config

            this.availableLocations = loadBiomesFromFile();

            if (this.availableLocations == null) {
                this.availableLocations = generateIslandLocations();
                saveToFile(availableLocations);
            }
        }

        return availableLocations;
    }

    @Nullable
    private HashMap<Biome, List<Location>> loadBiomesFromFile() {
        HashMap<Biome, List<Location>> availableLocations = new HashMap<>();
        FileConfiguration biomesCache = new YamlConfiguration();

        try {
            biomesCache.load(biomesCacheFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().info("Could not load biome cache file.");
            return null;
        }

        // Loop biomes
        for (String key : biomesCache.getKeys(false)) {
            Biome biome = parseBiome(key);
            if (biome != null) {
                List<Location> locations = new ArrayList<>();

                // Loop locations inside a biome
                for (String coordinatesIndex : biomesCache.getConfigurationSection(key).getKeys(false)) {
                    List<String> locationStrings = biomesCache.getStringList(key + "." + coordinatesIndex);

                    if (locationStrings.size() != 3) continue;

                    Location location = new Location(
                            world,
                            Integer.parseInt(locationStrings.get(0)),
                            Integer.parseInt(locationStrings.get(1)),
                            Integer.parseInt(locationStrings.get(2))
                    );

                    locations.add(location);
                }


                availableLocations.put(biome, locations);
            }
        }

        return availableLocations;
    }


    @NotNull
    public HashMap<Biome, List<Location>> generateIslandLocations() {
        HashMap<Biome, List<Location>> locations = new HashMap<>();
        List<int[]> usedPositions = new ArrayList<>();

        plugin.getLogger().info("Generating biomes...");
        for (int x = 0; x < config.biomeSearchSize - config.biggestIslandSize; x += config.biomeSearchJumpBlocks) {
            zLoop:
            for (int z = 0; z < config.biomeSearchSize - config.biggestIslandSize; z += config.biomeSearchJumpBlocks) {
                for (int[] pos : usedPositions) {
                    if (pos[0] <= x && x <= pos[0] + config.biggestIslandSize && pos[1] <= z && z <= pos[1] + config.biggestIslandSize) {
                        z += config.biggestIslandSize;
                        continue zLoop;
                    }
                }

                Biome biome = getBiome(x, z);

                if (isBlacklisted(biome)
                        || (locations.containsKey(biome) && locations.get(biome).size() >= config.maxLocationsPerBiome)) {
                    continue;
                }

                if (isSuitableLocation(x, z, config.biggestIslandSize, biome)) {
                    Location location = new Location(world, x, 0, z);

                    if (locations.containsKey(biome)) {
                        locations.get(biome).add(location);
                    } else {
                        List<Location> list = new ArrayList<>();
                        list.add(location);
                        locations.put(biome, list);
                    }

                    usedPositions.add(new int[]{x, z});
                    z += config.biggestIslandSize;
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

    private boolean isBlacklisted(Biome biome) {
        for (String biomeName : config.biomeBlacklist) {
            if (biomeName.equalsIgnoreCase(biome.name())) return true;
        }

        return false;
    }


    @Nullable
    private static Biome parseBiome(String biome) {
        Biome targetBiome = null;

        for (Biome b : Biome.values()) {
            if (b.name().equalsIgnoreCase(biome)) {
                targetBiome = b;
            }
        }

        return targetBiome;
    }

    private Biome getBiome(int x, int z) {
        return world.getBiome(x, 180, z);
    }

    boolean isSuitableLocation(int xCorner, int zCorner, int rectSize, Biome biome) {
        for (int x = 0; x < rectSize; x += config.biomeSearchJumpBlocks) {
            for (int z = 0; z < rectSize; z += config.biomeSearchJumpBlocks) {
                if (getBiome(xCorner + x, zCorner + z) != biome) {
                    return false;
                }
            }
        }
        return true;
    }

    public Biome getRandomBiome() {
        int size = availableLocations.keySet().size();
        int item = new Random().nextInt(size);
        int i = 0;

        for (Biome biome : availableLocations.keySet()) {
            if (i == item) return biome;
            i++;
        }

        // To make it always return biome (shouldn't ever get here)
        return new ArrayList<>(availableLocations.keySet()).get(0);
    }

    public void saveToFile(HashMap<Biome, List<Location>> availableLocations) {
        FileConfiguration biomesCache = new YamlConfiguration();

        for (Biome biome : availableLocations.keySet()) {
            List<Location> locationsList = availableLocations.get(biome);

            int index = 0;
            for (Location location : locationsList) {
                List<String> stringsList = new ArrayList<>();

                stringsList.add(String.valueOf(location.getBlockX()));
                stringsList.add(String.valueOf(location.getBlockY()));
                stringsList.add(String.valueOf(location.getBlockZ()));

                biomesCache.set(biome.toString() + "." + index, stringsList);
                index++;
            }

        }

        try {
            biomesCache.save(biomesCacheFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save biomesConfig");
        }
    }
}
