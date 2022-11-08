package me.aleksilassila.islands;

import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.BiomeMaterials;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class IslandsConfig {
    protected final Islands islands;
    private final Plugin plugin;

    final Config configuration;

    public HashMap<String, Entry> entries;
    public Entry spawnIsland = null;

    private FileConfiguration config;
    private File configFile;

    public IslandsConfig(Islands islands) {
        this.islands = islands;
        this.plugin = islands.plugin;

        this.configuration = islands.config;
    }

    public FileConfiguration getConfig() {
        if (config != null) return config;

        configFile = new File(Plugin.instance.getDataFolder(), "islands.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            Plugin.instance.saveResource("islands.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        entries = loadEntries();

        return config;
    }

    public void saveIslandsConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Plugin.instance.getLogger().severe("Unable to save islandsConfig");
        }
    }

    public HashMap<String, Entry> loadEntries() {
        HashMap<String, Entry> entries = new HashMap<>();
        for (String islandId : getConfig().getKeys(false)) {
            Entry e = new Entry(this, islandId);
            entries.put(islandId, e);
            if (e.isSpawn) spawnIsland = e;
        }

        return entries;
    }

    public void updateEntries() {
        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (e.shouldUpdate) {
                e.writeToConfig();
                e.shouldUpdate = false;
            }
        }

        saveIslandsConfig();
    }

    @Nullable
    public Entry getEntry(int x, int z, boolean useRawCoordinates) {
        if (!useRawCoordinates) return getEntry(x, z);

        int xIndex = x / configuration.islandSpacing;
        int zIndex = z / configuration.islandSpacing;

        return getEntry(xIndex, zIndex);
    }

    @Nullable
    public Entry getEntry(int xIndex, int zIndex) { // fixme not finding some islands by raw coordinates
        for (Entry e : entries.values()) {
            if (e.xIndex == xIndex && e.zIndex == zIndex) return e;
        }

        return null;
    }

    public Entry createIsland(UUID uuid, int islandSize, int height, Biome biome) {
        int index = 0;

        Set<String> islands = entries.keySet();

        while (true) {
            int[] pos = placement.getIslandPos(index);

            if (!islands.contains(posToIslandId(pos[0], pos[1]))) {
                return addIsland(pos[0], pos[1], islandSize, height, uuid, String.valueOf(getNewHomeId(uuid)), biome);
            }

            index++;
        }
    }

    @NotNull
    private Entry addIsland(int xIndex, int zIndex, int islandSize, int height, UUID uuid, String name, Biome biome) {
        String islandId = posToIslandId(xIndex, zIndex);
        Entry e = new Entry(this, xIndex, zIndex, islandSize, height, uuid, name, biome);
        entries.put(islandId, e);
        e.writeToConfig();
        saveIslandsConfig();
        return e;
    }

    @NotNull
    public List<Entry> getOwnedIslands(UUID uuid) {
        List<Entry> islands = new ArrayList<>();

        for (Entry e : entries.values()) {
            if (uuid.equals(e.uuid)) islands.add(e);
        }

        return islands;
    }

    @NotNull
    public Map<String, Map<String, String>> getIslandsInfo(boolean publicOnly) {
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);

            if (!publicOnly || e.isPublic) {
                String name = e.isPublic ? e.name : islandId;

                Map<String, String> values = new HashMap<>();
                values.put("name", name);
                values.put("owner", e.uuid != null ? e.uuid.toString() : "Server");

                try {
                    String biome = e.biome.toString();
                    values.put("material", BiomeMaterials.valueOf(biome).name());
                } catch (Exception exception) {
                    values.put("material", BiomeMaterials.DEFAULT.name());
                }

                values.put("public", String.valueOf(e.isPublic ? 1 : 0));

                islands.put(islandId, values);
            }
        }

        return islands;
    }

    @NotNull
    public Map<String, Map<String, String>> getIslandsInfo(UUID uuid) {
        Map<String, Map<String, String>> islands = getIslandsInfo(false);
        Map<String, Map<String, String>> finalIslands = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (islands.containsKey(islandId) && uuid.equals(e.uuid))
                finalIslands.put(islandId, islands.get(islandId));
        }

        return finalIslands;
    }

    @NotNull
    public Map<UUID, Integer> getIslandOwners() {
        Map<UUID, Integer> players = new HashMap<>();

        for (Entry e : entries.values()) {
            if (e.uuid != null) {
                if (players.containsKey(e.uuid)) {
                    players.put(e.uuid, players.get(e.uuid) + 1);
                } else {
                    players.put(e.uuid, 1);
                }
            }
        }

        return players;
    }

    @Nullable
    public Entry getIslandByName(String name) {
        for (Entry e : entries.values()) {
            if (name.equalsIgnoreCase(e.name) && e.isPublic) {
                return e;
            }
        }

        return null;
    }

    @Nullable
    public Entry getHomeIsland(UUID uuid, int homeId) {
        List<Entry> allIslands = getOwnedIslands(uuid);

        for (Entry e : allIslands) {
            if (e.homeId == homeId) {
                return e;
            }
        }

        return null;
    }

    @Nullable
    public int getLowestHome(UUID uuid) {
        List<Entry> allIslands = getOwnedIslands(uuid);

        int lowestHome = -1;

        for (Entry e : allIslands) {
            if (e.homeId != -1 && (e.homeId < lowestHome || lowestHome == -1)) {
                lowestHome = e.homeId;
            }
        }

        return lowestHome;
    }

    /**
     * Checks if block (relative to the position of
     * the island) is inside water flow are.
     * <p>
     * From bottom to up the area is first a half sphere with diameter
     * of the island width and then a cylinder with same diameter.
     */
    public boolean isBlockInWaterFlowArea(int x, int y, int z) {
        int xIndex = x / configuration.islandSpacing;
        int zIndex = z / configuration.islandSpacing;

        Entry e = getEntry(xIndex, zIndex);
        if (e == null)
            return false;

        int[][] ic = getIslandCorner(xIndex, zIndex, e.size);

        int relativeX = x - ic[0][0];
        int relativeZ = z - ic[0][1];
        int relativeY = y - getIslandY(xIndex, zIndex);

        if (relativeY <= e.size / 2d) {
            return IslandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, e.size);
        } else {
            return IslandGeneration.isBlockInIslandCylinder(relativeX, relativeZ, e.size);
        }
    }

    public int getNewHomeId(UUID uuid) {
        List<Integer> homeIds = new ArrayList<>();

        for (Entry e : getOwnedIslands(uuid)) {
            homeIds.add(e.homeId);
        }

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (!homeIds.contains(i)) return i;
        }

        return 0;
    }

    // UTILS

    int getIslandY(int xIndex, int zIndex) {
        return 10 + ((xIndex + zIndex) % 3) * configuration.verticalSpacing;
    }

    String posToIslandId(int xIndex, int zIndex) {
        return xIndex + "x" + zIndex;
    }

    public int[][] getIslandCorner(int xIndex, int zIndex, int size) {
        return new int[][]{
                new int[]{
                        xIndex * configuration.islandSpacing + configuration.islandSpacing / 2 - size / 2,
                        zIndex * configuration.islandSpacing + configuration.islandSpacing / 2 - size / 2
                }, new int[]{
                xIndex * configuration.islandSpacing + configuration.islandSpacing / 2 + size / 2 - 1,
                zIndex * configuration.islandSpacing + configuration.islandSpacing / 2 + size / 2 - 1
        }
        };
    }

    public int[][] getIslandPlotCorner(int xIndex, int zIndex) {
        return new int[][]{
                new int[]{
                        xIndex * configuration.islandSpacing,
                        zIndex * configuration.islandSpacing
                }, new int[]{
                xIndex * configuration.islandSpacing + configuration.islandSpacing - 1,
                zIndex * configuration.islandSpacing + configuration.islandSpacing - 1
        }
        };
    }


    public static class placement {
        public static int getLayer(int index) {
            return (int) Math.floor(Math.sqrt(index));
        }

        public static int getLayerSize(int layer) {
            return 2 * layer + 1;
        }

        public static int firstOfLayer(int layer) {
            return layer * layer;
        }

        public static int[] getIslandPos(int index) {
            int layer = getLayer(index);

            int x = Math.min(index - firstOfLayer(layer), layer);
            int z = (index - firstOfLayer(layer) < layer + 1) ? layer : firstOfLayer(layer) + getLayerSize(layer) - 1 - index;

            return new int[]{x, z};
        }

        // TODO: Optimize
        public static int getIslandIndex(int[] pos) {
            int index = 0;
            while (!Arrays.equals(getIslandPos(index), pos)) {
                index++;
            }

            return index;
        }

        public static int getIslandIndex(String islandId) {
            try {
                return getIslandIndex(new int[]{Integer.parseInt(islandId.split("x")[0]), Integer.parseInt(islandId.split("x")[1])});
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
}
