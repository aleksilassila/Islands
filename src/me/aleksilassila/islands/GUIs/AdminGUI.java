package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminGUI extends PageGUI {
    private final Player player;

    public AdminGUI(Player player) {
        this.player = player;
    }

    @Override
    public ChestGui getMainGui() {
        ChestGui gui = new ChestGui(3, Messages.get("gui.admin.TITLE"));
        gui.setOnTopClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        addBackground(gui, Material.GRAY_STAINED_GLASS_PANE);

        StaticPane navigationPane = new StaticPane(2, 1, 5, 1);

        ItemStack players = createGuiItem(Material.PLAYER_HEAD,
                Messages.get("gui.admin.PLAYERS_LINK"),
                false,
                Messages.get("gui.admin.PLAYERS_LINK_LORE"));

        navigationPane.addItem(new GuiItem(players, event -> showPlayersGui()), 1, 0);

        ItemStack islands = createGuiItem(Material.GRASS_BLOCK,
                Messages.get("gui.admin.ISLANDS_LINK"),
                false,
                Messages.get("gui.admin.ISLANDS_LINK_LORE"));

        navigationPane.addItem(new GuiItem(islands, event -> showIslandsGui()), 3, 0);

        gui.addPane(navigationPane);

        return gui;
    }

    public void showIslandsGui() {
        int PAGE_HEIGHT = 4;

        List<StaticPane> pages = new ArrayList<>();
        Map<String, Map<String, String>> publicIslands = IslandsConfig.getIslandsInfo(false);
        List<String> sortedSet = new ArrayList<>(publicIslands.keySet());

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        int itemCount = 0;
        for (String islandId : sortedSet) {
            if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                pages.add(pane);
                pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
            }

            String displayName;

            try {
                displayName = Bukkit.getOfflinePlayer(UUID.fromString(publicIslands.get(islandId).get("owner"))).getName();
            } catch (Exception e) {
                displayName = "Server";
            }

            pane.addItem(new GuiItem(createGuiItem(BiomeMaterials.valueOf(publicIslands.get(islandId).get("material")).getMaterial(),
                        Messages.get("gui.admin.ISLANDS_NAME", publicIslands.get(islandId).get("name")),
                        "Server".equals(displayName),
                        Messages.get("gui.admin.ISLANDS_LORE", displayName, Integer.parseInt(publicIslands.get(islandId).get("public")))),
                        event -> {
                            if (!(event.getWhoClicked() instanceof Player)) return; // Dunno if this is necessary in practice, cows don't click inventories

                            teleportIsland(islandId);
                        }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
            itemCount++;
        }

        if (pane.getItems().size() > 0) pages.add(pane);

        addMainMenuButton(createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.admin.ISLANDS_TITLE"), pages)).show(player);
    }

    public void showPlayersGui() {
        int PAGE_HEIGHT = 4;

        List<StaticPane> pages = new ArrayList<>();
        Map<String, Integer> players = IslandsConfig.getPlayers();

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        int itemCount = 0;
        for (String uuid : players.keySet()) {
            if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                pages.add(pane);
                pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

            String displayName = offlinePlayer.getName();
            if (displayName == null) continue;

            ItemStack skull = createGuiItem(Material.PLAYER_HEAD,
                    Messages.get("gui.admin.PLAYER_NAME", displayName),
                    false,
                    Messages.get("gui.admin.PLAYER_LORE", players.get(uuid)));

//            SkullMeta meta = (SkullMeta) skull.getItemMeta();
//            if (meta != null) {
//                meta.setOwningPlayer(offlinePlayer);
//                skull.setItemMeta(meta);
//            }

            pane.addItem(new GuiItem(skull,
                    event -> {
                        if (!(event.getWhoClicked() instanceof Player)) return; // Dunno if this is necessary in practice, cows don't click inventories

                        showPlayerIslandsGui(uuid);
                    }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
            itemCount++;
        }

        if (pane.getItems().size() > 0) pages.add(pane);

        addMainMenuButton(createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.admin.PLAYERS_TITLE"), pages)).show(player);
    }

    public void showPlayerIslandsGui(String uuid) {
        int PAGE_HEIGHT = 4;

        String displayName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
        if (displayName == null) return;

        List<StaticPane> pages = new ArrayList<>();
        Map<String, Map<String, String>> islands = IslandsConfig.getIslandsInfo(uuid);

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        int itemCount = 0;
        for (String islandId : islands.keySet()) {
            if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                pages.add(pane);
                pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
            }

            pane.addItem(new GuiItem(createGuiItem(BiomeMaterials.valueOf(islands.get(islandId).get("material")).getMaterial(),
                        Messages.get("gui.admin.ISLAND_NAME", islands.get(islandId).get("name")),
                        false,
                        Messages.get("gui.admin.ISLAND_LORE", islandId)),
                        event -> {
                            if (!(event.getWhoClicked() instanceof Player)) return; // Dunno if this is necessary in practice, cows don't click inventories

                            teleportIsland(islandId);
                        }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
            itemCount++;
        }

        if (pane.getItems().size() > 0) pages.add(pane);

        ChestGui gui = createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.admin.PLAYER_TITLE", displayName), pages);

        StaticPane back = new StaticPane(4, gui.getRows() - 1, 1, 1);

        back.addItem(new GuiItem(createGuiItem(Material.BARRIER,
                Messages.get("gui.BACK"),
                false),
                inventoryClickEvent -> showPlayersGui()), 0, 0);

        gui.addPane(back);

        gui.show(player);
    }

    private void teleportIsland(String islandId) {
        Location location = IslandsConfig.getIslandSpawn(islandId);

        if (location != null) {
            player.teleport(location);
        } else {
            player.sendMessage(Messages.get("error.ISLAND_NOT_FOUND"));
        }
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
