package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class VisitGUI {
    private final Islands plugin;
    private final Player player;

    private int sort = 1;

    private final int PAGE_HEIGHT = 4; // < 1

    public VisitGUI(Islands plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        getGui().show(player);
    }

    public Gui getGui() {
        Gui gui = new Gui(PAGE_HEIGHT, Messages.get("info.VISIT_GUI_TITLE", getSort(false)));

        PaginatedPane pane = new PaginatedPane(0, 0, 9, PAGE_HEIGHT - 1);

        List<StaticPane> gotPanes = getPanes();

        int i = 0;
        for (StaticPane page : gotPanes) {
            pane.addPane(i, page);
            i++;
        }

        gui.addPane(pane);

        // Toolbar

        OutlinePane background = new OutlinePane(0, PAGE_HEIGHT - 1, 9, 1, Pane.Priority.LOWEST);
        background.addItem(new GuiItem(createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "", false, ""), inventoryClickEvent -> inventoryClickEvent.setCancelled(true)));
        background.setRepeat(true);

        gui.addPane(background);

        StaticPane back = new StaticPane(2, PAGE_HEIGHT - 1, 1, 1);
        StaticPane forward = new StaticPane(6, PAGE_HEIGHT - 1, 1, 1);

        back.addItem(new GuiItem(createGuiItem(Material.ARROW, ChatColor.GOLD + "Previous page", false), event -> {
            pane.setPage(pane.getPage() - 1);

            if (pane.getPage() == 0) {
                back.setVisible(false);
            }

            forward.setVisible(true);
            gui.update();
        }), 0, 0);

        back.setVisible(false);

        forward.addItem(new GuiItem(createGuiItem(Material.ARROW, ChatColor.GOLD + "Next page", false), event -> {

        pane.setPage(pane.getPage() + 1);

        if (pane.getPage() == pane.getPages() - 1) {
            forward.setVisible(false);
        }

        back.setVisible(true);
        gui.update();
        }), 0, 0);

        if (pane.getPages() <= 1) forward.setVisible(false);

        gui.addPane(back);
        gui.addPane(forward);

        StaticPane sort = new StaticPane(4, PAGE_HEIGHT - 1, 1, 1);

        sort.addItem(new GuiItem(createGuiItem(Material.REDSTONE, ChatColor.GOLD + "Sort by " + getSort(true), false), event -> {
            toggleSort();
            event.getWhoClicked().closeInventory();
            getGui().show(event.getWhoClicked());
        }), 0, 0);

        gui.addPane(sort);

        return gui;
    }

    private List<StaticPane> getPanes() {
        List<StaticPane> panes = new ArrayList<>();

        Map<String, Map<String, String>> publicIslands = plugin.layout.getPublicIslands();

        List<String> sortedSet = new ArrayList<>(publicIslands.keySet());

        // Sort islands
        if (sort == 1) { // Sort by date, oldest first
            sortedSet.sort(Comparator.comparingInt(a ->
                    IslandLayout.placement.getIslandIndex(new int[]{Integer.parseInt(a.split("x")[0]), Integer.parseInt(a.split("x")[1])})));
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
                displayName = Bukkit.getPlayer(UUID.fromString(publicIslands.get(islandId).get("owner"))).getDisplayName();
            } catch (Exception e) {
                displayName = "Server";
            }

            pane.addItem(new GuiItem(createGuiItem(BiomeMaterials.valueOf(publicIslands.get(islandId).get("material")).getMaterial(),
                        ChatColor.GOLD + publicIslands.get(islandId).get("name"),
                        displayName.equals("Server"),
                        ChatColor.GRAY + "By " + displayName),
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

    public String getSort(boolean invert) { // yuk
        int test = sort;
        if (invert) test = test == 0 ? 1 : 0;
        return test == 0 ? "name" : "date";
    }

    protected ItemStack createGuiItem(final Material material, final String name, boolean shiny, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        if (shiny) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setDisplayName(name);

        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

}
