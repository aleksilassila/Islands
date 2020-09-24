package me.aleksilassila.islands.commands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.List;

public class IslandManagmentCommands extends ChatUtils implements CommandExecutor {
    private final Main plugin;
    private final IslandGrid grid;

    public IslandManagmentCommands(Main plugin) {
        this.plugin = plugin;
        this.grid = plugin.islands.grid;

        plugin.getCommand("go").setExecutor(this);
        plugin.getCommand("island").setExecutor(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission(Permissions.island.island)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }

            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("create")) {
                    createIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("regenerate")) {
                    regenerateIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("delete")) {
                    deleteIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("give")) {
                    giveIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("name")) {
                    nameIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("unname")) {
                    unnameIsland(player, args);

                    return true;
                }
            }

            sendHelp(player);
        }

        return true;
    }

    private void deleteIsland(Player player, String[] args) {
        if (!player.hasPermission(Permissions.island.delete)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.Bypass.delete)) {
            grid.deleteIsland(islandId);

            player.sendMessage(Messages.success.DELETED);
        } else {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(success("Available /island subcommands:"));

        player.sendMessage(Messages.help.CREATE);
        player.sendMessage(Messages.help.REGENERATE);
        player.sendMessage(Messages.help.DELETE);
        player.sendMessage(Messages.help.NAME);
        player.sendMessage(Messages.help.UNNAME);
        player.sendMessage(Messages.help.GIVE);
    }

    private void giveIsland(Player player, String[] args) {
        if (!player.hasPermission(Permissions.island.give)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }


        if (args.length != 2) {
            player.sendMessage(Messages.help.GIVE);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            player.sendMessage(Messages.error.NO_PLAYER_FOUND);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.Bypass.give)) {
            if (plugin.getIslandsConfig().getInt("islands." + islandId + ".public") == 1) {
                grid.giveIsland(islandId, targetPlayer);

                player.sendMessage(Messages.success.OWNER_CHANGED(args[1]));
                targetPlayer.sendMessage(Messages.success.ISLAND_RECEIVED(targetPlayer.getName(), args[1]));
            } else {
                player.sendMessage(Messages.error.NOT_PUBLIC);
            }
        } else {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }
    }

    private void unnameIsland(Player player, String[] args) {
        if (!player.hasPermission(Permissions.island.unname)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length != 1) {
            player.sendMessage(Messages.help.UNNAME);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.Bypass.unname)) {
            grid.unnameIsland(islandId);

            player.sendMessage(Messages.success.UNNAMED);
        } else {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }
    }

    private void nameIsland(Player player, String[] args) {
        if (!player.hasPermission(Permissions.island.name)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length != 2) {
            player.sendMessage(Messages.help.NAME);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.Bypass.name)) {
            if (grid.getPublicIsland(args[1]) != null) {
                player.sendMessage(Messages.error.NAME_TAKEN);
                return;
            }

            if (plugin.getConfig().getStringList("illegalIslandNames").contains(args[1])) {
                player.sendMessage(Messages.error.NAME_BLOCKED);
                return;
            }

            grid.nameIsland(islandId, args[1]);

            player.sendMessage(Messages.success.NAME_CHANGED(args[1]));
        } else {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }
    }

    @Nullable
    private Biome getTargetBiome(String biome) {
         Biome targetBiome = null;

         for (Biome b : Biome.values()) {
             if (b.name().equalsIgnoreCase(biome)) {
                 targetBiome = b;
             }
         }

         return targetBiome;
    }

    private void createIsland(Player player, String[] args) {
        if (!player.hasPermission(Permissions.island.create)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        Islands.IslandSize islandSize = args.length == 3 ? parseIslandSize(args[2]) : Islands.IslandSize.NORMAL;

        String permissionRequired;

        switch (islandSize) {
            case BIG:
                permissionRequired = Permissions.island.createBig;
                break;
            case SMALL:
                permissionRequired = Permissions.island.createSmall;
                break;
            case NORMAL:
            default:
                permissionRequired = Permissions.island.createNormal;
                break;
        }

        if (!player.hasPermission(permissionRequired)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

        if (args.length < 2) {
            player.sendMessage(Messages.help.CREATE);

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD
                            +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
                }
            }

            return;
        }

        int previousIslands = grid.getAllIslandIds(player.getUniqueId()).size();

        if (previousIslands == plugin.getConfig().getInt("islandsLimit") && !player.hasPermission(Permissions.Bypass.create)) {
            player.sendMessage(Messages.error.ISLAND_LIMIT);
        }

        Biome targetBiome = getTargetBiome(args[1]);

        if (targetBiome == null) {
            player.sendMessage(Messages.error.NO_BIOME_FOUND);
            return;
        }

        if (!availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);
            return;
        }

        String islandId = null;

        try {
            islandId = plugin.islands.createNewIsland(targetBiome, islandSize, player);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);

            return;
        }

        if (islandId == null) {
            player.sendMessage(Messages.error.ONGOING_QUEUE_EVENT);
            return;
        }

        player.sendTitle(Messages.success.ISLAND_GEN_TITLE, Messages.success.ISLAND_GEN_SUBTITLE, 10, 20 * 7, 10);
    }

    @NotNull
    private Islands.IslandSize parseIslandSize(String size) {
        for (Islands.IslandSize targetSize : Islands.IslandSize.values()) {
            if (targetSize.name().equalsIgnoreCase(size)) {
                return targetSize;
            }
        }

        return Islands.IslandSize.NORMAL;
    }

    private void regenerateIsland(Player player, String[] args) {
        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

        if (!player.hasPermission(Permissions.island.regenerate)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        Islands.IslandSize islandSize = args.length == 3 ? parseIslandSize(args[2]) : Islands.IslandSize.NORMAL;

        String permissionRequired;

        switch (islandSize) {
            case BIG:
                permissionRequired = Permissions.island.createBig;
                break;
            case SMALL:
                permissionRequired = Permissions.island.createSmall;
                break;
            case NORMAL:
            default:
                permissionRequired = Permissions.island.createNormal;
                break;
        }

        if (!player.hasPermission(permissionRequired)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Messages.help.REGENERATE);

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD
                            +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
                }
            }

            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null ||
                (!plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.Bypass.regenerate))) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        Biome targetBiome = getTargetBiome(args[1]);

        if (targetBiome == null) {
            player.sendMessage(Messages.error.NO_BIOME_FOUND);
            return;
        }

        if (!plugin.islands.islandGeneration.biomes.availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);
            return;
        }

        try {
            boolean success = plugin.islands.regenerateIsland(islandId, targetBiome, islandSize, player);

            if (!success) {
                player.sendMessage(Messages.error.ONGOING_QUEUE_EVENT);
                return;
            }

            player.sendTitle(Messages.success.ISLAND_GEN_TITLE, Messages.success.ISLAND_GEN_SUBTITLE, 10, 20 * 7, 10);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);
        }
    }

    private static class Messages {
        public static class error {
            public static final String UNAUTHORIZED = error("You don't own this island.");
            public static final String NOT_PUBLIC = error("The island must be public");
            public static final String NO_PLAYER_FOUND = error("No given player found.");
            public static final String NAME_TAKEN = error("That name is already taken.");
            public static final String ONGOING_QUEUE_EVENT = error("Wait for your current queue event to finish.");
            public static final String NAME_BLOCKED = error("You can't use that name");
            public static final String NO_PERMISSION = error("You don't have permission to use that command.");
            public static final String ISLAND_LIMIT = error("You already have maximum amount of islands.");
            public static final String WRONG_WORLD = error("You can't use that command in this world.");
            public static String ISLAND_GEN_FAILED = error("Island regeneration failed.");
            public static String TELEPORT_FAILED = error("Could not teleport.");
            public static String NO_BIOME_FOUND = error("Biome not found.");
            public static String NO_LOCATIONS_FOR_BIOME = error("No available locations for specified biome.");
        }

        public static class success {
            public static final String DELETED = success("Island deleted successfully. It will be overwritten when someone creates a new island.");
            public static final String UNNAMED = success("Island unnamed and made private.");
            public static final String ISLAND_GEN_TITLE = ChatColor.GOLD + "Island generation event added to queue.";
            public static final String ISLAND_GEN_SUBTITLE = ChatColor.GOLD + "Explore the wilderness while your island is being generated. Use /home to access your island.";
            public static String ISLAND_GEN = success("Island generation started.");

            public static String OWNER_CHANGED(String name) {
                return success("Island owner switched to " + name + ".");
            }

            public static String NAME_CHANGED(String name) {
                return success("Island name changed to " + name + ". Anyone with your island name can now visit it.");
            }

            public static String ISLAND_RECEIVED(String playerName, String islandName) {
                return success("You are now the owner of " + playerName + "'s island " + islandName + ".");
            }
        }

        public static class help {
            public static String CREATE = ChatColor.GRAY + "/island create <biome> (<BIG/NORMAL/SMALL>)";
            public static String REGENERATE = ChatColor.GRAY + "/island regenerate <biome> (<BIG/NORMAL/SMALL>) (You have to be on target island)";
            public static String NAME = ChatColor.GRAY + "/island name <name> (You have to be on target island)";
            public static String UNNAME = ChatColor.GRAY + "/island unname (You have to be on target island)";
            public static String GIVE = ChatColor.GRAY + "/island give <name> (You have to be on target island)";
            public static String DELETE = ChatColor.GRAY + "/island delete (You have to be on target island)";
        }
    }
}
