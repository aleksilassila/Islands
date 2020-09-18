package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommands implements CommandExecutor {
    private Islands plugin;

    public IslandCommands(Islands plugin) {
        this.plugin = plugin;

        plugin.getCommand("goislands").setExecutor(this);
        plugin.getCommand("goback").setExecutor(this);
        plugin.getCommand("findisland").setExecutor(this);
        plugin.getCommand("getbiome").setExecutor(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (label.equalsIgnoreCase("goislands")) {
                player.teleport(new Location(plugin.islandsWorld, player.getLocation().getBlockX(), 140, player.getLocation().getBlockZ()));
            } else if (label.equalsIgnoreCase("goback")) {
                player.teleport(new Location(plugin.getServer().getWorlds().get(0), player.getLocation().getBlockX(), 140, player.getLocation().getBlockZ()));
            } else if (label.equalsIgnoreCase("findisland")) {
                int[] position = plugin.islandGen.getIslandSourceLocation(Biome.BAMBOO_JUNGLE, 100);
                player.sendMessage("Found suitable BAMBOOJUNGLE island. x:" + position[0] + ", y: " + position[1]);
            } else if (label.equalsIgnoreCase("getbiome")) {
                player.sendMessage("Your biome in islands: " + plugin.islandsWorld.getBiome(player.getLocation().getBlockX(), 180, player.getLocation().getBlockZ()));
            }

            return true;
        }

        return false;
    }
}
