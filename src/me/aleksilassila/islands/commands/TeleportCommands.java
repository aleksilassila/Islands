package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.GUIs.VisitGUI;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TeleportCommands {
    private final Islands plugin;
    private final IslandLayout layout;

    public TeleportCommands(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    public class VisitCommand implements CommandExecutor {

        public VisitCommand() {
            plugin.getCommand("visit").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            plugin.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.command.visit)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            if (args.length != 1) {
                new VisitGUI(plugin, player).open();
                return true;
            }

            if (!canTeleport(player) && !player.hasPermission(Permissions.bypass.home)) {
                player.sendMessage(Messages.get("error.COOLDOWN", teleportCooldown(player)));
                return true;
            }

            String islandId = plugin.layout.getIslandByName(args[0]);

            if (islandId != null) {
                player.teleport(plugin.layout.getIslandSpawn(islandId));
                player.sendTitle(Messages.get("success.VISIT_TITLE", args[0]), "", 10, 20 * 5, 10);
            } else {
                player.sendMessage(Messages.get("error.ISLAND_NOT_FOUND"));
            }

            return true;
        }
    }

    public class HomeCommand implements CommandExecutor {
        private final boolean allowHomeOnlyFromOverworld;
        private final boolean disableNeutralTeleports;
        private final int neutralTeleportRange;
        private final boolean unfinishedIslandTeleports;

        public HomeCommand() {
            plugin.getCommand("home").setExecutor(this);
            plugin.getCommand("homes").setExecutor(this);

            this.allowHomeOnlyFromOverworld = plugin.getConfig().getBoolean("allowHomeOnlyFromOverworld");
            this.disableNeutralTeleports = plugin.getConfig().getBoolean("disableNeutralTeleports");
            this.neutralTeleportRange = plugin.getConfig().getInt("neutralTeleportRange");
            this.unfinishedIslandTeleports = plugin.getConfig().getBoolean("unfinishedIslandTeleports");
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            plugin.confirmations.remove(player.getUniqueId().toString());

            if (args.length == 1 && args[0].equalsIgnoreCase("list") || label.equalsIgnoreCase("homes")) {
                if (!player.hasPermission(Permissions.command.listHomes)) {
                    player.sendMessage(Messages.get("error.NO_PERMISSION"));
                    return true;
                }

                List<String> ids = plugin.layout.getIslandIds(player.getUniqueId());

                player.sendMessage(Messages.get("success.HOMES_FOUND", ids.size()));
                for (String islandId : ids) {
                    String name = plugin.getIslandsConfig().getString(islandId + ".name");
                    String homeNumber = plugin.getIslandsConfig().getString(islandId + ".home");
                    player.sendMessage(Messages.get("success.HOME_ITEM", name, homeNumber));
                }

                return true;
            }

            if (!player.hasPermission(Permissions.command.home)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            if (!canTeleport(player) && !player.hasPermission(Permissions.bypass.home)) {
                player.sendMessage(Messages.get("error.COOLDOWN", teleportCooldown(player)));
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

            if (allowHomeOnlyFromOverworld && !player.getWorld().getEnvironment().equals(World.Environment.NORMAL)
                    && !player.hasPermission(Permissions.bypass.home)) {
                player.sendMessage(Messages.get("info.IN_OVERWORLD"));
                return true;
            }

            if (!player.hasPermission(Permissions.bypass.home)) {
                // Check if is on surface
                Location playerLocation = player.getLocation();

                for (int y = playerLocation.getBlockY(); y < player.getWorld().getHighestBlockYAt(playerLocation); y++) {
                    playerLocation.setY(y);
                    if (player.getWorld().getBlockAt(playerLocation).getBlockData().getMaterial().equals(Material.STONE)) {
                        player.sendMessage(Messages.get("info.ON_SURFACE"));
                        return true;
                    }
                }
            }

            int homeId;

            try {
                homeId = args.length == 0 ? 1 : Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                homeId = 1;
            }

            String islandId = layout.getHomeIsland(player.getUniqueId(), homeId);

            if (plugin.islandGeneration.queue.size() > 0
                    && plugin.islandGeneration.queue.get(0).getIslandId().equals(islandId)
                    && !unfinishedIslandTeleports) {
                Messages.send(player, "error.ISLAND_UNFINISHED");
                return true;
            }

            if (plugin.islandGeneration.queue.size() != 0)
                System.out.println("IslanId " + islandId + " and " + plugin.islandGeneration.queue.get(0).getIslandId());

            Location location = layout.getIslandSpawn(islandId);

            if (location != null) {
                if (!disableNeutralTeleports && player.hasPermission(Permissions.bypass.neutralTeleport)) {
                    List<Entity> entities = player.getNearbyEntities(neutralTeleportRange, neutralTeleportRange, neutralTeleportRange);
                    entities.removeIf(entity -> entity instanceof Monster || !entity.getType().isAlive());

                    Location animalLocation = location.clone();
                    animalLocation.setY(plugin.islandsWorld.getHighestBlockYAt(location) + 1);

                    for (Entity entity : entities) {
                        entity.teleport(animalLocation);
                    }
                }

                player.teleport(location);
            } else {
                player.sendMessage(Messages.get("error.HOME_NOT_FOUND"));
            }

            return true;
        }
    }

    private boolean canTeleport(Player player) {
        if (plugin.teleportCooldowns.containsKey(player.getUniqueId().toString())) {
            long cooldownTime  = plugin.getConfig().getInt("tpCooldownTime") * 1000;
            long timePassed = new Date().getTime() - plugin.teleportCooldowns.get(player.getUniqueId().toString());

            return timePassed >= cooldownTime;
        }

        return true;
    }

    private int teleportCooldown(Player player) {
        if (plugin.teleportCooldowns.containsKey(player.getUniqueId().toString())) {
            long cooldownTime  = plugin.getConfig().getInt("tpCooldownTime") * 1000;
            long timePassed = new Date().getTime() - plugin.teleportCooldowns.get(player.getUniqueId().toString());

            long remaining = cooldownTime - timePassed;

            return remaining < 0 ? 0 : (int)(remaining / 1000);
        }

        return 0;
    }
}