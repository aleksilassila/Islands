package me.aleksilassila.islands;

import me.aleksilassila.islands.commands.IslandCommands;
import org.bukkit.Bukkit;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    public World islandsWorld;
    public IslandGeneration islandGen;

    @Override
    public void onEnable() {
        islandGen = new IslandGeneration();
        islandsWorld = islandGen.islandsWorld;

        new IslandCommands(this);

        getLogger().info("Islands enabled!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
