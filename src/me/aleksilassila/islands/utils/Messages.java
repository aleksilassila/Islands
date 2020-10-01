package me.aleksilassila.islands.utils;

import org.bukkit.ChatColor;

public class Messages extends ChatUtils {
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
            public static final String SUBCOMMAND_NOT_FOUND = error("Invalid subcommand.");
            public static final String ERROR = "An internal error occurred. Contact staff.";
            public static final String NOT_ON_ISLAND = error("You have to be on an island.");
            public static final String NOT_OWNED = error("You don't own this island.");
            public static final String PLAYER_NOT_FOUND = error("Player not found.");
            public static String NO_BIOME_FOUND = error("Biome not found.");
            public static String NO_LOCATIONS_FOR_BIOME = error("No available locations for specified biome.");
        }

        public static class success {
            public static final String DELETED = success("Island deleted successfully. It will be overwritten when someone creates a new island.");
            public static final String UNNAMED = success("Island unnamed and made private.");
            public static final String ISLAND_GEN_TITLE = ChatColor.GOLD + "Island generation event added to queue.";
            public static final String ISLAND_GEN_SUBTITLE = ChatColor.GOLD + "Explore the wilderness while your island is being generated. Use /home to access your island.";
            public static final String UNTRUSTED = success("Player untrusted!");
            public static final String TRUSTED = success("Player trusted!");
            public static final String SPAWNPOINT_CHANGED = success("Island spawn point changed.");

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

        public static class info {
            public static final String CONFIRM = info("Are you sure? Repeat the command to confirm.");

            public static String TRUSTED_INFO(int numberOfPlayers) {
                return info("You have trusted " + numberOfPlayers + " player(s).");
            }

            public static String TRUSTED_PLAYER(String displayName) {
                return ChatColor.GRAY + " - " + displayName;
            }
        }

        public static class help {
            public static final String UNTRUST = info("/untrust <player> (You have to be on target island)");
            public static final String TRUST = info("/trust <player> (You have to be on target island)");
            public static final String SETSPAWN = info("/island setspawn");
            public static String CREATE = ChatColor.GRAY + "/island create <biome> (<BIG/NORMAL/SMALL>)";
            public static String REGENERATE = ChatColor.GRAY + "/island regenerate <biome> (<BIG/NORMAL/SMALL>) (You have to be on target island)";
            public static String NAME = ChatColor.GRAY + "/island name <name> (You have to be on target island)";
            public static String UNNAME = ChatColor.GRAY + "/island unname (You have to be on target island)";
            public static String GIVE = ChatColor.GRAY + "/island give <name> (You have to be on target island)";
            public static String DELETE = ChatColor.GRAY + "/island delete (You have to be on target island)";
        }
    }