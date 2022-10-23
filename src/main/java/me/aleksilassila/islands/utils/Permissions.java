package me.aleksilassila.islands.utils;

public class Permissions {
    public static class bypass {
        public static final String create = "islands.bypass.islandLimit";       // Create unlimited islands
        public static final String recreate = "islands.bypass.recreate";        // Recreate any island
        public static final String clear = "islands.bypass.clear";              // Delete any island
        public static final String give = "islands.bypass.give";                // Transfer any island ownership
        public static final String name = "islands.bypass.name";                // Name any island
        public static final String unname = "islands.bypass.unname";            // Unname any island
        public static final String setSpawn = "islands.bypass.setspawn";        // Set any island's spawn

        public static final String interactInPlot = "islands.bypass.plotprotection";
        public static final String home = "islands.bypass.home";
        public static final String economy = "islands.bypass.economy";
        public static final String queue = "islands.bypass.queue";
        public static final String queueLimit = "islands.bypass.queue.limit";
        public static final String info = "islands.bypass.info";
        public static final String neutralTeleport = "islands.bypass.neutralTeleport";
    }

    public static class command {
        public static final String create = "islands.command.create";
        public static final String createCustom = "islands.command.create.custom";
        public static final String createAny = "islands.command.create.*";
        public static final String recreate = "islands.command.recreate";
        public static final String clear = "islands.command.clear";
        public static final String give = "islands.command.give";
        public static final String name = "islands.command.name";
        public static final String unname = "islands.command.unname";
        public static final String setSpawn = "islands.command.setspawn";
        public static final String makeSpawnIsland = "islands.command.setspawnisland";
        public static final String island = "islands.command";
        public static final String save = "islands.command.save";
        public static final String confirm = "islands.command.confirm";
        public static final String info = "islands.command.info";
        public static final String moderate = "islands.command.moderate";

        public static final String home = "islands.command.home";
        public static final String listHomes = "islands.command.home.list";
        public static final String visit = "islands.command.visit";
    }
}
