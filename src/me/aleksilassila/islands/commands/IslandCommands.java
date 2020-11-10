package me.aleksilassila.islands.commands;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.subcommands.*;
import me.aleksilassila.islands.utils.ChatUtils;
import me.aleksilassila.islands.utils.ConfirmItem;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandCommands extends ChatUtils implements TabExecutor {
    private final Islands plugin;
    public final Set<Subcommand> subcommands;

    public IslandCommands(Islands plugin) {
        this.plugin = plugin;

        plugin.getCommand("island").setExecutor(this);

        subcommands = new HashSet<>();

        subcommands.add(new CreateSubcommand(plugin));
        subcommands.add(new RecreateSubcommand(plugin));
        subcommands.add(new ClearSubcommand(plugin));
        subcommands.add(new NameSubcommand(plugin));
        subcommands.add(new UnnameSubcommand(plugin));
        subcommands.add(new GiveSubcommand(plugin));
        subcommands.add(new SetSpawnSubcommand(plugin));
        subcommands.add(new SaveSubcommand(plugin));
        subcommands.add(new SetSpawnIslandSubcommand(plugin));
        subcommands.add(new ConfirmSubcommand());
        subcommands.add(new HelpSubcommand(this));
        subcommands.add(new InfoSubcommand(plugin));
        subcommands.add(new ModerateSubcommand(plugin));
        subcommands.add(new SettingsSubcommand(plugin));

        TeleportCommands teleportCommands = new TeleportCommands(plugin);

        boolean homePrefix = plugin.getConfig().getBoolean("homeSubcommand");

        TeleportCommands.HomeCommand homeCommand = teleportCommands.new HomeCommand(homePrefix);
        TeleportCommands.HomesCommand homesCommand = teleportCommands.new HomesCommand(homePrefix);

        if (homePrefix) {
            subcommands.add(homeCommand);
            subcommands.add(homesCommand);
        }

        new TrustCommands(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(Permissions.command.island)) {
            player.sendMessage(Messages.get("error.NO_PERMISSION"));
            return true;
        }

        if (args.length >= 1) {
            Subcommand target = getSubcommand(args[0]);

            if (target == null) {
                player.sendMessage(Messages.get("error.SUBCOMMAND_NOT_FOUND"));
                getSubcommand("help").onCommand(player, new String[0], true);
                return true;
            }

            if (target.getPermission() != null && !player.hasPermission(target.getPermission())) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            boolean confirmed = false;

            if (target.getName().equalsIgnoreCase("confirm")) {
                if (!plugin.confirmations.containsKey(player.getUniqueId().toString())) {
                    player.sendMessage(Messages.get("info.CONFIRM_ERROR"));
                    return true;
                }

                String targetCommand = plugin.confirmations.get(player.getUniqueId().toString()).command;

                if (plugin.confirmations.get(player.getUniqueId().toString()).expired()) {
                    player.sendMessage(Messages.get("info.CONFIRM_EXPIRED"));
                    return true;
                }

                target = getSubcommand(targetCommand);
                plugin.confirmations.remove(player.getUniqueId().toString());
                args = Arrays.copyOfRange(targetCommand.split(" "), 1, targetCommand.split(" ").length);
                confirmed = true;
            } else {
                String issuedCommand = String.join(" ", label, String.join(" ", args));
                plugin.confirmations.put(player.getUniqueId().toString(), new ConfirmItem(issuedCommand, 8 * 1000));
            }

            try {
                target.onCommand(player, Arrays.copyOfRange(args, 1, args.length), confirmed);
                return true;
            } catch (Exception e) {
                player.sendMessage(Messages.get("error.ERROR"));
                return true;
            }
        }

        player.sendMessage(Messages.get("info.VERSION_INFO", plugin.getDescription().getVersion()));

        return true;
    }

    @Nullable
    private Subcommand getSubcommand(String name) {
        if (name.split(" ").length > 1) {
            name = name.split(" ")[1];
        }

        for (Subcommand subcommand : subcommands) {
            if (subcommand.getName().equalsIgnoreCase(name)) return subcommand;
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return null;

        Player player = (Player) sender;

        List<String> availableArgs = new ArrayList<>();

        if (args.length == 1) {
            for (Subcommand subcommand : subcommands) {
                if (subcommand.getPermission() == null || player.hasPermission(subcommand.getPermission()))
                    availableArgs.add(subcommand.getName());
            }
        } else if (args.length > 1) {
            Subcommand currentSubcommand = getSubcommand(args[0]);
            if (currentSubcommand == null) return null;

            if (currentSubcommand.getPermission() == null || player.hasPermission(currentSubcommand.getPermission()))
                availableArgs = currentSubcommand.onTabComplete(player, Arrays.copyOfRange(args, 1, args.length));
        }

        return availableArgs;
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
