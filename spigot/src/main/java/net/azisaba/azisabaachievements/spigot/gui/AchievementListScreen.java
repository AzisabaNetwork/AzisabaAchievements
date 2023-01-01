package net.azisaba.azisabaachievements.spigot.gui;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import net.azisaba.azisabaachievements.spigot.message.SMessages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AchievementListScreen extends Screen {
    private final Player player;
    private final List<TranslatedAchievement> achievements;
    private final Map<Key, Long> progress;
    private final List<TranslatedAchievement> filteredAchievements;
    private final Map<Integer, Consumer<InventoryClickEvent>> eventHandlers = new HashMap<>();
    private int page = 1;

    public AchievementListScreen(@Nullable Inventory parent, @NotNull Player player, @NotNull List<TranslatedAchievement> achievements, @NotNull Map<Key, Long> progress, @NotNull String title) {
        super(parent, 54, title);
        this.player = player;
        this.achievements = achievements;
        this.progress = progress;
        Key parentKey = findParent();
        if (parentKey == null) {
            filteredAchievements = Collections.emptyList();
        } else {
            filteredAchievements = achievements.stream()
                    .filter(data -> data.getData().getKey().parent().equals(parentKey))
                    .sorted((a, b) -> AchievementsMainScreen.compareAchievementData(progress, a.getData(), b.getData()))
                    .collect(Collectors.toList());
        }
        refresh();
    }

    private @Nullable Key findParent() {
        if (achievements == null) {
            return null;
        }
        Key key = null;
        int slashes = Integer.MAX_VALUE;
        for (TranslatedAchievement achievement : achievements) {
            Key k = achievement.getData().getKey().parent();
            int s = k.path().split("/").length;
            if (s == 1) {
                return k;
            }
            if (s < slashes) {
                key = k;
                slashes = s;
            }
        }
        return key;
    }

    private void refresh() {
        eventHandlers.clear();
        for (int i = 0; i < 45; i++) {
            setItem(i, LIGHT_GRAY_PANE);
        }
        for (int i = 45; i < 54; i++) {
            setItem(i, BLACK_PANE);
        }
        setItem(49, Material.ARROW, 0, SMessages.getFormattedMessage(player, "gui.back"), null);
        int start = (page - 1) * 45;
        int end = Math.min(start + 45, filteredAchievements.size());
        for (int i = start; i < end; i++) {
            TranslatedAchievement achievement = filteredAchievements.get(i);
            long current = progress.getOrDefault(achievement.getData().getKey(), 0L);
            Material type;
            if (achievement.getData().getFlags().contains(AchievementFlags.CATEGORY)) {
                type = Material.CHEST;
                List<TranslatedAchievement> children =
                        achievements
                                .stream()
                                .filter(data -> data.getData().getKey().toString().startsWith(achievement.getData().getKey().toString() + '/'))
                                .sorted((a, b) -> AchievementsMainScreen.compareAchievementData(progress, a.getData(), b.getData()))
                                .collect(Collectors.toList());
                eventHandlers.put(i - start, e -> {
                    AchievementTranslationData translationData = achievement.getTranslationForLocale(player.getLocale());
                    String title = translationData == null ? "?" : translationData.getName();
                    player.openInventory(new AchievementListScreen(inventory, player, children, progress, title).getInventory());
                });
            } else if (current == 0) {
                type = Material.BONE;
            } else if (current >= achievement.getData().getCount()) {
                type = Material.DIAMOND;
            } else {
                type = Material.GOLD_INGOT;
            }
            ItemStack item = new ItemStack(type);
            item.setAmount(Math.max(1, Math.min(64, achievement.getData().getPoint())));
            ItemMeta meta = item.getItemMeta();
            AchievementTranslationData translationData = achievement.getTranslationForLocale(player.getLocale());
            List<String> lore;
            ChatColor color;
            if (current >= achievement.getData().getCount()) {
                color = ChatColor.GREEN;
            } else {
                color = ChatColor.RED;
            }
            if (translationData != null) {
                meta.setDisplayName(color + ChatColor.translateAlternateColorCodes('&', translationData.getName()));
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.translateAlternateColorCodes('&', translationData.getDescription()).split("\n")
                ));
            } else {
                meta.setDisplayName(color + achievement.getData().getKey().toString());
                lore = new ArrayList<>(Collections.singletonList(ChatColor.GRAY + "No description defined."));
            }
            lore.add("");
            if (achievement.getData().getFlags().contains(AchievementFlags.CATEGORY)) {
                Set<AchievementData> children =
                        achievement.getData()
                                .getChildren()
                                .join()
                                .stream()
                                .filter(data -> !data.getFlags().contains(AchievementFlags.CATEGORY))
                                .collect(Collectors.toSet());
                Set<AchievementData> unlocked = children.stream().filter(c -> progress.getOrDefault(c.getKey(), 0L) >= c.getCount()).collect(Collectors.toSet());
                long allPoints = children.stream().mapToLong(AchievementData::getPoint).sum();
                long unlockedPoints = unlocked.stream().mapToLong(AchievementData::getPoint).sum();
                double unlockedPointsPercentage = (double) unlockedPoints / allPoints * 100;
                double unlockedPercentage = (double) unlocked.size() / children.size() * 100;
                lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.category.points",
                        unlockedPoints, allPoints, unlockedPointsPercentage));
                lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.category.unlocked",
                        unlocked.size(), children.size(), unlockedPercentage));
            } else {
                lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.entry.points",
                        achievement.getData().getPoint()));
                if (achievement.getData().getCount() > 1) {
                    lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.entry.progress",
                            color, current, achievement.getData().getCount()));
                }
            }
            if (achievement.getData().getHidden() != AchievementHideFlags.NEVER) {
                lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.hidden"));
            }
            lore.add("");
            if (current >= achievement.getData().getCount()) {
                lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.unlocked"));
            } else {
                lore.add(SMessages.getFormattedMessage(player, "gui.achievementListScreen.locked"));
            }
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
        return page < filteredAchievements.size() / 45 + 1;
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
            if (e.getSlot() >= 0 && e.getSlot() < 45 && screen.eventHandlers.containsKey(e.getSlot())) {
                Objects.requireNonNull(screen.eventHandlers.get(e.getSlot())).accept(e);
            }
            if (e.getSlot() == 49) {
                if (screen.getParent() != null) {
                    e.getWhoClicked().openInventory(screen.getParent());
                } else {
                    e.getWhoClicked().closeInventory();
                }
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
