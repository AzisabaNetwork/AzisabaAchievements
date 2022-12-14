package net.azisaba.azisabaachievements.spigot.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Screen implements InventoryHolder {
    protected static final ItemStack BLACK_PANE = createItem(Material.STAINED_GLASS_PANE, (short) 15, " ", null);
    protected static final ItemStack GRAY_PANE = createItem(Material.STAINED_GLASS_PANE, (short) 7, " ", null);
    protected final @Nullable Inventory parent;
    protected final Inventory inventory;

    public Screen(@Nullable Inventory parent, int size, @Nullable String title) {
        this.parent = parent;
        if (title == null) {
            this.inventory = Bukkit.createInventory(this, size);
        } else {
            this.inventory = Bukkit.createInventory(this, size, title);
        }
    }

    @Contract(pure = true)
    public final @Nullable Inventory getParent() {
        return parent;
    }

    protected final void setItem(int slot, @Nullable ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected final void clearItems() {
        inventory.clear();
    }

    @Contract(pure = true)
    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * A method called when the screen is closed (closing) by player. Default implementation opens the parent screen,
     * if any.
     * @param e the event
     */
    protected void onClose(@NotNull InventoryCloseEvent e) {
        if (getParent() != null) {
            e.getPlayer().openInventory(getParent());
        }
    }

    public static @NotNull ItemStack createItem(@NotNull Material type, int durability, @Nullable String title, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(type);
        if (durability != 0) {
            item.setDurability((short) durability);
        }
        ItemMeta meta = item.getItemMeta();
        if (title != null) {
            meta.setDisplayName(title);
        }
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryClose(@NotNull InventoryCloseEvent e) {
            if (!(e.getInventory().getHolder() instanceof Screen)) {
                return;
            }
            Screen screen = (Screen) e.getInventory().getHolder();
            screen.onClose(e);
        }
    }
}
