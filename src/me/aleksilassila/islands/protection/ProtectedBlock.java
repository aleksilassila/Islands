package me.aleksilassila.islands.protection;

import me.aleksilassila.islands.IslandsConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class ProtectedBlock {
    private final String islandId;
    private final String ownerUUID;

    private final FileConfiguration islandsConfig;

    public ProtectedBlock(String islandId) {
        this.islandsConfig = IslandsConfig.getConfig();
        this.islandId = islandId;
        this.ownerUUID = islandsConfig.getString(islandId + ".UUID", null);
    }

    public ProtectedBlock(int x, int z) {
        this(IslandsConfig.getIslandId(x, z));
    }

    public boolean canDoAnything(String uuid) {
        if (uuid.equalsIgnoreCase(ownerUUID)) return true;
        if (!islandsConfig.getBoolean(islandId + ".protect.building", true))
            return true;

        return islandsConfig.getBoolean(islandId + ".trusted." + uuid + ".build", false);
    }

    public boolean canOpenContainers(String uuid) {
        if (!islandsConfig.getBoolean(islandId + ".protect.containers", true))
            return true;

        return canDoAnything(uuid)
                || islandsConfig.getBoolean(islandId + ".trusted." + uuid + ".accessContainers", false);
    }

    public boolean canUseDoors(String uuid) {
        if (!islandsConfig.getBoolean(islandId + ".protect.doors", true))
            return true;

        return canDoAnything(uuid)
                || islandsConfig.getBoolean(islandId + ".trusted." + uuid + ".accessDoors", false);
    }

    public boolean canUseUtility(String uuid) {
        if (!islandsConfig.getBoolean(islandId + ".protect.utility", true))
            return true;

        return canDoAnything(uuid)
                || islandsConfig.getBoolean(islandId + ".trusted." + uuid + ".accessUtility", false);
    }
}
