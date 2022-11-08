package me.aleksilassila.islands;

import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.plugins.Economy;
import me.aleksilassila.islands.plugins.GriefPrevention;
import me.aleksilassila.islands.plugins.VaultPermissions;
import me.aleksilassila.islands.plugins.WorldEdit;
import me.aleksilassila.islands.utils.ConfirmItem;
import me.aleksilassila.islands.world.IslandsWorld;
import me.aleksilassila.islands.world.SourceWorld;
import me.aleksilassila.islands.world.WildernessWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Islands {
    public final Plugin plugin;

    public IslandsWorld islandsWorld;
    public SourceWorld sourceWorld;
    public WildernessWorld wildernessWorld;

    public final IslandsConfig islandsConfig;
    public final Config config;
    public final IslandGeneration generator;

    // Plugins
    public Economy economy;
    public VaultPermissions vaultPermissions;
    public WorldEdit worldEdit;
    public GriefPrevention griefPrevention;

    public Map<String, Long> teleportCooldowns = new HashMap<>();
    public Set<Player> playersWithNoFall = new HashSet<>();
    public HashMap<String, ConfirmItem> confirmations = new HashMap<>();


    public World getIslandsWorld() {
        return islandsWorld.getWorld();
    }

    public World getSourceWorld() {
        return sourceWorld.getWorld();
    }

    public World getWildernessWorld() {
        return wildernessWorld.getWorld();
    }

    public Islands(Plugin plugin) {
        this.plugin = plugin;
        this.config = new Config(this);

        this.islandsWorld = new IslandsWorld(this);
        this.sourceWorld = new SourceWorld(this);
        this.wildernessWorld = new WildernessWorld(this);

        this.islandsConfig = new IslandsConfig(this);
        this.generator = new IslandGeneration(this);

        if (plugin.getServer().getPluginManager().getPlugin("vault") != null) {
            this.economy = new Economy(this);
            this.vaultPermissions = new VaultPermissions(this);
        }
        if (plugin.getServer().getPluginManager().getPlugin("griefprevention") != null) {
            this.griefPrevention = new GriefPrevention(this);
        }
        if (plugin.getServer().getPluginManager().getPlugin("worldedit") != null) {
            this.worldEdit = new WorldEdit(this);
        }
    }
}
