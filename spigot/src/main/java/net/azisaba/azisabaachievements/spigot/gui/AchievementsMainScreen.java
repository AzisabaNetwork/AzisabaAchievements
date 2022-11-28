package net.azisaba.azisabaachievements.spigot.gui;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AchievementsMainScreen extends Screen {
    private final Player player;
    private final List<TranslatedAchievement> achievements;
    private final Map<Key, Long> counts = new HashMap<>();

    public AchievementsMainScreen(@NotNull Player player, @NotNull List<TranslatedAchievement> achievements, @NotNull Set<PlayerAchievementData> playerAchievements) {
        super(27, "実績メニュー"); // TODO: hardcoded message
        this.player = player;
        this.achievements = achievements;
        initCounts(playerAchievements);
        initInventory();
    }

    private void initCounts(@NotNull Set<PlayerAchievementData> playerAchievements) {
        for (PlayerAchievementData data : playerAchievements) {
            if (counts.getOrDefault(data.getAchievementKey(), 0L) < data.getCount() || data.getCount() < 0) {
                counts.put(data.getAchievementKey(), data.getCount());
            }
        }
    }

    public void initInventory() {
        for (int i = 0; i < 27; i++) {
            setItem(i, BLACK_PANE);
        }
        // TODO: hardcoded message
        ItemStack unlocked = createItem(Material.DIAMOND, 0, ChatColor.GREEN + "解除済みの実績", null);
        // TODO: hardcoded message
        ItemStack locked = createItem(Material.IRON_INGOT, 0, ChatColor.RED + "未解除の実績", null);
        setItem(11, unlocked);
        setItem(15, locked);
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            if (e.getInventory().getHolder() instanceof AchievementsMainScreen) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getInventory().getHolder() instanceof AchievementsMainScreen)) {
                return;
            }
            e.setCancelled(true);
            AchievementsMainScreen screen = (AchievementsMainScreen) e.getInventory().getHolder();
            if (!e.getWhoClicked().equals(screen.player)) {
                // wrong player
                e.getWhoClicked().closeInventory();
                return;
            }
            if (e.getCurrentItem() == null) {
                return;
            }
            if (e.getSlot() == 11) {
                List<TranslatedAchievement> list =
                        screen.achievements
                                .stream()
                                .filter(achievement -> screen.counts.getOrDefault(achievement.getData().getKey(), 0L) >= achievement.getData().getCount())
                                .collect(Collectors.toList());
                // TODO: hardcoded message
                screen.player.openInventory(new AchievementListScreen(screen.player, list, screen.counts, "解除済みの実績").getInventory());
            }
            if (e.getSlot() == 15) {
                List<TranslatedAchievement> list =
                        screen.achievements
                                .stream()
                                .filter(achievement -> screen.counts.getOrDefault(achievement.getData().getKey(), 0L) < achievement.getData().getCount())
                                .collect(Collectors.toList());
                // TODO: hardcoded message
                screen.player.openInventory(new AchievementListScreen(screen.player, list, screen.counts, "未解除の実績").getInventory());
            }
        }
    }
}
