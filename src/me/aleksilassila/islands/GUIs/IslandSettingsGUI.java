package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandSettingsGUI extends PageGUI {
    private final Islands plugin;
    private final Player player;
    private final IslandLayout layout;
    private final String islandId;

    public IslandSettingsGUI(Islands plugin, String islandId, Player player) {
        this.player = player;
        this.islandId = islandId;
        this.layout = plugin.layout;
        this.plugin = plugin;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Gui getMainGui() {
        Gui gui = new Gui(3, Messages.get("gui.trust.TITLE"));
        gui.setOnTopClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        addBackground(gui, Material.GRAY_STAINED_GLASS_PANE);

        StaticPane navigation = new StaticPane(3, 1, 3, 1);

        // Global rules
        ItemStack globalRules = createGuiItem(Material.CLOCK,
                Messages.get("gui.trust.GLOBAL_RULES_BUTTON"),
                false,
                Messages.get("gui.trust.GLOBAL_RULES_LORE"));

        navigation.addItem(new GuiItem(globalRules, event -> showIslandProtectionMenu()), 0, 0);

        // Trusted players
        ItemStack trustedPlayers = createGuiItem(Material.PLAYER_HEAD,
                Messages.get("gui.trust.TRUSTED_PLAYERS_BUTTON"),
                false,
                Messages.get("gui.trust.TRUSTED_PLAYERS_LORE"));

        navigation.addItem(new GuiItem(trustedPlayers, event -> showTrustedPlayersList()), 2, 0);

        gui.addPane(navigation);

        return gui;
    }

    public void showTrustedPlayersList() {
        int PAGE_HEIGHT = 4;

        List<StaticPane> pages = new ArrayList<>();
        ConfigurationSection section = plugin.getIslandsConfig().getConfigurationSection(islandId + ".trusted");

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        if (section == null) {
            pages.add(pane);
        } else {
            Set<String> players = section.getKeys(false);

            int itemCount = 0;
            for (String uuid : players) {
                if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                    pages.add(pane);
                    pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
                }

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

                String displayName = offlinePlayer.getName();
                if (displayName == null) continue;

                ItemStack skull = createGuiItem(Material.PLAYER_HEAD,
                        Messages.get("gui.trust.players.PLAYER", displayName),
                        false,
                        Messages.get("gui.trust.players.PLAYER_LORE", displayName));

                pane.addItem(new GuiItem(skull,
                        event -> {
                            if (event.isShiftClick()) {
                                if (player.hasPermission(Permissions.command.trust))
                                    layout.removeTrusted(islandId, offlinePlayer.getUniqueId().toString());
                                showTrustedPlayersList();
                            } else showPlayerMenu(offlinePlayer);
                        }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
                itemCount++;
            }

            if (pane.getItems().size() > 0) pages.add(pane);
        }

        addMainMenuButton(createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.trust.players.TITLE"), pages)).show(player);
    }

    public void showPlayerMenu(OfflinePlayer offlinePlayer) {
        String uuid = offlinePlayer.getUniqueId().toString();

        Gui gui = new Gui(4, Messages.get("gui.trust.player.TITLE", offlinePlayer.getName()));
        gui.setOnTopClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        addBackground(gui, Material.GRAY_STAINED_GLASS_PANE);

        StaticPane settings = new StaticPane(1, 1, 7, 1);

        // Building
        if (player.hasPermission(Permissions.command.trust)) {
            boolean canBuild = layout.canBuild(islandId, uuid);
            ItemStack building = createGuiItem(Material.CHEST,
                    Messages.get("gui.trust.player.BUILDING"),
                    canBuild,
                    Messages.get("gui.trust.player.BUILDING_LORE", canBuild ? 1 : 0));

            settings.addItem(new GuiItem(building, event -> {
                layout.setBuildAccess(islandId, uuid, !canBuild);
                showPlayerMenu(offlinePlayer);
            }), settings.getItems().size() * 2, 0);
        }

        // Containers
        if (player.hasPermission(Permissions.command.containerTrust)) {
            boolean canAccessContainers = layout.canAccessContainers(islandId, uuid);
            ItemStack containers = createGuiItem(Material.CHEST,
                    Messages.get("gui.trust.player.CHESTS"),
                    canAccessContainers,
                    Messages.get("gui.trust.player.CHESTS_LORE", canAccessContainers ? 1 : 0));

            settings.addItem(new GuiItem(containers, event -> {
                layout.setContainerAccess(islandId, uuid, !canAccessContainers);
                showPlayerMenu(offlinePlayer);
            }), settings.getItems().size() * 2, 0);
        }

        // Doors
        if (player.hasPermission(Permissions.command.doorTrust)) {
            boolean canUseDoors = layout.canAccessDoors(islandId, uuid);
            ItemStack doors = createGuiItem(Material.OAK_DOOR,
                    Messages.get("gui.trust.player.DOORS"),
                    canUseDoors,
                    Messages.get("gui.trust.player.DOORS_LORE", canUseDoors ? 1 : 0));

            settings.addItem(new GuiItem(doors, event -> {
                layout.setDoorAccess(islandId, uuid, !canUseDoors);
                showPlayerMenu(offlinePlayer);
            }), settings.getItems().size() * 2, 0);
        }

        // Utility
        if (player.hasPermission(Permissions.command.utilityTrust)) {
            boolean canUseUtility = layout.canUseUtility(islandId, uuid);
            ItemStack utility = createGuiItem(Material.PLAYER_HEAD,
                    Messages.get("gui.trust.player.UTILITY"),
                    canUseUtility,
                    Messages.get("gui.trust.player.UTILITY_LORE", canUseUtility ? 1 : 0));

            settings.addItem(new GuiItem(utility, event -> {
                layout.setUtilityAccess(islandId, uuid, !canUseUtility);
                showPlayerMenu(offlinePlayer);
            }), settings.getItems().size() * 2, 0);
        }

        gui.addPane(settings);

        StaticPane back = new StaticPane(4, gui.getRows() - 1, 1, 1);

        back.addItem(new GuiItem(createGuiItem(Material.BARRIER,
                Messages.get("gui.BACK"),
                false),
                inventoryClickEvent -> showTrustedPlayersList()), 0, 0);

        gui.addPane(back);

        gui.show(player);
    }

    public void showIslandProtectionMenu() {
        Gui gui = new Gui(4, Messages.get("gui.trust.global.TITLE"));
        gui.setOnTopClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        addBackground(gui, Material.GRAY_STAINED_GLASS_PANE);

        StaticPane settings = new StaticPane(1, 1, 7, 1);

        // Building
        if (player.hasPermission(Permissions.command.generalBuildProtection)) {
            boolean buildProtection = layout.buildProtection(islandId);
            ItemStack building = createGuiItem(Material.CHEST,
                    Messages.get("gui.trust.global.BUILDING"),
                    buildProtection,
                    Messages.get("gui.trust.global.BUILDING_LORE", buildProtection ? 1 : 0));

            settings.addItem(new GuiItem(building, event -> {
                layout.setBuildProtection(islandId, !buildProtection);
                showIslandProtectionMenu();
            }), 0, 0);
        }

        // Containers
        if (player.hasPermission(Permissions.command.generalContainerTrust)) {
            boolean containerProtection = layout.containerProtection(islandId);
            ItemStack containers = createGuiItem(Material.CHEST,
                    Messages.get("gui.trust.global.CHESTS"),
                    containerProtection,
                    Messages.get("gui.trust.global.CHESTS_LORE", containerProtection ? 1 : 0));

            settings.addItem(new GuiItem(containers, event -> {
                layout.setContainerProtection(islandId, !containerProtection);
                showIslandProtectionMenu();
            }), 2, 0);
        }

        // Doors
        if (player.hasPermission(Permissions.command.generalDoorTrust)) {
            boolean doorProtection = layout.doorProtection(islandId);
            ItemStack doors = createGuiItem(Material.OAK_DOOR,
                    Messages.get("gui.trust.global.DOORS"),
                    doorProtection,
                    Messages.get("gui.trust.global.DOORS_LORE", doorProtection ? 1 : 0));

            settings.addItem(new GuiItem(doors, event -> {
                layout.setDoorProtection(islandId, !doorProtection);
                showIslandProtectionMenu();
            }), 4, 0);
        }

        // Utility
        if (player.hasPermission(Permissions.command.generalUtilityTrust)) {
            boolean utilityProtection = layout.utilityProtection(islandId);
            ItemStack utility = createGuiItem(Material.PLAYER_HEAD,
                    Messages.get("gui.trust.global.UTILITY"),
                    utilityProtection,
                    Messages.get("gui.trust.global.UTILITY_LORE", utilityProtection ? 1 : 0));

            settings.addItem(new GuiItem(utility, event -> {
                layout.setUtilityProtection(islandId, !utilityProtection);
                showIslandProtectionMenu();
            }), 6, 0);
        }

        gui.addPane(settings);

        addMainMenuButton(gui).show(player);
    }
}
