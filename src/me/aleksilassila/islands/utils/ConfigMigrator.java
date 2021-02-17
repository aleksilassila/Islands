package me.aleksilassila.islands.utils;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigMigrator {
    private final FileConfiguration islandsConfig;

    public ConfigMigrator() {
        this.islandsConfig = IslandsConfig.getConfig();

        migrateTrustedPlayers();
        migrateIslandProtection();
    }

    private void migrateIslandProtection() {
        Islands.instance.getLogger().warning("REMOVING DEPRECATED ISLAND PROTECTION CONFIGURATION FROM islands.yml...");

        for (String islandId : islandsConfig.getKeys(false)) {
            if (islandsConfig.contains(islandId + ".protect")) islandsConfig.set(islandId + ".protect", null);
        }

        IslandsConfig.saveIslandsConfig();
    }

    private void migrateTrustedPlayers() {
        Islands.instance.getLogger().warning("REMOVING DEPRECATED ISLAND TRUST CONFIGURATION FROM islands.yml...");

        for (String islandId : islandsConfig.getKeys(false)) {
            if (islandsConfig.get(islandId + ".trusted") instanceof List)
                islandsConfig.set(islandId + ".trusted.", null);
        }

        IslandsConfig.saveIslandsConfig();
    }
}
