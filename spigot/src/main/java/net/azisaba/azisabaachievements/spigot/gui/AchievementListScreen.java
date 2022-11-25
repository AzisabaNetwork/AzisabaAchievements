package net.azisaba.azisabaachievements.spigot.gui;

import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AchievementListScreen implements InventoryHolder {
    private final List<TranslatedAchievement> achievements;
    private final Inventory inventory;
    private final Player player;
    private int page = 1;

    public AchievementListScreen(@NotNull AchievementDataCache cache, @NotNull Player player) {
        this.achievements = cache.getAchievementsAsList();
        this.inventory = Bukkit.createInventory(this, 54, "nanntoka");
        this.player = player;
        refresh();
    }

    private void refresh() {
        inventory.clear();
        for (int i = 0; i < 54; i++) {
            ItemStack blackPane = new ItemStack(Material.STAINED_GLASS_PANE);
            blackPane.setDurability((short) 15);
            ItemMeta meta = blackPane.getItemMeta();
            meta.setDisplayName(" ");
            blackPane.setItemMeta(meta);
            inventory.setItem(i, blackPane);
        }
        int start = (page - 1) * 45;
        int end = Math.min(start + 45, achievements.size());
        for (int i = start; i < end; i++) {
            TranslatedAchievement achievement = achievements.get(i);
            ItemStack item = new ItemStack(Material.DIAMOND);
            ItemMeta meta = item.getItemMeta();
            AchievementTranslationData translationData = achievement.getTranslationForLocale(player.getLocale());
            if (translationData != null) {
                meta.setDisplayName(translationData.getName());
                meta.setLore(Arrays.asList(translationData.getDescription().split("\n")));
            } else {
                meta.setDisplayName(achievement.getData().getKey().toString());
                meta.setLore(Collections.singletonList("No description defined"));
            }
            item.setItemMeta(meta);
            inventory.setItem(i - start, item);
        }
        if (page > 1) {
            ItemStack item = new ItemStack(Material.ARROW);
            inventory.setItem(45, item);
        }
        if (page < achievements.size() / 45 + 1) {
            ItemStack item = new ItemStack(Material.ARROW);
            inventory.setItem(53, item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onDrag(InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof AchievementListScreen) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!(e.getInventory().getHolder() instanceof AchievementListScreen)) {
                return;
            }
            AchievementListScreen screen = (AchievementListScreen) e.getInventory().getHolder();
            if (!e.getWhoClicked().equals(screen.player)) {
                // wrong player
                e.getWhoClicked().closeInventory();
                return;
            }
            e.setCancelled(true);
            if (e.getCurrentItem() == null) {
                return;
            }
            if (e.getSlot() == 45) {
                screen.page--;
                screen.refresh();
            } else if (e.getSlot() == 53) {
                screen.page++;
                screen.refresh();
            }
        }
    }
}
