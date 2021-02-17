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
        boolean announce = false;

        for (String islandId : islandsConfig.getKeys(false)) {
            if (islandsConfig.contains(islandId + ".protect")) {
                islandsConfig.set(islandId + ".protect", null);
                announce = true;
            }
        }
        if (announce)
            Islands.instance.getLogger().warning("REMOVING DEPRECATED ISLAND PROTECTION CONFIGURATION FROM islands.yml...");

        IslandsConfig.saveIslandsConfig();
    }

    private void migrateTrustedPlayers() {
        boolean announce = false;

        for (String islandId : islandsConfig.getKeys(false)) {
            if (islandsConfig.get(islandId + ".trusted") instanceof List) {
                islandsConfig.set(islandId + ".trusted.", null);
                announce = true;
            }
        }

        if (announce)
            Islands.instance.getLogger().warning("REMOVING DEPRECATED ISLAND TRUST CONFIGURATION FROM islands.yml...");

        IslandsConfig.saveIslandsConfig();
    }
}
