package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IslandCommands {
    public static class UntrustCommand implements CommandExecutor {
        private Main plugin;

        public UntrustCommand(Main plugin) {
            this.plugin = plugin;

            plugin.getCommand("untrust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.island.untrust)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }


            if (!player.getWorld().equals(plugin.islandsWorld)) {
                player.sendMessage(Messages.error.WRONG_WORLD);
                return true;
            }


            if (args.length != 1) {
                player.sendMessage(Messages.help.UNTRUST);
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.error.NOT_ON_ISLAND);
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !player.hasPermission(Permissions.Bypass.untrust)) {
                player.sendMessage(Messages.error.NOT_OWNED);
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(Messages.error.PLAYER_NOT_FOUND);
                return true;
            }

            plugin.islands.grid.removeTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(Messages.success.UNTRUSTED);

            return true;
        }
    }

    public static class TrustCommand implements CommandExecutor {
        private final Main plugin;

        public TrustCommand(Main plugin) {
            this.plugin = plugin;

            plugin.getCommand("trust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.island.trust)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(Messages.help.TRUST);
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.error.NOT_ON_ISLAND);
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !player.hasPermission(Permissions.Bypass.trust)) {
                player.sendMessage(Messages.error.NOT_OWNED);
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(Messages.error.PLAYER_NOT_FOUND);
                return true;
            }

            plugin.islands.grid.addTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(Messages.success.TRUSTED);

            return true;
        }
    }

    public static class VisitCommand implements CommandExecutor {
        private Main plugin;

        public VisitCommand(Main plugin) {
            this.plugin = plugin;

            plugin.getCommand("visit").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.island.visit)) {
                player.sendMessage(Messages.error.NO_PERMISSION);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(Messages.help.VISIT);
                return true;
            }


            String islandId = plugin.islands.grid.getPublicIsland(args[0]);

            if (islandId != null) {
                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));
            } else {
                player.sendMessage(Messages.error.ISLAND_NOT_FOUND);
            }

            return true;
        }
    }

    public static class HomeCommand implements CommandExecutor {
        private Main plugin;
        private IslandGrid grid;

        public HomeCommand(Main plugin) {
            this.plugin = plugin;
            this.grid = plugin.islands.grid;

            plugin.getCommand("home").setExecutor(this);
            plugin.getCommand("homes").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            if (args.length == 1 && args[0].equalsIgnoreCase("list") || label.equalsIgnoreCase("homes")) {
                if (!player.hasPermission(Permissions.island.listHomes)) {
                    player.sendMessage(Messages.error.NO_PERMISSION);
                    return true;
                }

                List<String> ids = plugin.islands.grid.getAllIslandIds(player.getUniqueId());

                player.sendMessage(Messages.success.HOMES_FOUND(ids.size()));
                for (String islandId : ids) {
                    String name = plugin.getIslandsConfig().getString("islands." + islandId + ".name");
                    String homeNumber = plugin.getIslandsConfig().getString("islands." + islandId + ".home");
                    player.sendMessage(ChatColor.AQUA + " - " + name + " (" + homeNumber + ")");
                }

                return true;
            } else {
                if (!player.hasPermission(Permissions.island.home)) {
                    player.sendMessage(Messages.error.NO_PERMISSION);
                    return true;
                }

                try {
                    if (args.length != 0) {
                        Integer.parseInt(args[0]);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Messages.help.HOME);
                    return true;
                }
            }

            if (player.getWorld().getName().equals("world_nether")) {
                player.sendMessage(Messages.info.IN_OVERWORLD);
                return true;
            }

            if (player.getWorld().getName().equals("world")) {
                // Check if is on surface
                Location playerLocation = player.getLocation();

                for (int y = playerLocation.getBlockY(); y < player.getWorld().getHighestBlockYAt(playerLocation); y++) {
                    playerLocation.setY(y);
                    if (player.getWorld().getBlockAt(playerLocation).getBlockData().getMaterial().equals(Material.STONE)) {
                        player.sendMessage(Messages.info.ON_SURFACE);
                        return true;
                    }
                }
            }

            String homeId = args.length == 0 ? "1" : args[0];

            Location location = grid.getIslandSpawn(grid.getHomeIsland(player.getUniqueId(), homeId));

            if (location != null) {
                player.teleport(location);
            } else {
                player.sendMessage(Messages.error.ISLAND_NOT_FOUND);
            }

            return true;
        }
    }

    static class Messages extends ChatUtils {
        static class error {
            public static final String ISLAND_NOT_FOUND = error("404 - Home not found.");
            public static final String NO_PERMISSION = error("You don't have permission to use this command.");
            public static final String NOT_ON_ISLAND = error("You have to be on an island.");
            public static final String NOT_OWNED = error("You don't own this island.");
            public static final String PLAYER_NOT_FOUND = error("Player not found.");
            public static final String WRONG_WORLD = error("You can't use that command in this world.");
        }

        static class success {
            public static final String UNTRUSTED = success("Player untrusted!");
            public static final String TRUSTED = success("Player trusted!");

            public static String HOMES_FOUND(int amount) {
                return success("Found " + amount + " home(s).");
            }
        }

        static class info {
            public static final String ON_SURFACE = info("You can only use this command on surface.");
            public static final String IN_OVERWORLD = info("You can only use this command in overworld.");
        }

        static class help {

            public static final String UNTRUST = info("/untrust <player> (You have to be on target island)");
            public static final String TRUST = info("/trust <player> (You have to be on target island)");
            public static final String VISIT = info("Usage: /visit name");
            public static final String HOME = error("Usage: /home <id>");
        }
    }
}