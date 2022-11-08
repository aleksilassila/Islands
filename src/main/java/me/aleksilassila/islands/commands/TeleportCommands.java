package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.GUIs.VisitGUI;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.*;

public class TeleportCommands {
    private final Islands islands;
    private final Plugin plugin;
    private final boolean allowHomeOnlyFromOverworld;

    public TeleportCommands(Islands islands) {
        this.islands = islands;
        this.plugin = islands.plugin;
        this.allowHomeOnlyFromOverworld = plugin.getConfig().getBoolean("allowHomeOnlyFromOverworld");

        new VisitCommand();
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

            islands.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.command.visit)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            if (!worldCheck(player)) {
                player.sendMessage(Messages.get("info.IN_OVERWORLD"));
                return true;
            }

            if (!surfaceCheck(player)) {
                player.sendMessage(Messages.get("info.ON_SURFACE"));
                return true;
            }

            if (args.length != 1) {
                new VisitGUI(islands, player).open();
                return true;
            }

            if (!canTeleport(player) && !player.hasPermission(Permissions.bypass.home)) {
                player.sendMessage(Messages.get("error.COOLDOWN", teleportCooldown(player)));
                return true;
            }

            Entry e = islands.islandsConfig.getIslandByName(args[0]);

            if (e != null) {
                e.teleport(player);
                player.sendTitle(Messages.get("success.VISIT_TITLE", args[0]), "", 10, 20 * 5, 10);
            } else {
                player.sendMessage(Messages.get("error.ISLAND_NOT_FOUND"));
            }

            return true;
        }
    }

    public class HomesCommand extends Subcommand implements CommandExecutor {
        public HomesCommand(Islands islands, boolean subcommand) {
            super(islands);
            if (!subcommand) {
                plugin.getCommand("homes").setExecutor(this);
            }
        }

        @Override
        public void onCommand(Player player, String[] args, boolean confirmed) {
            onCommand(player, null, "homes", args);
        }

        @Override
        public List<String> onTabComplete(Player player, String[] args) {
            return new ArrayList<>();
        }

        @Override
        public String getName() {
            return "homes";
        }

        @Override
        public String help() {
            return null;
        }

        @Override
        public String getPermission() {
            return Permissions.command.listHomes;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            islands.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.command.listHomes)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            List<Entry> islands = this.islands.islandsConfig.getOwnedIslands(player.getUniqueId());
            Map<String, Integer> idMap = new HashMap<>();

            for (Entry e : islands) {
                idMap.put(e.islandId, e.homeId);
            }

            islands.sort(Comparator.comparingInt(e -> e.homeId));

            player.sendMessage(Messages.get("success.HOMES_FOUND", islands.size()));
            for (Entry e : islands) {
                Messages.send(player, "success.HOME_ITEM", e.name, idMap.get(e.islandId));
            }

            return true;
        }
    }

    public class HomeCommand extends Subcommand implements CommandExecutor {
        private final boolean disableNeutralTeleports;
        private final boolean enableAllTaggedTeleports;
        private final int neutralTeleportRange;
        private final boolean unfinishedIslandTeleports;

        public HomeCommand(Islands islands, boolean subcommand) {
            super(islands);
            if (!subcommand) {
                plugin.getCommand("home").setExecutor(this);
            }

            // Fixme use Config
            this.disableNeutralTeleports = plugin.getConfig().getBoolean("disableNeutralTeleports");
            this.enableAllTaggedTeleports = plugin.getConfig().getBoolean("teleportAllNameTagged");
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

            islands.confirmations.remove(player.getUniqueId().toString());

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
                Messages.send(player, "usage.HOME");
                return true;
            }

            if (!worldCheck(player)) {
                player.sendMessage(Messages.get("info.IN_OVERWORLD"));
                return true;
            }

            if (!surfaceCheck(player)) {
                player.sendMessage(Messages.get("info.ON_SURFACE"));
                return true;
            }

            int homeId;

            try {
                homeId = args.length == 0 ? islands.islandsConfig.getLowestHome(player.getUniqueId()) : Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                homeId = islands.islandsConfig.getLowestHome(player.getUniqueId());
            }

            Entry island = islands.islandsConfig.getHomeIsland(player.getUniqueId(), homeId);

            if (island == null) {
                player.sendMessage(Messages.get("error.HOME_NOT_FOUND"));
                return true;
            }

            if (islands.generator.queue.size() > 0
                    && islands.generator.queue.get(0).getIslandId().equals(island.islandId)
                    && !unfinishedIslandTeleports) {
                Messages.send(player, "error.ISLAND_UNFINISHED");
                return true;
            }

            teleportNeutrals(player, island.getIslandSpawn());
            island.teleport(player);

            return true;
        }

        @Override
        public void onCommand(Player player, String[] args, boolean confirmed) {
            onCommand(player, null, "home", args);
        }

        @Override
        public List<String> onTabComplete(Player player, String[] args) {
            return new ArrayList<>();
        }

        @Override
        public String getName() {
            return "home";
        }

        @Override
        public String help() {
            return null;
        }

        @Override
        public String getPermission() {
            return Permissions.command.home;
        }

        private void teleportNeutrals(Player player, Location location) {
            if (!disableNeutralTeleports && player.hasPermission(Permissions.bypass.neutralTeleport) && player.getWorld().equals(islands.wildernessWorld.getWorld())) {
                List<Entity> entities = player.getNearbyEntities(neutralTeleportRange, neutralTeleportRange, neutralTeleportRange);
                List<Entity> notAnimals = new ArrayList<>();
                entities.removeIf(entity -> {
                    if (!(entity instanceof Animals)
                            && !(entity instanceof Villager)) {
                        notAnimals.add(entity);
                        return true;
                    } else return false;
                });

                if (enableAllTaggedTeleports) {
                    for (Entity entity : notAnimals) {
                        if (entity.getCustomName() != null) {
                            entities.add(entity);
                        }
                    }
                }

                Location animalLocation = location.clone();
                animalLocation.setY(Utils.getHighestYAt(islands.islandsWorld.getWorld(), location.getBlockX(), location.getBlockZ()) + 2);

                for (Entity entity : entities) {
                    entity.teleport(animalLocation);
                }
            }
        }
    }

    private boolean canTeleport(Player player) {
        if (islands.teleportCooldowns.containsKey(player.getUniqueId().toString())) {
            long cooldownTime = plugin.getConfig().getInt("tpCooldownTime") * 1000L;
            long timePassed = new Date().getTime() - islands.teleportCooldowns.get(player.getUniqueId().toString());

            return timePassed >= cooldownTime;
        }

        return true;
    }

    /**
     * Check if player is on surface
     */
    private boolean surfaceCheck(Player player) {
        if (player.hasPermission(Permissions.bypass.home)) return true;

        Location playerLocation = player.getLocation();

        for (int y = playerLocation.getBlockY(); y < player.getWorld().getHighestBlockYAt(playerLocation); y++) {
            playerLocation.setY(y);
            if (player.getWorld().getBlockAt(playerLocation).getBlockData().getMaterial().equals(Material.STONE)) {
                return false;
            }
        }

        return true;
    }

    private boolean worldCheck(Player player) {
        if (!allowHomeOnlyFromOverworld) return true;
        return player.getWorld().getEnvironment().equals(World.Environment.NORMAL);
    }

    private int teleportCooldown(Player player) {
        if (islands.teleportCooldowns.containsKey(player.getUniqueId().toString())) {
            long cooldownTime = plugin.getConfig().getInt("tpCooldownTime") * 1000L;
            long timePassed = new Date().getTime() - islands.teleportCooldowns.get(player.getUniqueId().toString());

            long remaining = cooldownTime - timePassed;

            return remaining < 0 ? 0 : (int) (remaining / 1000);
        }

        return 0;
    }
}
