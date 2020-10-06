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
            public static final String ISLAND_NO_OWNER = error("To make an island private, it must have an owner.");
            public static final String NO_BIOME_FOUND = error("Biome not found.");
            public static final String NO_LOCATIONS_FOR_BIOME = error("No available locations for specified biome.");
            public static final String ISLAND_NOT_FOUND = error("404 - Home not found.");
            public static final String HOME_NOT_FOUND = error("404 - Home not found :(");
            public static final String NOT_TRUSTED = error("You need owner's permission to interact here.");
            public static final String INVALID_ISLAND_SIZE = error("Island size exceeds limits.");

            public static String COOLDOWN(int remainingTime) {
                return error("You took damage recently. You have to wait for " + remainingTime + "s before teleporting.");
            }
        }

        public static class success {
            public static final String DELETED = success("Island deleted successfully. It will be overwritten when someone creates a new island.");
            public static final String UNNAMED = success("Island unnamed and made private.");
            public static final String ISLAND_GEN_TITLE = ChatColor.GOLD + "Island generation event added to queue.";
            public static final String ISLAND_GEN_SUBTITLE = ChatColor.GOLD + "Your island is being generated. Use /home to access your island.";
            public static final String UNTRUSTED = success("Player untrusted!");
            public static final String TRUSTED = success("Player trusted!");
            public static final String SPAWNPOINT_CHANGED = success("Island spawn point changed.");
            public static final String OWNER_REMOVED = success("Island owner removed.");
            public static final String GENERATION_DONE = success("Island generation completed.");
            public static final String CLEARING_DONE = success("Island clearing done.");

            public static String OWNER_CHANGED(String name) {
                return success("Island owner switched to " + name + ".");
            }

            public static String NAME_CHANGED(String name) {
                return success("Island name changed to " + name + ". Anyone with your island name can now visit it.");
            }

            public static String ISLAND_RECEIVED(String playerName, String islandName) {
                return success("You are now the owner of " + playerName + "'s island " + islandName + ".");
            }

            public static String HOMES_FOUND(int amount) {
                return success("Found " + amount + " home(s).");
            }
        }

        public static class info {
            public static final String CONFIRM = info("Are you sure? Repeat the command to confirm.");
            public static final String ON_SURFACE = info("You can only use this command on surface.");
            public static final String IN_OVERWORLD = info("You can only use this command in overworld.");

            public static String TRUSTED_INFO(int numberOfPlayers) {
                return info("You have trusted " + numberOfPlayers + " player(s).");
            }

            public static String TRUSTED_PLAYER(String displayName) {
                return ChatColor.GRAY + " - " + displayName;
            }

            public static String GENERATION_STARTED(double time) {
                return info("Your generation event has been started. It will take approximately " + (int) time + " seconds.");
            }

            public static String QUEUE_STATUS(int queueSize) {
                return info("Your event has been added to the queue. There are " + (queueSize - 1) + " event(s) before yours.");
            }

            public static String GENERATION_STATUS(int status) {
                return info("Your generation event is " + status + "% completed.");
            }

            public static String CLEARING_STATUS(int status) {
                return info("Clearing event " + status + "% completed.");
            }

            public static String VERSION_INFO(String version) {
                return info("Islands " + version);
            }
        }

        public static class help {
            public static final String UNTRUST = info("/untrust <player> (You have to be on target island)");
            public static final String TRUST = info("/trust <player> (You have to be on target island)");
            public static final String SETSPAWN = info("/island setspawn");
            public static final String CREATE = ChatColor.GRAY + "/island create <biome> (<BIG/NORMAL/SMALL>)";
            public static final String REGENERATE = ChatColor.GRAY + "/island regenerate <biome> (<BIG/NORMAL/SMALL>) (You have to be on target island)";
            public static final String NAME = ChatColor.GRAY + "/island name <name> (You have to be on target island)";
            public static final String UNNAME = ChatColor.GRAY + "/island unname (You have to be on target island)";
            public static final String GIVE = ChatColor.GRAY + "/island give <name> (You have to be on target island)";
            public static final String DELETE = ChatColor.GRAY + "/island delete (You have to be on target island)";
            public static final String VISIT = info("Usage: /visit name");
            public static final String HOME = error("Usage: /home <id>");
        }
    }