package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import me.aleksilassila.islands.commands.subcommands.*;
import me.aleksilassila.islands.utils.ChatUtils;
import me.aleksilassila.islands.utils.ConfirmItem;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IslandCommands extends ChatUtils implements TabExecutor {
    private final Plugin plugin;
    private final Islands islands;
    public final Set<Subcommand> subcommands = new HashSet<>();
    int confirmTimeout;

    public IslandCommands(Islands islands) {
        this.islands = islands;
        this.plugin = islands.plugin;

        plugin.getCommand("island").setExecutor(this);

        subcommands.add(new CreateSubcommand(islands));
        subcommands.add(new RecreateSubcommand(islands));
        subcommands.add(new ClearSubcommand(islands));
        subcommands.add(new NameSubcommand(islands));
        subcommands.add(new UnnameSubcommand(islands));
        subcommands.add(new GiveSubcommand(islands));
        subcommands.add(new SetSpawnSubcommand(islands));
        subcommands.add(new SaveSubcommand(islands));
        subcommands.add(new SetSpawnIslandSubcommand(islands));
        subcommands.add(new ConfirmSubcommand(islands));
        subcommands.add(new HelpSubcommand(islands, this));
        subcommands.add(new InfoSubcommand(islands));
        subcommands.add(new ModerateSubcommand(islands));

        TeleportCommands teleportCommands = new TeleportCommands(islands);

        boolean homePrefix = plugin.getConfig().getBoolean("homeSubcommand");
        confirmTimeout = plugin.getConfig().getInt("confirmTimeout", 8);

        TeleportCommands.HomeCommand homeCommand = teleportCommands.new HomeCommand(islands, homePrefix);
        TeleportCommands.HomesCommand homesCommand = teleportCommands.new HomesCommand(islands, homePrefix);

        if (homePrefix) {
            subcommands.add(homeCommand);
            subcommands.add(homesCommand);
        }
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
                if (!islands.confirmations.containsKey(player.getUniqueId().toString())) {
                    player.sendMessage(Messages.get("info.CONFIRM_ERROR"));
                    return true;
                }

                String targetCommand = islands.confirmations.get(player.getUniqueId().toString()).command;

                if (islands.confirmations.get(player.getUniqueId().toString()).expired()) {
                    player.sendMessage(Messages.get("info.CONFIRM_EXPIRED"));
                    return true;
                }

                target = getSubcommand(targetCommand);
                islands.confirmations.remove(player.getUniqueId().toString());
                args = Arrays.copyOfRange(targetCommand.split(" "), 1, targetCommand.split(" ").length);
                confirmed = true;
            } else {
                String issuedCommand = String.join(" ", label, String.join(" ", args));
                islands.confirmations.put(player.getUniqueId().toString(), new ConfirmItem(issuedCommand, confirmTimeout * 1000L));
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
}
