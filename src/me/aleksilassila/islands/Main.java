package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.generation.EmptyWorldGenerator;
import org.bukkit.*;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public World islandsWorld;
    public World islandsSourceWorld;

    public Islands islands;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        islandsWorld = createIslandsWorldIfNecessary();
        islandsSourceWorld = createIslandsSourceWorldIfNecessary();

        islands = new Islands(islandsWorld, islandsSourceWorld, this);

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

    World createIslandsSourceWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands source world...");

        WorldCreator wc = new WorldCreator("islandsSource");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }

    World createIslandsWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands world...");

        WorldCreator wc = new WorldCreator("islands");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generator(new EmptyWorldGenerator());

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }
}
