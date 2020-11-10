package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class VisitGUI extends PageGUI {
    private final Player player;

    private int sort = 0; // 0 = date, 1 = name fixme test

    private final int PAGE_HEIGHT = 4; // < 1

    public VisitGUI(Player player) {
        this.player = player;
    }

    @Override
    public Gui getMainGui() {
        Gui gui = createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.visit.TITLE"), getPanes());

        StaticPane sort = new StaticPane(4, PAGE_HEIGHT - 1, 1, 1);

        sort.addItem(new GuiItem(createGuiItem(Material.REDSTONE, Messages.get("gui.visit.SORT", this.sort), false), event -> {
            toggleSort();
            open();
        }), 0, 0);

        gui.addPane(sort);

        return gui;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private List<StaticPane> getPanes() {
        List<StaticPane> panes = new ArrayList<>();

        Map<String, Map<String, String>> publicIslands = IslandsConfig.getIslandsInfo(true);

        List<String> sortedSet = new ArrayList<>(publicIslands.keySet());

        // Sort islands
        if (sort == 0) { // Sort by date, oldest first
            sortedSet.sort(Comparator.comparingInt(IslandsConfig.placement::getIslandIndex));
        } else { // Sort by name
            sortedSet.sort(Comparator.comparingInt(a -> publicIslands.get(a).get("name").charAt(0)));
        }

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        int itemCount = 0;
        for (String islandId : sortedSet) {
            if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                panes.add(pane);
                pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
            }

            String displayName;
            try {
                displayName = Bukkit.getOfflinePlayer(UUID.fromString(publicIslands.get(islandId).get("owner"))).getName();
            } catch (Exception e) {
                displayName = "Server";
            }


            pane.addItem(new GuiItem(createGuiItem(BiomeMaterials.valueOf(publicIslands.get(islandId).get("material")).getMaterial(),
                        Messages.get("gui.visit.ISLAND_NAME", publicIslands.get(islandId).get("name")),
                        "Server".equals(displayName),
                        Messages.get("gui.visit.ISLAND_OWNER", displayName)),
                        event -> {
                            if (!(event.getWhoClicked() instanceof Player)) return; // Dunno if this is necessary in practice, cows don't click inventories

                            ((Player) event.getWhoClicked()).performCommand("visit " + publicIslands.get(islandId).get("name"));
                        }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
            itemCount++;
        }

        if (pane.getItems().size() > 0) panes.add(pane);

        return panes;
    }

    public void toggleSort() {
        sort = sort == 0 ? 1 : 0;
    }
}
