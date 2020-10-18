package me.aleksilassila.islands.commands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.commands.subcommands.*;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.utils.ChatUtils;
import me.aleksilassila.islands.utils.ConfirmItem;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandManagmentCommands extends ChatUtils implements TabExecutor {
    private final Main plugin;
    private final Set<Subcommand> subcommands;

    public IslandManagmentCommands(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("island").setExecutor(this);

        subcommands = new HashSet<>();

        subcommands.add(new createSubcommand(plugin));
        subcommands.add(new regenerateSubcommand(plugin));
        subcommands.add(new deleteSubcommand(plugin));
        subcommands.add(new nameSubcommand(plugin));
        subcommands.add(new unnameSubcommand(plugin));
        subcommands.add(new giveSubcommand(plugin));
        subcommands.add(new setSpawnSubcommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!Permissions.checkPermission(player, Permissions.command.island)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return true;
        }

        boolean confirmed = false;
        String issuedCommand = String.join(" ", label, String.join(" ", args));

        ConfirmItem item = plugin.islands.confirmations.get(player.getUniqueId().toString());
        if (item != null
                && item.command.equals(issuedCommand)
                && !item.expired()) {
            plugin.islands.confirmations.remove(player.getUniqueId().toString());

            confirmed = true;
        } else {
            plugin.islands.confirmations.put(player.getUniqueId().toString(), new ConfirmItem(issuedCommand, 8 * 1000));
        }

        if (args.length >= 1) {
            Subcommand target = getSubcommand(args[0]);

            if (target == null) {
                player.sendMessage(Messages.error.SUBCOMMAND_NOT_FOUND);
                sendHelp(player);
                return true;
            }
            try {
                target.onCommand(player, Arrays.copyOfRange(args, 1, args.length), confirmed);
                return true;
            } catch (Exception e) {
                player.sendMessage(Messages.error.ERROR);
                return true;
            }
        }

        player.sendMessage(Messages.info.VERSION_INFO(plugin.getDescription().getVersion()));

        return true;
    }

    @Nullable
    private Subcommand getSubcommand(String name) {
        for (Subcommand subcommand : subcommands) {
            if (subcommand.getName().equalsIgnoreCase(name)) return subcommand;
        }

        return null;
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return null;

        Player player = (Player) sender;

        List<String> avalableArgs = new ArrayList<>();

        if (args.length == 1) {
            for (Subcommand subcommand : subcommands) {
                avalableArgs.add(subcommand.getName());
            }
        } else if (args.length > 1) {
            Subcommand currentSubcommand = getSubcommand(args[0]);
            if (currentSubcommand == null) return null;

            avalableArgs = currentSubcommand.onTabComplete(player, Arrays.copyOfRange(args, 1, args.length));
        }

        return avalableArgs;
    }

    public static class Utils {
        @Nullable
        public Biome getTargetBiome(String biome) {
             Biome targetBiome = null;

             for (Biome b : Biome.values()) {
                 if (b.name().equalsIgnoreCase(biome)) {
                     targetBiome = b;
                 }
             }

             return targetBiome;
        }
    }
}
