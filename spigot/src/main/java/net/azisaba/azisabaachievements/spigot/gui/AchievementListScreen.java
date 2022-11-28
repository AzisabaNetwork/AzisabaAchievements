package net.azisaba.azisabaachievements.spigot.gui;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AchievementListScreen extends Screen {
    private final Player player;
    private final List<TranslatedAchievement> achievements;
    private final Map<Key, Long> progress;
    private int page = 1;

    public AchievementListScreen(@NotNull Player player, @NotNull List<TranslatedAchievement> achievements, @NotNull Map<Key, Long> progress, @NotNull String title) {
        super(54, title);
        this.player = player;
        this.achievements = achievements;
        this.progress = progress;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < 45; i++) {
            setItem(i, GRAY_PANE);
        }
        for (int i = 45; i < 54; i++) {
            setItem(i, BLACK_PANE);
        }
        int start = (page - 1) * 45;
        int end = Math.min(start + 45, achievements.size());
        for (int i = start; i < end; i++) {
            TranslatedAchievement achievement = achievements.get(i);
            ItemStack item = new ItemStack(Material.DIAMOND);
            ItemMeta meta = item.getItemMeta();
            AchievementTranslationData translationData = achievement.getTranslationForLocale(player.getLocale());
            List<String> lore;
            if (translationData != null) {
                meta.setDisplayName(ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', translationData.getName()));
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.translateAlternateColorCodes('&', translationData.getDescription()).split("\n")
                ));
            } else {
                meta.setDisplayName(ChatColor.GREEN + achievement.getData().getKey().toString());
                lore = new ArrayList<>(Collections.singletonList(ChatColor.GRAY + "No description defined."));
            }
            lore.add("");
            long current = progress.getOrDefault(achievement.getData().getKey(), 0L);
            ChatColor color;
            if (current >= achievement.getData().getCount()) {
                color = ChatColor.GREEN;
            } else {
                color = ChatColor.RED;
            }
            lore.add(ChatColor.GOLD + "進行度: " + color + current + ChatColor.GRAY + "/" + ChatColor.GREEN + achievement.getData().getCount());
            meta.setLore(lore);
            item.setItemMeta(meta);
            setItem(i - start, item);
        }
        if (hasPreviousPage()) {
            ItemStack item = new ItemStack(Material.ARROW);
            setItem(45, item);
        }
        if (hasNextPage()) {
            ItemStack item = new ItemStack(Material.ARROW);
            setItem(53, item);
        }
    }

    private boolean hasNextPage() {
        return page < achievements.size() / 45 + 1;
    }

    private boolean hasPreviousPage() {
        return page > 1;
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
            e.setCancelled(true);
            AchievementListScreen screen = (AchievementListScreen) e.getInventory().getHolder();
            if (!e.getWhoClicked().equals(screen.player)) {
                // wrong player
                e.getWhoClicked().closeInventory();
                return;
            }
            if (e.getCurrentItem() == null) {
                return;
            }
            if (e.getSlot() == 45 && screen.hasPreviousPage()) {
                screen.page--;
                screen.refresh();
            } else if (e.getSlot() == 53 && screen.hasNextPage()) {
                screen.page++;
                screen.refresh();
            }
        }
    }
}
