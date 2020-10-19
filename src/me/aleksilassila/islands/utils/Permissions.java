package me.aleksilassila.islands.utils;

public class Permissions {
    public static class bypass {
        public static final String create = "islands.bypass.islandLimit";       // Create unlimited islands
        public static final String recreate = "islands.bypass.recreate";        // Recreate any island
        public static final String delete = "islands.bypass.delete";            // Delete any island
        public static final String give = "islands.bypass.give";                // Transfer any island ownership
        public static final String name = "islands.bypass.name";                // Name any island
        public static final String unname = "islands.bypass.unname";            // Unname any island
        public static final String setSpawn = "islands.bypass.setspawn";        // Set any island's spawn

        public static final String trust = "islands.bypass.trust";
        public static final String untrust = "islands.bypass.untrust";
        public static final String listTrusted = "islands.bypass.turst.list";

        public static final String interactEverywhere = "islands.bypass.protection";
        public static final String home = "islands.bypass.home";
    }

    public static class command {
        public static final String create = "islands.command.create";
        public static final String createCustom = "islands.command.create.custom";
        public static final String recreate = "islands.command.recreate";
        public static final String delete = "islands.command.delete";
        public static final String give = "islands.command.give";
        public static final String name = "islands.command.name";
        public static final String unname = "islands.command.unname";
        public static final String setSpawn = "islands.command.setspawn";

        public static final String home = "islands.command.home";
        public static final String listHomes = "islands.command.home.list";
        public static final String trust = "islands.command.turst";
        public static final String listTrusted = "islands.command.turst.list";
        public static final String untrust = "islands.command.untrust";
        public static final String visit = "islands.command.visit";
        public static final String island = "islands.command";
    }
}
