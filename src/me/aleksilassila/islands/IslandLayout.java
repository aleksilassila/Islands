package me.aleksilassila.islands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.TrustedPlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandLayout {
    private final Islands plugin;

    public final int islandSpacing;
    public final int verticalSpacing;

    public IslandLayout(Islands plugin) {
        this.plugin = plugin;

        this.islandSpacing = plugin.getConfig().getInt("generation.islandGridSpacing");
        this.verticalSpacing = plugin.getConfig().getInt("generation.islandGridVerticalSpacing");
    }

    private FileConfiguration getIslandsConfig() {
        return plugin.getIslandsConfig();
    }

    public String createIsland(UUID uuid, int islandSize, int height, Biome biome) {
        int index = 0;

        while (true) {
            int[] pos = placement.getIslandPos(index);

            if (!getIslandsConfig().getKeys(false).contains(posToIslandId(pos[0], pos[1]))) {
                return addIslandToConfig(pos[0], pos[1], islandSize, height, uuid, String.valueOf(getNewHomeId(uuid)), biome);
            }

            index++;
        }
    }

    @NotNull
    private String addIslandToConfig(int xIndex, int zIndex, int islandSize, int height, UUID uuid, String name, Biome biome) {
        int realX = xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;
        int realY = getIslandY(xIndex, zIndex);
        int realZ = zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;

        int home = getNewHomeId(uuid);

        String islandId = posToIslandId(xIndex, zIndex);

        getIslandsConfig().set(islandId + ".xIndex", xIndex);
        getIslandsConfig().set(islandId + ".zIndex", zIndex);

        getIslandsConfig().set(islandId + ".x", realX);
        getIslandsConfig().set(islandId + ".y", realY);
        getIslandsConfig().set(islandId + ".z", realZ);

        getIslandsConfig().set(islandId + ".spawnPoint.x", realX + islandSize / 2);
        getIslandsConfig().set(islandId + ".spawnPoint.z", realZ + islandSize / 2);

        getIslandsConfig().set(islandId + ".UUID", uuid.toString());
        getIslandsConfig().set(islandId + ".name", name);
        getIslandsConfig().set(islandId + ".home", home);
        getIslandsConfig().set(islandId + ".size", islandSize);
        getIslandsConfig().set(islandId + ".height", height);
        getIslandsConfig().set(islandId + ".public", false);
        getIslandsConfig().set(islandId + ".biome", biome.name());

        getIslandsConfig().set(islandId + ".protection.containers", false);
        getIslandsConfig().set(islandId + ".protection.doors", false);
        getIslandsConfig().set(islandId + ".protection.utility", false);

        plugin.saveIslandsConfig();

        return islandId;
    }

    @Nullable
    public String getIslandId(int x, int z) {
        for (String islandId : getIslandsConfig().getKeys(false)) {
            if (x / islandSpacing == getIslandsConfig().getInt(islandId + ".xIndex")) {
                if (z / islandSpacing == getIslandsConfig().getInt(islandId + ".zIndex")) {
                    return islandId;
                }
            }
        }

        return null;
    }

    @NotNull
    public List<String> getIslandIds(UUID uuid) {
        List<String> islands = new ArrayList<>();

        for (String islandId : getIslandsConfig().getKeys(false)) {
            String islandUUID = getIslandsConfig().getString(islandId + ".UUID");

            if (islandUUID != null && islandUUID.equals(uuid.toString()))
                islands.add(islandId);
        }

        return islands;
    }

    @NotNull
    public Map<String, Map<String, String>> getIslandsInfo(boolean publicOnly) {
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : getIslandsConfig().getKeys(false)) {
            boolean isPublic = getIslandsConfig().getBoolean(islandId + ".public");

            if (!publicOnly || isPublic) {
                String name = isPublic ? getIslandsConfig().getString(islandId + ".name") : islandId;
                String ownerUUID = getIslandsConfig().getString(islandId + ".UUID");

                Map<String, String> values = new HashMap<>();
                values.put("name", name);
                values.put("owner", ownerUUID);

                try {
                    String biome = getIslandsConfig().getString(islandId + ".biome");
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
    public Map<String, Map<String, String>> getIslandsInfo(String uuid) {
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : getIslandsConfig().getKeys(false)) {
            String ownerUUID = getIslandsConfig().getString(islandId + ".UUID");
            if (!uuid.equalsIgnoreCase(ownerUUID)) continue;

            String name = getIslandsConfig().getBoolean(islandId + ".public")
                    ? getIslandsConfig().getString(islandId + ".name")
                    : islandId;

            Map<String, String> values = new HashMap<>();
            values.put("name", name);

            try {
                String biome = getIslandsConfig().getString(islandId + ".biome");
                values.put("material", BiomeMaterials.valueOf(biome).name());
            } catch (Exception e) {
                values.put("material", BiomeMaterials.DEFAULT.name());
            }

            islands.put(islandId, values);
        }

        return islands;
    }

    @NotNull
    public Map<String, Integer> getPlayers() {
        Map<String, Integer> players = new HashMap<>();

        for (String islandId : getIslandsConfig().getKeys(false)) {
            String uuid = getIslandsConfig().getString(islandId + ".UUID");

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
    public String getIslandByName(String name) {
        for (String islandId : getIslandsConfig().getKeys(false)) {
            if (name.equalsIgnoreCase(getIslandsConfig().getString(islandId + ".name")) && getIslandsConfig().getBoolean(islandId + ".public")) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public String getHomeIsland(UUID uuid, int homeId) {
        List<String> allIslands = getIslandIds(uuid);

        for (String islandId : allIslands) {
            if (getIslandsConfig().getInt(islandId + ".home") == homeId) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public Location getIslandSpawn(String islandId) {
        if (getIslandsConfig().getKeys(false).contains(islandId)) {
            return new Location(
                    plugin.islandsWorld,
                    getIslandsConfig().getInt(islandId + ".spawnPoint.x"),
                    getIslandsConfig().getInt(islandId + ".y") + 100,
                    getIslandsConfig().getInt(islandId + ".spawnPoint.z")
            );
        } else {
            return null;
        }
    }

    @Nullable
    public String getSpawnIsland() {
        for (String islandId : getIslandsConfig().getKeys(false)) {
            if (getIslandsConfig().getBoolean(islandId + ".isSpawn")) {
                return islandId;
            }
        }

        return null;
    }

    public boolean isBlockInIslandSphere(int x, int y, int z) {
        int xIndex = x / islandSpacing;
        int zIndex = z / islandSpacing;
        int islandLowY = getIslandY(xIndex, zIndex);

        int islandSize = getIslandsConfig().getInt(posToIslandId(xIndex, zIndex)  + ".size");

        int relativeX = x - (xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);
        int relativeZ = z - (zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);
        int relativeY = y - islandLowY;

        return IslandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, islandSize);
    }

    @Nullable
    public String getBlockOwnerUUID(int x, int z) {
        int xIndex = x / islandSpacing;
        int zIndex = z / islandSpacing;

        int islandSize = getIslandsConfig().getInt(posToIslandId(xIndex, zIndex)  + ".size");

        int relativeX = x - (xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);
        int relativeZ = z - (zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);

        boolean isInside = IslandGeneration.isBlockInIslandCylinder(relativeX + 2, relativeZ + 2, islandSize + 4);

        if (!isInside) return null;

        return Optional.ofNullable(getIslandsConfig().getString(posToIslandId(xIndex, zIndex) + ".UUID")).orElse("Server");
    }

    public int getNewHomeId(UUID uuid) {
        List<String> ids = getIslandIds(uuid);
        List<Integer> homeIds = new ArrayList<>();

        for (String islandId : ids) {
            int homeNumber = getIslandsConfig().getInt(islandId + ".home");
            homeIds.add(homeNumber);
        }

        int home = getNumberOfIslands(uuid) + 1;

        for (int i = 1; i <= getNumberOfIslands(uuid) + 1; i++) {
            if (!homeIds.contains(i)) home = i;
        }

        return home;
    }

    // UTILS

    private int getIslandY(int xIndex, int zIndex) {
        return 10 + ((xIndex + zIndex) % 3) * verticalSpacing;
    }

    public int getNumberOfIslands(UUID uuid) {
        return getIslandIds(uuid).size();
    }

    @NotNull
    public TrustedPlayer getTrusted(String islandId, String uuid) {
        ConfigurationSection section = getIslandsConfig().getConfigurationSection(islandId + ".trusted." + uuid);

        TrustedPlayer trustedPlayer = new TrustedPlayer(UUID.fromString(uuid));

        if (section != null) {
            Set<String> keys = section.getKeys(false);

            if (section.getBoolean("generalTrust", false)) return trustedPlayer.setGeneralTrust(true);

            if (section.getBoolean("doorTrust", false)) trustedPlayer.setDoorTrust(true);
            if (section.getBoolean("containerTrust", false)) trustedPlayer.setContainerTrust(true);
        }

        return trustedPlayer;
    }

    @NotNull
    public TrustedPlayer getTrusted(int x, int y, String uuid) {
        return getTrusted(getIslandId(x, y), uuid);
    }

    String posToIslandId(int xIndex, int zIndex) {
        return xIndex + "x" + zIndex;
    }

    @NotNull
    public String getUUID(String islandId) {
        return Optional.ofNullable(getIslandsConfig().getString(islandId + ".UUID")).orElse("");
    }

    // MANAGMENT

    public void updateIsland(String islandId, int islandSize, int height, Biome biome) {
        int xIndex = getIslandsConfig().getInt(islandId + ".xIndex");
        int zIndex = getIslandsConfig().getInt(islandId + ".zIndex");

        int realX = xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;
        int realZ = zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;

        getIslandsConfig().set(islandId + ".x", realX);
        getIslandsConfig().set(islandId + ".z", realZ);

        getIslandsConfig().set(islandId + ".size", islandSize);
        getIslandsConfig().set(islandId + ".height", height);
        getIslandsConfig().set(islandId + ".biome", biome.name());

        plugin.saveIslandsConfig();
    }

    public void addTrusted(String islandId, String uuid) {
        if (!getIslandsConfig().contains(islandId + ".trusted." + uuid)) {
            getIslandsConfig().set(islandId + ".trusted." + uuid + ".generalTrust", true);

            plugin.saveIslandsConfig();
        }
    }

    public void removeTrusted(String islandId, String UUID) {
        List<String> trusted = getIslandsConfig().getStringList(islandId + ".trusted");
        trusted.remove(UUID);
        getIslandsConfig().set(islandId + ".trusted", trusted);
        plugin.saveIslandsConfig();
    }

    public void setSpawnPoint(String islandId, int x, int z) {
        getIslandsConfig().set(islandId + ".spawnPoint.x", x);
        getIslandsConfig().set(islandId + ".spawnPoint.z", z);

        plugin.saveIslandsConfig();
    }

    public void unnameIsland(String islandId) {
        int homeId = getIslandsConfig().getInt(islandId + ".home");

        getIslandsConfig().set(islandId + ".name", String.valueOf(homeId));
        getIslandsConfig().set(islandId + ".public", false);

        plugin.saveIslandsConfig();
    }

    public void nameIsland(String islandId, String name){
            getIslandsConfig().set(islandId + ".name", name);
            getIslandsConfig().set(islandId + ".public", true);

            plugin.saveIslandsConfig();
    }

    public void giveIsland(String islandId, OfflinePlayer player) {
        getIslandsConfig().set(islandId + ".home", getNewHomeId(player.getUniqueId()));
        getIslandsConfig().set(islandId + ".UUID", player.getUniqueId().toString());

        plugin.saveIslandsConfig();
    }

    public void giveIsland(String islandId) {
        getIslandsConfig().set(islandId + ".home", -1);
        getIslandsConfig().set(islandId + ".UUID", null);

        plugin.saveIslandsConfig();
    }

    public void deleteIsland(String islandId) {
        getIslandsConfig().set(islandId, null);

        plugin.saveIslandsConfig();
    }

    public boolean setSpawnIsland(String islandId) {
        if (getIslandsConfig().getConfigurationSection(islandId) == null) return false;

        if (getIslandsConfig().getBoolean(islandId + ".isSpawn")) {
            getIslandsConfig().set(islandId + ".isSpawn", false);
            return true;
        }

        for (String island : getIslandsConfig().getKeys(false)) {
            if (getIslandsConfig().getBoolean(island + ".isSpawn")) {
                getIslandsConfig().set(island + ".isSpawn", false);
            }
        }

        getIslandsConfig().set(islandId + ".isSpawn", true);
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
    }
}
