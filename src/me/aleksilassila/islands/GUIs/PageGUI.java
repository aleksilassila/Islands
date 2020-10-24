package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageGUI {
    protected Gui createPaginatedGUI(int pageHeight, String title, List<StaticPane> pages) {
        Gui gui = new Gui(pageHeight, title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        PaginatedPane pane = new PaginatedPane(0, 0, 9, pageHeight - 1);

        int i = 0;
        for (StaticPane page : pages) {
            pane.addPane(i, page);
            i++;
        }

        gui.addPane(pane);

        // Toolbar

        OutlinePane background = new OutlinePane(0, pageHeight - 1, 9, 1, Pane.Priority.LOWEST);
        background.addItem(new GuiItem(createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.RESET + "", false)));
        background.setRepeat(true);

        gui.addPane(background);

        StaticPane back = new StaticPane(2, pageHeight - 1, 1, 1);
        StaticPane forward = new StaticPane(6, pageHeight - 1, 1, 1);

        back.addItem(new GuiItem(createGuiItem(Material.ARROW, Messages.get("gui.visit.PREVIOUS_PAGE"), false), event -> {
            pane.setPage(pane.getPage() - 1);

            if (pane.getPage() == 0) {
                back.setVisible(false);
            }

            forward.setVisible(true);
            gui.update();
        }), 0, 0);

        back.setVisible(false);

        forward.addItem(new GuiItem(createGuiItem(Material.ARROW, Messages.get("gui.visit.NEXT_PAGE"), false), event -> {

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

        return gui;
    }

    protected ItemStack createGuiItem(final Material material, final String name, boolean shiny, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        if (shiny) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setDisplayName(name);

        List<String> lores = new ArrayList<>();

        for (String l : lore) {
            lores.addAll(Arrays.asList(l.split("\n")));
        }

        meta.setLore(lores);
        item.setItemMeta(meta);

        return item;
    }
}
