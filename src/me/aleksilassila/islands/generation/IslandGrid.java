package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandGrid {
    private Islands islands;

    private int islandsInARow;
    private int islandSpacing;

    public IslandGrid(Islands instance) {
        this.islands = instance;

        this.islandsInARow = instance.plugin.getConfig().getInt("generation.islandsInARow");
        this.islandSpacing = instance.plugin.getConfig().getInt("generation.islandSpacing");

        Bukkit.broadcastMessage("INT IS " + islandSpacing);
    }

    public static class IslandGridException extends java.lang.Exception {
        public IslandGridException(String message) {
            super(message);
        }
    }

    private FileConfiguration getIslandsConfig() {
        return islands.plugin.getIslandsConfig();
    }

    public String getFirstIslandId(UUID uuid) throws IslandGridException {
        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");

        if (section == null) {
            throw new IslandGridException("No islands found.");
        }

        Set<String> islands = section.getKeys(false);

        for (String islandId : islands) {
            if (getIslandsConfig().getString("islands." + islandId + ".UUID").equals(uuid.toString())) {
                return islandId;
            }
        }

        throw new IslandGridException("Island not found");
    }

    public List<String> getAllIslandIds(UUID uuid) throws IslandGridException {
        List<String> islands = new ArrayList<>();

        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands");

        if (section == null) {
            throw new IslandGridException("No islands found.");
        }

        Set<String> allIslands = section.getKeys(false);


        for (String islandId : allIslands) {
            if (getIslandsConfig().getString("islands." + islandId + ".UUID").equals(uuid.toString())) {
                islands.add(islandId);
            }
        }

        return islands;
    }

    public String getPrivateIsland(UUID uuid, String name) throws IslandGridException {
        List<String> allIslands = getAllIslandIds(uuid);

        if (allIslands == null) {
            throw new IslandGridException("No islands found.");
        }

        for (String islandId : allIslands) {
            if (getIslandsConfig().getString("islands." + islandId + ".name").equals(name)) {
                return islandId;
            }
        }

        throw new IslandGridException("No island matched given name.");
    }

    public Location getIslandSpawn(String islandId) throws IslandGridException {
        if (getIslandsConfig().getConfigurationSection("islands."+islandId) != null) {
            return new Location(
                    islands.plugin.islandsWorld,
                    getIslandsConfig().getInt("islands." + islandId + ".spawnPoint.x"),
                    getIslandsConfig().getInt("islands." + islandId + ".y") + 100,
                    getIslandsConfig().getInt("islands." + islandId + ".spawnPoint.z")
            );
        } else {
            throw new IslandGridException("Island not found");
        }
    }

    private int getIslandY(int xIndex, int zIndex) {
        int islandIndex = (xIndex * islandsInARow + zIndex);
        return 20 + ((islandIndex + xIndex) % 3) * 70;
    }

    private String addIslandToConfig(int xIndex, int zIndex, int islandSize, UUID uuid, String name){
        int realX = xIndex * islandSpacing;
        int realY = getIslandY(xIndex, zIndex);
        int realZ = zIndex * islandSpacing;

        String islandId = String.valueOf((int) Math.floor(Math.random() * 10000000));

        getIslandsConfig().set("islands."+islandId+".xIndex", xIndex);
        getIslandsConfig().set("islands."+islandId+".zIndex", zIndex);

        getIslandsConfig().set("islands."+islandId+".x", realX);
        getIslandsConfig().set("islands."+islandId+".y", realY);
        getIslandsConfig().set("islands."+islandId+".z", realZ);

        getIslandsConfig().set("islands."+islandId+".spawnPoint.x", realX + islandSize / 2);
        getIslandsConfig().set("islands."+islandId+".spawnPoint.z", realZ + islandSize / 2);

        getIslandsConfig().set("islands."+islandId+".UUID", uuid.toString());
        getIslandsConfig().set("islands."+islandId+".name", name);
        getIslandsConfig().set("islands."+islandId+".home", String.valueOf(getNumberOfIslands(uuid) + 1));
        getIslandsConfig().set("islands."+islandId+".size", islandSize);

        islands.plugin.saveIslandsConfig();

        return islandId;
    }

    private int getNumberOfIslands(UUID uuid) {
        int numberOfPreviousIslands;

        try {
            numberOfPreviousIslands = getAllIslandIds(uuid).size();
        } catch (IslandGridException e) {
            numberOfPreviousIslands = 0;
        }

        return numberOfPreviousIslands;
    }

    public String createIsland(UUID uuid, int islandSize) throws IslandGridException {
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


        // Nothing fancy so far
//        for (int layer = 1; layer == -1; layer++) {
//            for (int index = 0; index == -1; index++) {
//                int nOfPositions = (layer - 1) * 2 + 1;
//            }
//        }

        throw new IslandGridException("Error creating island");
    }

    public void nameIsland(UUID uuid, String name) throws IslandGridException {
        if (getIslandsConfig().getConfigurationSection("islands." + name) != null) {
            return;
        }
        try {
            getIslandsConfig().set("islands."+ getPrivateIsland(uuid, name) + ".name", name);
            islands.plugin.saveIslandsConfig();

        } catch (IslandGridException e) {
            throw new IslandGridException(e.getMessage());
        }

        return;
    }

    public void setIslandOwner(UUID newUuid, String islandId) throws IslandGridException {
        ConfigurationSection section = getIslandsConfig().getConfigurationSection("islands." + islandId);

        if (section == null) {
            throw new IslandGridException("Island not found");
        }

        getIslandsConfig().set("islands." + islandId + ".UUID", newUuid.toString());
    }

    public String getHomeIsland(UUID uuid, String home) throws IslandGridException {
        List<String> allIslands = getAllIslandIds(uuid);

        if (allIslands == null) {
            throw new IslandGridException("No islands found.");
        }

        for (String islandId : allIslands) {
            if (getIslandsConfig().getString("islands." + islandId + ".home").equals(home)) {
                return islandId;
            }
        }

        throw new IslandGridException("No island matched given name.");
    }

    public boolean isBlockInIslandSphere(int x, int y, int z) {
        int xIndex = x / islandSpacing;
        int zIndex = z / islandSpacing;
        int islandLowY = getIslandY(xIndex, zIndex);

        int relativeX = x - xIndex * islandSpacing;
        int relativeZ = z - zIndex * islandSpacing;
        int relativeY = y - islandLowY;

        int islandSize = 64; // CHANGE THIS

        return islands.islandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, islandSize);
    }

    // deleteIsland
    // moveIsland
}
