package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TrustCommands {
    private final Islands plugin;

    public TrustCommands(Islands plugin) {
        this.plugin = plugin;

        new UntrustCommand();
        new TrustCommand();
        new ListTrustedCommand();
    }

    public class UntrustCommand implements CommandExecutor {
        public UntrustCommand() {
            plugin.getCommand("untrust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.command.untrust)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            if (!player.getWorld().equals(plugin.islandsWorld)) {
                Messages.send(player, "error.WRONG_WORLD");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(Messages.help.UNTRUST);
                return true;
            }

            String ownerUUID = plugin.layout.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !player.hasPermission(Permissions.bypass.untrust)) {
                player.sendMessage(Messages.get("error.NOT_OWNED"));
                return true;
            }

            OfflinePlayer targetPlayer = Utils.getOfflinePlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(Messages.get("error.PLAYER_NOT_FOUND"));
                return true;
            }

            plugin.layout.removeTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(Messages.get("success.UNTRUSTED"));

            return true;
        }
    }

    public class TrustCommand implements CommandExecutor {
        public TrustCommand() {
            plugin.getCommand("trust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.command.trust)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            if (!player.getWorld().equals(plugin.islandsWorld)) {
                Messages.send(player, "error.WRONG_WORLD");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(Messages.help.TRUST);
                return true;
            }

            String ownerUUID = plugin.layout.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !player.hasPermission(Permissions.bypass.trust)) {
                player.sendMessage(Messages.get("error.NOT_OWNED"));
                return true;
            }

            OfflinePlayer targetPlayer = Utils.getOfflinePlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(Messages.get("error.PLAYER_NOT_FOUND"));
                return true;
            }

            plugin.layout.addTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(Messages.get("success.TRUSTED"));

            return true;
        }
    }

    public class ListTrustedCommand implements CommandExecutor {
        public ListTrustedCommand() {
            plugin.getCommand("trusted").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            plugin.confirmations.remove(player.getUniqueId().toString());

            if (!player.hasPermission(Permissions.command.listTrusted)) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            if (!player.getWorld().equals(plugin.islandsWorld)) {
                Messages.send(player, "error.WRONG_WORLD");
                return true;
            }

            String ownerUUID = plugin.layout.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString()) && !player.hasPermission(Permissions.bypass.listTrusted)) {
                player.sendMessage(Messages.get("error.NOT_OWNED"));
                return true;
            }

            List<String> trustedList = plugin.layout.getTrusted(islandId);

            player.sendMessage(Messages.get("info.TRUSTED_INFO", trustedList.size()));
            for (String uuid : trustedList) {
                OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

                if (trustedPlayer.getName() != null)
                    Messages.send(player, "info.TRUSTED_PLAYER", trustedPlayer.getName());
            }

            return true;
        }
    }
}
