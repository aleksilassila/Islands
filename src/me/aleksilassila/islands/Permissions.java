package me.aleksilassila.islands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Permissions {
    public static boolean checkPermission(Player player, String permission) {
        if (player.hasPermission(permission)) return true;

        String[] parts = permission.split("\\.");

        Bukkit.getLogger().info("Parts: " + String.join(" ", parts));

        for (int index = 0; index < parts.length; index++) {
            String joined = String.join(".", Arrays.copyOfRange(parts, 0, index + 1));
            Bukkit.getLogger().info("Starred: " + joined + ".*");
            if (player.hasPermission(joined + ".*")) return true;
        }

        return false;
    }

    public static class bypass {
        public static final String create = "islands.bypass.islandLimit";       // Create unlimited islands
        public static final String regenerate = "islands.bypass.regenerate";    // Regenerate any island
        public static final String delete = "islands.bypass.delete";            // Delete any island
        public static final String give = "islands.bypass.give";                // Transfer any island ownership
        public static final String name = "islands.bypass.name";                // Name any island
        public static final String unname = "islands.bypass.unname";            // Unname any island

        public static final String trust = "islands.bypass.trust";
        public static final String untrust = "islands.bypass.untrust";
        public static final String listTrusted = "islands.bypass.turst.list";

        public static final String interactEverywhere = "islands.bypass.protection";
        public static final String home = "islands.bypass.home";
    }

    public static class island {
        public static final String create = "islands.create";
        public static final String createBig = "islands.create.big";
        public static final String createNormal = "islands.create.normal";
        public static final String createSmall = "islands.create.small";
        public static final String regenerate = "islands.regenerate";
        public static final String delete = "islands.delete";
        public static final String give = "islands.give";
        public static final String name = "islands.name";
        public static final String unname = "islands.unname";


        public static final String home = "islands.home";
        public static final String listHomes = "islands.home.list";
        public static final String trust = "islands.turst";
        public static final String listTrusted = "islands.turst.list";
        public static final String untrust = "islands.untrust";
        public static final String visit = "islands.visit";
        public static final String island = "islands";
    }
}
