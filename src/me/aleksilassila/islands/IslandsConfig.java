package me.aleksilassila.islands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum IslandsConfig {
    INSTANCE;

    public final int islandSpacing;
    public final int verticalSpacing;

    public static final HashMap<String, Entry> entries = loadEntries();
    public static String spawnIsland;

    private static FileConfiguration config;
    private static File configFile;

    IslandsConfig() {
        this.islandSpacing = Islands.instance.getConfig().getInt("generation.islandGridSpacing");
        this.verticalSpacing = Islands.instance.getConfig().getInt("generation.islandGridVerticalSpacing");
    }

    public static FileConfiguration getConfig() {
        if (config != null) return config;

        configFile = new File(Islands.instance.getDataFolder(), "islands.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            Islands.instance.saveResource("islands.yml", false);
         }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

    public static void saveIslandsConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Islands.instance.getLogger().severe("Unable to save islandsConfig");
        }
    }

    public static HashMap<String, Entry> loadEntries() {
        HashMap<String, Entry> entries = new HashMap<>();
        for (String islandId : getConfig().getKeys(false)) {
            Entry e = new Entry(islandId);
            entries.put(islandId, e);
            if (e.isSpawn) spawnIsland = islandId;
        }

        return entries;
    }

    public static void updateEntries() {
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
    public static Entry getEntry(int x, int z, boolean useRawCoordinates) {
        if (!useRawCoordinates) return getEntry(x, z);

        int xIndex = x / INSTANCE.islandSpacing;
        int zIndex = z / INSTANCE.islandSpacing;

        return getEntry(xIndex, zIndex);
    }

    @Nullable
    public static Entry getEntry(int xIndex, int zIndex) {
        for (Entry e : entries.values()) {
            if (e.xIndex == xIndex && e.zIndex == zIndex) return e;
        }

        return null;
    }

    public static Entry createIsland(UUID uuid, int islandSize, int height, Biome biome) {
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
    private static Entry addIsland(int xIndex, int zIndex, int islandSize, int height, UUID uuid, String name, Biome biome) {
        String islandId = posToIslandId(xIndex, zIndex);
        Entry e = new Entry(xIndex, zIndex, islandSize, height, uuid, name, biome);
        entries.put(islandId, e);
        e.writeToConfig();
        return e;
    }

    @Nullable
    public static String getIslandId(int x, int z) {
        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (x / INSTANCE.islandSpacing == e.xIndex) {
                if (z / INSTANCE.islandSpacing == e.zIndex) {
                    return islandId;
                }
            }
        }

        return null;
    }

    @NotNull
    public static List<String> getOwnedIslands(UUID uuid) {
        List<String> islands = new ArrayList<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (e.uuid == uuid) islands.add(islandId);
        }

        return islands;
    }

    @NotNull
    public static Map<String, Map<String, String>> getIslandsInfo(boolean publicOnly) {
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            boolean isPublic = e.isPublic;

            if (!publicOnly || isPublic) {
                String name = isPublic ? e.name : islandId;
                String ownerUUID = e.uuid.toString();

                Map<String, String> values = new HashMap<>();
                values.put("name", name);
                values.put("owner", ownerUUID);

                try {
                    String biome = e.biome.toString();
                    values.put("material", BiomeMaterials.valueOf(biome).name());
                } catch (Exception exception) {
                    values.put("material", BiomeMaterials.DEFAULT.name());
                }

                values.put("public", String.valueOf(isPublic ? 1 : 0));

                islands.put(islandId, values);
            }
        }

        return islands;
    }

    @NotNull
    public static Map<String, Map<String, String>> getIslandsInfo(String uuid) {
        Map<String, Map<String, String>> islands = getIslandsInfo(false);
        Map<String, Map<String, String>> finalIslands = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (islands.containsKey(islandId) && uuid.equals(e.uuid.toString()))
                finalIslands.put(islandId, islands.get(islandId));
        }

        return finalIslands;
    }

    @NotNull
    public static Map<String, Integer> getPlayers() {
        Map<String, Integer> players = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            String uuid = e.uuid.toString();

            if (uuid != null) {
                if (players.containsKey(uuid)) {
                    players.put(uuid, players.get(uuid) + 1);
                } else {
                    players.put(uuid, 1);
                }
            }
        }

        return players;
    }

    @Nullable
    public static String getIslandByName(String name) {
        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (name.equalsIgnoreCase(e.name) && e.isPublic) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public static String getHomeIsland(UUID uuid, int homeId) {
        List<String> allIslands = getOwnedIslands(uuid);

        for (String islandId : allIslands) {
            if (entries.get(islandId).homeId == homeId) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public static int getLowestHome(UUID uuid) {
        List<String> allIslands = getOwnedIslands(uuid);

        int lowestHome = -1;

        for (String islandId : allIslands) {
            int home = entries.get(islandId).homeId;

            if (home != -1 && (home < lowestHome || lowestHome == -1)) {
                lowestHome = home;
            }
        }

        return lowestHome;
    }

    @Nullable
    public static Location getIslandSpawn(String islandId) {
        if (entries.containsKey(islandId)) {
            return new Location(
                    Islands.islandsWorld,
                    entries.get(islandId).spawnPosition[0],
                    entries.get(islandId).y + 100,
                    entries.get(islandId).spawnPosition[1]
            );
        } else {
            return null;
        }
    }

    /**
     * Checks if block (relative to the position of
     * the island) is inside water flow are.
     *
     * From bottom to up the area is first a half sphere with diameter
     * of the island width and then a cylinder with same diameter.
     */
    public static boolean isBlockInWaterFlowArea(int x, int y, int z) {
        int xIndex = x / INSTANCE.islandSpacing;
        int zIndex = z / INSTANCE.islandSpacing;
        int islandLowY = getIslandY(xIndex, zIndex);

        Entry e = getEntry(xIndex, zIndex);
        if (e == null)
            return false;

        int relativeX = x - (xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - e.size / 2);
        int relativeZ = z - (zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - e.size / 2);
        int relativeY = y - islandLowY;

        if (relativeY <= e.size / 2d) {
            return IslandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, e.size);
        } else {
            return IslandGeneration.isBlockInIslandCylinder(relativeX, relativeZ, e.size);
        }
    }

    public static int getNewHomeId(UUID uuid) {
        List<Integer> homeIds = new ArrayList<>();

        for (String islandId : getOwnedIslands(uuid)) {
            homeIds.add(entries.get(islandId).homeId);
        }

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (!homeIds.contains(i)) return i;
        }

        return 0;
    }

    // UTILS

    private static int getIslandY(int xIndex, int zIndex) {
        return 10 + ((xIndex + zIndex) % 3) * INSTANCE.verticalSpacing;
    }

    static String posToIslandId(int xIndex, int zIndex) {
        return xIndex + "x" + zIndex;
    }

    @NotNull
    public static String getUUID(String islandId) {
        return Optional.ofNullable(getConfig().getString(islandId + ".UUID")).orElse("");
    }

    public static class Entry {
        public String islandId;
        public int xIndex;
        public int zIndex;
        public int size;
        public int height;
        public long claimId;
        public Biome biome;
        public UUID uuid;
        public String name;
        public String home;
        public int homeId;
        public boolean isPublic;
        public int y;
        public int[] spawnPosition;
        public boolean isSpawn;

        boolean shouldUpdate = false;

        public Entry(String islandId) {
            FileConfiguration fc = getConfig();
            this.islandId = islandId;

            this.xIndex = fc.getInt(islandId + ".xIndex");
            this.xIndex = fc.getInt(islandId + ".xIndex");

            String stringUuid = fc.getString(islandId + ".UUID");

            this.uuid = stringUuid != null ? UUID.fromString(stringUuid) : null;
            this.name = fc.getString(islandId + ".name", islandId);
            this.home = fc.getString(islandId + ".home");
            this.size = fc.getInt(islandId + ".size");
            this.height = fc.getInt(islandId + ".height");
            this.isPublic = fc.getBoolean(islandId + ".public", false);
            this.biome = Biome.valueOf(fc.getString(islandId + ".biome", "PLAINS"));
            this.homeId = fc.getInt(islandId + ".home", getNewHomeId(uuid));
            this.spawnPosition = new int[] {
                    fc.getInt(islandId + ".spawnPoint.x"),
                    fc.getInt(islandId + ".spawnPoint.z")
            };
            this.y = fc.getInt(islandId + ".y");
            this.isSpawn = fc.getBoolean(islandId + ".isSpawn", false);

            this.claimId = fc.getLong(islandId + ".claimId");
        }

        public Entry(int xIndex, int zIndex, int size, int height, UUID uuid, String name, Biome biome) {
            this.xIndex = xIndex;
            this.zIndex = zIndex;
            this.size = size;
            this.height = height;
            this.uuid = uuid;
            this.name = name;
            this.biome = biome;
            this.homeId = getNewHomeId(uuid);

            int realX = xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - size / 2;
            int realY = getIslandY(xIndex, zIndex);
            int realZ = zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - size / 2;

            CreateClaimResult r = Islands.gp.dataStore.createClaim(Islands.islandsWorld,
                realX - size, realX + size,
                realY - size, realY + size,
                realZ - size, realZ + size,
                uuid, null, new Random().nextLong(), Bukkit.getPlayer(uuid));

            if (r.succeeded)
                this.claimId = r.claim.getID();

            this.spawnPosition = new int[2];

            this.spawnPosition[0] = realX + size / 2;
            this.spawnPosition[1] = realZ + size / 2;
            this.isPublic = false;
            this.isSpawn = false;

        }

        public void delete() {
            getConfig().set(islandId, null);
            Islands.gp.dataStore.deleteClaim(Islands.gp.dataStore.getClaim(claimId));
            entries.remove(islandId);
        }

        public void writeToConfig() {
            int realX = xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - size / 2;
            int realZ = zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - size / 2;

            String islandId = posToIslandId(xIndex, zIndex);

            getConfig().set(islandId + ".xIndex", xIndex);
            getConfig().set(islandId + ".zIndex", zIndex);

            getConfig().set(islandId + ".x", realX);
            getConfig().set(islandId + ".y", y);
            getConfig().set(islandId + ".z", realZ);

            getConfig().set(islandId + ".spawnPoint.x", spawnPosition[0]);
            getConfig().set(islandId + ".spawnPoint.z", spawnPosition[1]);

            getConfig().set(islandId + ".UUID", uuid.toString());
            getConfig().set(islandId + ".name", name);
            getConfig().set(islandId + ".home", homeId);
            getConfig().set(islandId + ".size", size);
            getConfig().set(islandId + ".height", height);
            getConfig().set(islandId + ".public", isPublic);
            getConfig().set(islandId + ".biome", biome.name());

            getConfig().set(islandId + ".claimId", claimId);
            getConfig().set(islandId + ".isSpawn", isSpawn);

            saveIslandsConfig();
        }


        public void setSpawnPosition(int x, int z) {
            spawnPosition = new int[] {x, z};
            shouldUpdate = true;
        }

        public void unnameIsland() {
            name = String.valueOf(homeId);
            isPublic = false;

            shouldUpdate = true;
        }

        public void nameIsland(String name){
            isPublic = true;
            this.name = name;

            shouldUpdate = true;
        }

        public void giveIsland(OfflinePlayer player) {
            this.uuid = player.getUniqueId();
            this.homeId = getNewHomeId(player.getUniqueId());

            shouldUpdate = true;
        }

        public void makeServerOwned() {
            this.uuid = null;
            this.homeId = -1;

            shouldUpdate = true;
        }

        public void setSpawnIsland() {
            isSpawn = !isSpawn;
            IslandsConfig.spawnIsland = isSpawn ? this.islandId : null;


            shouldUpdate = true;
        }
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
