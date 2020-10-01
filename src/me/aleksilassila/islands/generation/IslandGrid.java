package me.aleksilassila.islands.generation;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandGrid {
    private Islands islands;

    private int islandsInARow;
    public int islandSpacing;

    public IslandGrid(Islands instance) {
        this.islands = instance;

        this.islandsInARow = instance.plugin.getConfig().getInt("generation.islandsInARow");
        this.islandSpacing = instance.plugin.getConfig().getInt("generation.islandSpacing");
    }

    private FileConfiguration getIslandsConfig() {
        return islands.plugin.getIslandsConfig();
    }

    public void unnameIsland(String islandId) {
        int homeId = islands.plugin.getIslandsConfig().getInt("islands." + islandId + ".home");

        getIslandsConfig().set("islands." + islandId + ".name", String.valueOf(homeId));
        getIslandsConfig().set("islands." + islandId + ".public", 0);

        islands.plugin.saveIslandsConfig();
    }

    public void nameIsland(String islandId, String name){
            getIslandsConfig().set("islands." + islandId + ".name", name);
            getIslandsConfig().set("islands." + islandId + ".public", 1);

            islands.plugin.saveIslandsConfig();
    }

    public void giveIsland(String islandId, Player player) {
        getIslandsConfig().set("islands." + islandId + ".home", getNewHomeId(player.getUniqueId()));
        getIslandsConfig().set("islands." + islandId + ".UUID", player.getUniqueId().toString());
        islands.plugin.saveIslandsConfig();
    }

    public void giveIsland(String islandId) {
        getIslandsConfig().set("islands." + islandId + ".home", -1);
        getIslandsConfig().set("islands." + islandId + ".UUID", null);
        islands.plugin.saveIslandsConfig();
    }

    public void deleteIsland(String islandId) {
        getIslandsConfig().set("islands." + islandId, null);
        islands.plugin.saveIslandsConfig();
    }

    @Nullable
    public String getIslandId(int x, int z) {
        for (String islandId : getIslandsConfig().getConfigurationSection("islands").getKeys(false)) {
            if (x / islandSpacing == getIslandsConfig().getInt("islands." + islandId + ".xIndex")) {
                if (z / islandSpacing == getIslandsConfig().getInt("islands." + islandId + ".zIndex")) {
                    return islandId;
                }
            }
        }

        return null;
    }

    @NotNull
    public List<String> getAllIslandIds(UUID uuid) {
        List<String> islands = new ArrayList<>();

        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");
        if (section == null) { return islands; }

        Set<String> allIslands = section.getKeys(false);

        for (String islandId : allIslands) {
            try {
                if (getIslandsConfig().getString("islands." + islandId + ".UUID").equals(uuid.toString())) {
                    islands.add(islandId);
                }
            } catch (NullPointerException e) { }
        }

        return islands;
    }

    @Nullable
    public String getPublicIsland(String name) {
        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");
        if (section == null) { return null; }

        for (String islandId : section.getKeys(false)) {
            if (getIslandsConfig().getString("islands." + islandId + ".name").equalsIgnoreCase(name) && getIslandsConfig().getInt("islands." + islandId + ".public") == 1) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public String getHomeIsland(UUID uuid, int homeId) {
        List<String> allIslands = getAllIslandIds(uuid);

        for (String islandId : allIslands) {
            if (getIslandsConfig().getInt("islands." + islandId + ".home") == homeId) {
                return islandId;
            }
        }

        return null;
    }

    @Nullable
    public Location getIslandSpawn(String islandId) {
        if (getIslandsConfig().getConfigurationSection("islands."+islandId) != null) {
            return new Location(
                    islands.plugin.islandsWorld,
                    getIslandsConfig().getInt("islands." + islandId + ".spawnPoint.x"),
                    getIslandsConfig().getInt("islands." + islandId + ".y") + 100,
                    getIslandsConfig().getInt("islands." + islandId + ".spawnPoint.z")
            );
        } else {
            return null;
        }
    }

    @Nullable
    public String getSpawnIsland() {
        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");

        if (section == null) return null;

        Set<String> islands = section.getKeys(false);

        for (String islandId : islands) {
            if (getIslandsConfig().getBoolean("islands." + islandId + ".isSpawn")) {
                return islandId;
            }
        }

        return null;
    }

    private int getIslandY(int xIndex, int zIndex) {
        int islandIndex = (xIndex * islandsInARow + zIndex);
        return 10 + ((islandIndex + xIndex) % 3) * islands.plugin.getConfig().getInt("generation.islandVerticalSpacing");
    }

    @NotNull
    private String addIslandToConfig(int xIndex, int zIndex, int islandSize, UUID uuid, String name) {
        int realX = xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;
        int realY = getIslandY(xIndex, zIndex);
        int realZ = zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;

        int home = getNewHomeId(uuid);

        String islandId = xIndex + "x" + zIndex;

        getIslandsConfig().set("islands." + islandId + ".xIndex", xIndex);
        getIslandsConfig().set("islands." + islandId + ".zIndex", zIndex);

        getIslandsConfig().set("islands." + islandId + ".x", realX);
        getIslandsConfig().set("islands." + islandId + ".y", realY);
        getIslandsConfig().set("islands." + islandId + ".z", realZ);

        getIslandsConfig().set("islands." + islandId + ".spawnPoint.x", realX + islandSize / 2);
        getIslandsConfig().set("islands." + islandId + ".spawnPoint.z", realZ + islandSize / 2);

        getIslandsConfig().set("islands." + islandId + ".UUID", uuid.toString());
        getIslandsConfig().set("islands." + islandId + ".name", name);
        getIslandsConfig().set("islands." + islandId + ".home", home);
        getIslandsConfig().set("islands." + islandId + ".size", islandSize);
        getIslandsConfig().set("islands." + islandId + ".public", 0);

        islands.plugin.saveIslandsConfig();

        return islandId;
    }

    public int getNumberOfIslands(UUID uuid) {
        return getAllIslandIds(uuid).size();
    }

    public String createIsland(UUID uuid, int islandSize) {
        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");

        if (section == null) {
            return addIslandToConfig(0, 1, islandSize, uuid, "1");
        }

        Set<String> islands = section.getKeys(false);

        for (int x = 0; x > -1; x++) {
            loop:
            for (int z = 1; z <= 3; z++) {
                for (String islandId : islands) {
                    if (getIslandsConfig().getInt("islands." + islandId + ".xIndex") == x && getIslandsConfig().getInt("islands." + islandId + ".zIndex") == z) {
                        continue loop;
                    }
                }

                return addIslandToConfig(x, z, islandSize, uuid, String.valueOf(getNewHomeId(uuid)));
            }
        }

        return null;
    }

    public boolean isBlockInIslandSphere(int x, int y, int z) {
        int xIndex = x / islandSpacing;
        int zIndex = z / islandSpacing;
        int islandLowY = getIslandY(xIndex, zIndex);

        int islandSize = getIslandsConfig().getInt("islands." + xIndex + "x" + zIndex  + ".size");


        int relativeX = x - (xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);
        int relativeZ = z - (zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);
        int relativeY = y - islandLowY;

        return islands.islandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, islandSize);
    }

    @Nullable
    public String getBlockOwnerUUID(int x, int z) {
        int xIndex = x / islandSpacing;
        int zIndex = z / islandSpacing;

        int islandSize = getIslandsConfig().getInt("islands." + xIndex + "x" + zIndex  + ".size");

        int relativeX = x - (xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);
        int relativeZ = z - (zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2);

        boolean isInside = islands.islandGeneration.isBlockInIslandCylinder(relativeX + 2, relativeZ + 2, islandSize + 4);

        if (!isInside) return null;

        return getIslandsConfig().getString("islands." + xIndex + "x" + zIndex + ".UUID");
    }

    public void updateIslandSize(String islandId, int islandSize) {
        int xIndex = getIslandsConfig().getInt("islands." + islandId + ".xIndex");
        int zIndex = getIslandsConfig().getInt("islands." + islandId + ".zIndex");

        int realX = xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;
        int realZ = zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;

        getIslandsConfig().set("islands." + islandId + ".x", realX);
        getIslandsConfig().set("islands." + islandId + ".z", realZ);

        getIslandsConfig().set("islands." + islandId + ".size", islandSize);

        islands.plugin.saveIslandsConfig();
    }

    public void addTrusted(String islandId, String UUID) {
        List<String> trusted = getIslandsConfig().getStringList("islands." + islandId + ".trusted");
        trusted.add(UUID);
        getIslandsConfig().set("islands." + islandId + ".trusted", trusted);

        islands.plugin.saveIslandsConfig();
    }

    public void removeTrusted(String islandId, String UUID) {
        List<String> trusted = getIslandsConfig().getStringList("islands." + islandId + ".trusted");
        trusted.remove(UUID);
        getIslandsConfig().set("islands." + islandId + ".trusted", trusted);
        islands.plugin.saveIslandsConfig();
    }

    @NotNull
    public List<String> getTrusted(String islandId) {
        return getIslandsConfig().getStringList("islands." + islandId + ".trusted");
    }

    public void setSpawnPoint(String islandId, int x, int y) {
        getIslandsConfig().set("islands." + islandId + ".spawnPoint.x", x);
        getIslandsConfig().set("islands." + islandId + ".spawnPoint.y", y);
    }

    public int getNewHomeId(UUID uuid) {
        List<String> ids = getAllIslandIds(uuid);
        List<Integer> homeIds = new ArrayList<>();

        for (String islandId : ids) {
            int homeNumber = getIslandsConfig().getInt("islands." + islandId + ".home");
            homeIds.add(homeNumber);
        }

        int home = getNumberOfIslands(uuid) + 1;

        for (int i = 1; i <= getNumberOfIslands(uuid) + 1; i++) {
            if (!homeIds.contains(i)) home = i;
        }

        return home;
    }

    // deleteIsland
    // moveIsland
}
