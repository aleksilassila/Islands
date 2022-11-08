package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.UpdateChecker;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class Plugin extends JavaPlugin {
    public static Plugin instance;
    public static Islands islands;

    public Permission perms = null;
    public Economy econ = null;
    public WorldEditPlugin worldEdit = null;

    public Map<Integer, Double> islandPrices;

    public Map<String, Integer> definedIslandSizes;

    @Override
    public void onEnable() {
        instance = this;
        islands = new Islands(this);

        new UpdateChecker(this, 84303).checkForUpdates();

        if (new File(getDataFolder() + "/config.yml").exists()) {
            if (!islands.config.validateConfig()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } else saveDefaultConfig();

        // ISLANDS
        Messages.init();

        int pluginId = 8974;
        new Metrics(this, pluginId);

        getLogger().info("Islands enabled!");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::initialise);

        // Save island configuration every 5 minutes
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, islands.islandsConfig::updateEntries, 20 * 60 * 5, 20 * 60 * 5);
    }

    // This will be run when all the plugins are loaded.
    public void initialise() {
        getLogger().info("Initialising commands and configuration");

        // Init islands config
//        IslandsConfig.getConfig();

        new IslandCommands(islands);
        new Listeners(this);
    }

    @Override
    public void onDisable() {
        islands.islandsConfig.updateEntries();
        super.onDisable();
    }

    @NotNull
    public int parseIslandSize(String size) {
        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedSize.equalsIgnoreCase(size)) return definedIslandSizes.get(definedSize);
        }

        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return definedIslandSizes.containsKey("NORMAL") ? definedIslandSizes.get("NORMAL") : definedIslandSizes.get(definedIslandSizes.keySet().iterator().next());
        }
    }

    @Nullable
    public String parseIslandSize(int size) {
        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) == size) return definedSize;
        }

        return null;
    }

    @NotNull
    public int getSmallestIslandSize() {
        int smallestSize = islands.config.islandSpacing;

        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) < smallestSize)
                smallestSize = definedIslandSizes.get(definedSize);
        }

        return smallestSize;
    }

    @NotNull
    public String getCreatePermission(int islandSize) {
        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) == islandSize)
                return Permissions.command.create + "." + definedSize;
        }

        return Permissions.command.createCustom;
    }
}
