package me.aleksilassila.islands.generation;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandGrid {
    private Islands islands;

    private int islandsInARow;
    private int islandSpacing;

    public IslandGrid(Islands instance) {
        this.islands = instance;

        this.islandsInARow = instance.plugin.getConfig().getInt("generation.islandsInARow");
        this.islandSpacing = instance.plugin.getConfig().getInt("generation.islandSpacing");
    }

    public void unnameIsland(String islandId) {
        String homeId = islands.plugin.getIslandsConfig().getString("islands." + islandId + ".home");

        getIslandsConfig().set("islands." + islandId + ".name", homeId);
        getIslandsConfig().set("islands." + islandId + ".public", 0);

        islands.plugin.saveIslandsConfig();
    }

    public void giveIsland(String islandId, Player player) {
        getIslandsConfig().set("islands." + islandId + ".home", String.valueOf(getNumberOfIslands(player.getUniqueId()) + 1));
        getIslandsConfig().set("islands." + islandId + ".UUID", player.getUniqueId().toString());
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

    private FileConfiguration getIslandsConfig() {
        return islands.plugin.getIslandsConfig();
    }

    @NotNull
    public List<String> getAllIslandIds(UUID uuid) {
        List<String> islands = new ArrayList<>();

        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");
        if (section == null) { return islands; }

        Set<String> allIslands = section.getKeys(false);


        for (String islandId : allIslands) {
            if (getIslandsConfig().getString("islands." + islandId + ".UUID").equals(uuid.toString())) {
                islands.add(islandId);
            }
        }

        return islands;
    }

    @Nullable
    public String getPrivateIsland(UUID uuid, String name) {
        List<String> allIslands = getAllIslandIds(uuid);

        for (String islandId : allIslands) {
            if (getIslandsConfig().getString("islands." + islandId + ".name").equals(name)) {
                return islandId;
            }
        }

        return null;
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

    private int getIslandY(int xIndex, int zIndex) {
        int islandIndex = (xIndex * islandsInARow + zIndex);
        return 10 + ((islandIndex + xIndex) % 3) * islands.plugin.getConfig().getInt("generation.islandVerticalSpacing");
    }

    @NotNull
    private String addIslandToConfig(int xIndex, int zIndex, int islandSize, UUID uuid, String name) {
        int realX = xIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;
        int realY = getIslandY(xIndex, zIndex);
        int realZ = zIndex * islandSpacing + islandSpacing / 2 - islandSize / 2;

        String home = String.valueOf(getNumberOfIslands(uuid) + 1);

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

                return addIslandToConfig(x, z, islandSize, uuid, String.valueOf(getNumberOfIslands(uuid) + 1));
            }
        }

        return null;
    }

    public void nameIsland(String islandId, String name){
            getIslandsConfig().set("islands." + islandId + ".name", name);
            getIslandsConfig().set("islands." + islandId + ".public", 1);

            islands.plugin.saveIslandsConfig();
    }

    @Nullable
    public String getHomeIsland(UUID uuid, String home) {
        List<String> allIslands = getAllIslandIds(uuid);

        for (String islandId : allIslands) {
            if (getIslandsConfig().getString("islands." + islandId + ".home").equals(home)) {
                return islandId;
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

        boolean isInside = islands.islandGeneration.isBlockInIslandCircle(relativeX, relativeZ, islandSize);

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
        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands." + islandId + ".trusted");

        if (section == null) {
            return new ArrayList<>();
        }

        return getIslandsConfig().getStringList("islands." + islandId + ".trusted");
    }

    // deleteIsland
    // moveIsland
}
