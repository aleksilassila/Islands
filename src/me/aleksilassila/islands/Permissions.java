package me.aleksilassila.islands;

public class Permissions {
    public static class Bypass {
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
