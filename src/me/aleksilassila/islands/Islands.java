package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.commands.IslandCommands;
import org.bukkit.Bukkit;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    public World islandsWorld;
    public World islandsSourceWorld;
    public IslandGeneration islandGen;

    @Override
    public void onEnable() {
        islandGen = new IslandGeneration();
        islandsWorld = islandGen.islandsWorld;
        islandsSourceWorld = islandGen.islandsSourceWorld;

        new IslandCommands(this);

        getLogger().info("Islands enabled!");
    }

    public static WorldEditPlugin getWorldEdit() {
        return (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
