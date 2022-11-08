package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class PageGUI extends GUI {
    public PageGUI(Islands islands) {
        super(islands);
    }

    protected ChestGui createPaginatedGUI(int pageHeight, String title, List<StaticPane> pages) {
        ChestGui gui = new ChestGui(pageHeight, title);
        gui.setOnTopClick(event -> event.setCancelled(true));

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

        back.addItem(new GuiItem(createGuiItem(Material.ARROW, Messages.get("gui.PREVIOUS_PAGE"), false), event -> {
            pane.setPage(pane.getPage() - 1);

            if (pane.getPage() == 0) {
                back.setVisible(false);
            }

            forward.setVisible(true);
            gui.update();
        }), 0, 0);

        back.setVisible(false);

        forward.addItem(new GuiItem(createGuiItem(Material.ARROW, Messages.get("gui.NEXT_PAGE"), false), event -> {

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

    protected ChestGui addMainMenuButton(ChestGui gui) {
        StaticPane pane = new StaticPane(4, gui.getRows() - 1, 1, 1);

        pane.addItem(new GuiItem(createGuiItem(Material.BARRIER,
                Messages.get("gui.BACK"),
                false),
                event -> {
                    if (!(event.getWhoClicked() instanceof Player))
                        return; // Dunno if this is necessary in practice, cows don't click inventories

                    open();
                }), 0, 0);

        gui.addPane(pane);

        return gui;
    }
}
