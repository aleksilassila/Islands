package me.aleksilassila.islands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.BiomeMaterials;
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

    public static String createIsland(UUID uuid, int islandSize, int height, Biome biome) {
        int index = 0;

        Set<String> islands = getConfig().getKeys(false);

        while (true) {
            int[] pos = placement.getIslandPos(index);

            if (!islands.contains(posToIslandId(pos[0], pos[1]))) {
                return addIsland(pos[0], pos[1], islandSize, height, uuid, String.valueOf(getNewHomeId(uuid)), biome);
            }

            index++;
        }
    }

    @NotNull
    private static String addIsland(int xIndex, int zIndex, int islandSize, int height, UUID uuid, String name, Biome biome) {
        int realX = xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2;
        int realY = getIslandY(xIndex, zIndex);
        int realZ = zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2;

        int home = getNewHomeId(uuid);

        String islandId = posToIslandId(xIndex, zIndex);

        getConfig().set(islandId + ".xIndex", xIndex);
        getConfig().set(islandId + ".zIndex", zIndex);

        getConfig().set(islandId + ".x", realX);
        getConfig().set(islandId + ".y", realY);
        getConfig().set(islandId + ".z", realZ);

        getConfig().set(islandId + ".spawnPoint.x", realX + islandSize / 2);
        getConfig().set(islandId + ".spawnPoint.z", realZ + islandSize / 2);

        getConfig().set(islandId + ".UUID", uuid.toString());
        getConfig().set(islandId + ".name", name);
        getConfig().set(islandId + ".home", home);
        getConfig().set(islandId + ".size", islandSize);
        getConfig().set(islandId + ".height", height);
        getConfig().set(islandId + ".public", false);
        getConfig().set(islandId + ".biome", biome.name());

        getConfig().set(islandId + ".protect.building", true);
        getConfig().set(islandId + ".protect.containers", true);
        getConfig().set(islandId + ".protect.doors", true);
        getConfig().set(islandId + ".protect.utility", true);

        saveIslandsConfig();

        return islandId;
    }

    @Nullable
    public static String getIslandId(int x, int z) {
        for (String islandId : getConfig().getKeys(false)) {
            if (x / INSTANCE.islandSpacing == getConfig().getInt(islandId + ".xIndex")) {
                if (z / INSTANCE.islandSpacing == getConfig().getInt(islandId + ".zIndex")) {
                    return islandId;
                }
            }
        }

        return null;
    }

    @NotNull
    public static List<String> getIslandIds(UUID uuid) {
        List<String> islands = new ArrayList<>();

        for (String islandId : getConfig().getKeys(false)) {
            String islandUUID = getConfig().getString(islandId + ".UUID");

            if (uuid.toString().equals(islandUUID))
                islands.add(islandId);
        }

        return islands;
    }

    @NotNull
    public static Map<String, Map<String, String>> getIslandsInfo(boolean publicOnly) {
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : getConfig().getKeys(false)) {
            boolean isPublic = getConfig().getBoolean(islandId + ".public");

            if (!publicOnly || isPublic) {
                String name = isPublic ? getConfig().getString(islandId + ".name") : islandId;
                String ownerUUID = getConfig().getString(islandId + ".UUID");

                Map<String, String> values = new HashMap<>();
                values.put("name", name);
                values.put("owner", ownerUUID);

                try {
                    String biome = getConfig().getString(islandId + ".biome");
                    values.put("material", BiomeMaterials.valueOf(biome).name());
                } catch (Exception e) {
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
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : getConfig().getKeys(false)) {
            String ownerUUID = getConfig().getString(islandId + ".UUID");
            if (!uuid.equalsIgnoreCase(ownerUUID)) continue;

            String name = getConfig().getBoolean(islandId + ".public")
                    ? getConfig().getString(islandId + ".name")
                    : islandId;

            Map<String, String> values = new HashMap<>();
            values.put("name", name);

            try {
                String biome = getConfig().getString(islandId + ".biome");
                values.put("material", BiomeMaterials.valueOf(biome).name());
            } catch (Exception e) {
                values.put("material", BiomeMaterials.DEFAULT.name());
            }

            islands.put(islandId, values);
        }

        return islands;
    }

    @NotNull
    public static Map<String, Integer> getPlayers() {
        Map<String, Integer> players = new HashMap<>();

        for (String islandId : getConfig().getKeys(false)) {
            String uuid = getConfig().getString(islandId + ".UUID");

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
        for (String islandId : getConfig().getKeys(false)) {
            if (name.equalsIgnoreCase(getConfig().getString(islandId + ".name")) && getConfig().getBoolean(islandId + ".public")) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public static String getHomeIsland(UUID uuid, int homeId) {
        List<String> allIslands = getIslandIds(uuid);

        for (String islandId : allIslands) {
            if (getConfig().getInt(islandId + ".home", -1) == homeId) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public static int getLowestHome(UUID uuid) {
        List<String> allIslands = getIslandIds(uuid);

        int lowestHome = -1;

        for (String islandId : allIslands) {
            int home = getConfig().getInt(islandId + ".home", -1);

            if (home != -1 && (home < lowestHome || lowestHome == -1)) {
                lowestHome = home;
            }
        }

        return lowestHome;
    }

    @Nullable
    public static Location getIslandSpawn(String islandId) {
        if (getConfig().getKeys(false).contains(islandId)) {
            return new Location(
                    Islands.islandsWorld,
                    getConfig().getInt(islandId + ".spawnPoint.x"),
                    getConfig().getInt(islandId + ".y") + 100,
                    getConfig().getInt(islandId + ".spawnPoint.z")
            );
        } else {
            return null;
        }
    }

    @Nullable
    public static String getSpawnIsland() {
        for (String islandId : getConfig().getKeys(false)) {
            if (getConfig().getBoolean(islandId + ".isSpawn")) {
                return islandId;
            }
        }

        return null;
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

        int islandSize = getConfig().getInt(posToIslandId(xIndex, zIndex)  + ".size");

        int relativeX = x - (xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2);
        int relativeZ = z - (zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2);
        int relativeY = y - islandLowY;

        if (relativeY <= islandSize / 2.0) {
            return IslandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, islandSize);
        } else {
            return IslandGeneration.isBlockInIslandCylinder(relativeX, relativeZ, islandSize);
        }
    }

    @Nullable
    public static String getBlockOwnerUUID(int x, int z) {
        return getBlockOwnerUUID(x, z, false);
    }

    /**
     * Get block owner.
     *
     * @param plotOnly If false, the space between islands will be considered
     *                 as no man's land. If true, the whole plot belongs to the
     *                 island owner.
     */
    @Nullable
    public static String getBlockOwnerUUID(int x, int z, boolean plotOnly) {
        int plotX = x / INSTANCE.islandSpacing;
        int plotZ = z / INSTANCE.islandSpacing;

        // Check if block is inside an island, not only inside plot
        if (!plotOnly) {
            int islandSize = getConfig().getInt(posToIslandId(plotX, plotZ) + ".size");

            int relativeX = x - (plotX * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2);
            int relativeZ = z - (plotZ * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2);

            boolean isInside = IslandGeneration.isBlockInIslandCylinder(relativeX + 2, relativeZ + 2, islandSize + 4);

            if (!isInside) return null;
        }

        return Optional.ofNullable(getConfig().getString(posToIslandId(plotX, plotZ) + ".UUID")).orElse("Server");
    }

    public static int getNewHomeId(UUID uuid) {
        List<String> ids = getIslandIds(uuid);
        List<Integer> homeIds = new ArrayList<>();

        for (String islandId : ids) {
            int homeNumber = getConfig().getInt(islandId + ".home");
            homeIds.add(homeNumber);
        }

        int home = getNumberOfIslands(uuid) + 1;

        for (int i = 1; i <= getNumberOfIslands(uuid) + 1; i++) {
            if (!homeIds.contains(i)) home = i;
        }

        return home;
    }

    // UTILS

    private static int getIslandY(int xIndex, int zIndex) {
        return 10 + ((xIndex + zIndex) % 3) * INSTANCE.verticalSpacing;
    }

    public static int getNumberOfIslands(UUID uuid) {
        return getIslandIds(uuid).size();
    }

    static String posToIslandId(int xIndex, int zIndex) {
        return xIndex + "x" + zIndex;
    }

    @NotNull
    public static String getUUID(String islandId) {
        return Optional.ofNullable(getConfig().getString(islandId + ".UUID")).orElse("");
    }

    // MANAGMENT

    public static void updateIsland(String islandId, int islandSize, int height, Biome biome) {
        int xIndex = getConfig().getInt(islandId + ".xIndex");
        int zIndex = getConfig().getInt(islandId + ".zIndex");

        int realX = xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2;
        int realZ = zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - islandSize / 2;

        getConfig().set(islandId + ".x", realX);
        getConfig().set(islandId + ".z", realZ);

        getConfig().set(islandId + ".size", islandSize);
        getConfig().set(islandId + ".height", height);
        getConfig().set(islandId + ".biome", biome.name());

        saveIslandsConfig();
    }

    public static void addTrusted(String islandId, String uuid) {
        if (!getConfig().contains(islandId + ".trusted." + uuid)) {
            getConfig().set(islandId + ".trusted." + uuid + ".build", true);
            getConfig().set(islandId + ".trusted." + uuid + ".accessContainers", false);
            getConfig().set(islandId + ".trusted." + uuid + ".accessDoors", false);
            getConfig().set(islandId + ".trusted." + uuid + ".accessUtility", false);

            saveIslandsConfig();
        }
    }

    public static void removeTrusted(String islandId, String uuid) {
        getConfig().set(islandId + ".trusted." + uuid, null);
        saveIslandsConfig();
    }

    // Per player

    public static void setBuildAccess(String islandId, String uuid, boolean value) {
        getConfig().set(islandId + ".trusted." + uuid + ".build", value);
        saveIslandsConfig();
    }

    public static void setContainerAccess(String islandId, String uuid, boolean value) {
        getConfig().set(islandId + ".trusted." + uuid + ".accessContainers", value);
        saveIslandsConfig();
    }

    public static void setDoorAccess(String islandId, String uuid, boolean value) {
        getConfig().set(islandId + ".trusted." + uuid + ".accessDoors", value);
        saveIslandsConfig();
    }

    public static void setUtilityAccess(String islandId, String uuid, boolean value) {
        getConfig().set(islandId + ".trusted." + uuid + ".accessUtility", value);
        saveIslandsConfig();
    }

    public static boolean canBuild(String islandId, String uuid) {
        return getConfig().getBoolean(islandId + ".trusted." + uuid + ".build", false);
    }

    public static boolean canAccessContainers(String islandId, String uuid) {
        return getConfig().getBoolean(islandId + ".trusted." + uuid + ".accessContainers", false);
    }

    public static boolean canAccessDoors(String islandId, String uuid) {
        return getConfig().getBoolean(islandId + ".trusted." + uuid + ".accessDoors", false);
    }

    public static boolean canUseUtility(String islandId, String uuid) {
        return getConfig().getBoolean(islandId + ".trusted." + uuid + ".accessUtility", false);
    }

    // Global for island
    public static void setBuildProtection(String islandId, boolean protect) {
        getConfig().set(islandId + ".protect.building", protect);
        saveIslandsConfig();
    }

    public static void setContainerProtection(String islandId, boolean protect) {
        getConfig().set(islandId + ".protect.containers", protect);
        saveIslandsConfig();
    }

    public static void setDoorProtection(String islandId, boolean protect) {
        getConfig().set(islandId + ".protect.doors", protect);
        saveIslandsConfig();
    }

    public static void setUtilityProtection(String islandId, boolean protect) {
        getConfig().set(islandId + ".protect.utility", protect);
        saveIslandsConfig();
    }

    public static boolean buildProtection(String islandId) {
        return getConfig().getBoolean(islandId + ".protect.building", true);
    }

    public static boolean containerProtection(String islandId) {
        return getConfig().getBoolean(islandId + ".protect.containers", true);
    }

    public static boolean doorProtection(String islandId) {
        return getConfig().getBoolean(islandId + ".protect.doors", true);
    }

    public static boolean utilityProtection(String islandId) {
        return getConfig().getBoolean(islandId + ".protect.utility", true);
    }

    public static void setSpawnPoint(String islandId, int x, int z) {
        getConfig().set(islandId + ".spawnPoint.x", x);
        getConfig().set(islandId + ".spawnPoint.z", z);

        saveIslandsConfig();
    }

    public static void unnameIsland(String islandId) {
        int homeId = getConfig().getInt(islandId + ".home", -1);

        getConfig().set(islandId + ".name", String.valueOf(homeId));
        getConfig().set(islandId + ".public", false);

        saveIslandsConfig();
    }

    public static void nameIsland(String islandId, String name){
        getConfig().set(islandId + ".name", name);
        getConfig().set(islandId + ".public", true);

        saveIslandsConfig();
    }

    public static void giveIsland(String islandId, OfflinePlayer player) {
        getConfig().set(islandId + ".home", getNewHomeId(player.getUniqueId()));
        getConfig().set(islandId + ".UUID", player.getUniqueId().toString());

        saveIslandsConfig();
    }

    public static void giveIsland(String islandId) {
        getConfig().set(islandId + ".home", null);
        getConfig().set(islandId + ".UUID", null);

        saveIslandsConfig();
    }

    public static void deleteIsland(String islandId) {
        getConfig().set(islandId, null);

        saveIslandsConfig();
    }

    public static boolean setSpawnIsland(String islandId) {
        if (getConfig().getConfigurationSection(islandId) == null) return false;

        if (getConfig().getBoolean(islandId + ".isSpawn")) {
            getConfig().set(islandId + ".isSpawn", false);
            return true;
        }

        for (String island : getConfig().getKeys(false)) {
            if (getConfig().getBoolean(island + ".isSpawn")) {
                getConfig().set(island + ".isSpawn", false);
            }
        }

        getConfig().set(islandId + ".isSpawn", true);
        saveIslandsConfig();
        return true;
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
