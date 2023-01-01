package net.azisaba.azisabaachievements.spigot.gui;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import net.azisaba.azisabaachievements.spigot.message.SMessages;
import net.azisaba.azisabaachievements.spigot.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AchievementsMainScreen extends Screen {
    private final Player player;
    private final List<TranslatedAchievement> achievements;
    private final Map<Key, Long> counts = new HashMap<>();

    public AchievementsMainScreen(@NotNull Player player, @NotNull List<TranslatedAchievement> achievements, @NotNull Set<PlayerAchievementData> playerAchievements) {
        super(null, 27, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.title"));
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

    private @NotNull Set<AchievementData> getUnlockedAchievements() {
        return achievements
                .stream()
                .map(TranslatedAchievement::getData)
                .filter(data -> counts.getOrDefault(data.getKey(), 0L) >= data.getCount())
                .collect(Collectors.toSet());
    }

    private @NotNull Set<AchievementData> getObtainableAchievements() {
        return achievements
                .stream()
                .map(TranslatedAchievement::getData)
                .filter(data -> !data.getFlags().contains(AchievementFlags.UNOBTAINABLE) && !data.getFlags().contains(AchievementFlags.CATEGORY))
                .collect(Collectors.toSet());
    }

    public void initInventory() {
        for (int i = 0; i < 27; i++) {
            setItem(i, BLACK_PANE);
        }
        Set<AchievementData> obtainable = getObtainableAchievements();
        Set<AchievementData> unlocked = getUnlockedAchievements();
        // obtainable + unlocked (may include unobtainable achievements)
        Set<AchievementData> all = Stream.concat(obtainable.stream(), unlocked.stream()).collect(Collectors.toSet());
        long allPoints = all.stream().mapToLong(AchievementData::getPoint).sum();
        long unlockedPoints = unlocked.stream().mapToLong(AchievementData::getPoint).sum();
        double unlockedPointsPercentage = (double) unlockedPoints / allPoints * 100;
        double unlockedPercentage = (double) unlocked.size() / all.size() * 100;
        setItem(10, Material.GRASS, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.general.title"), null);
        setItem(11, Material.WHEAT, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.life.title"), null);
        setItem(12, Material.BOW, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.lgw.title"), null);
        setItem(13, Material.RED_ROSE, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.diverse.title"), null);
        setItem(14, Material.WATER_BUCKET, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.sclat.title"), null);
        setItem(15, Material.CHEST, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.vanilife.title"), null);
        setItem(16, Material.MONSTER_EGG, 58, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.despawn.title"), null);
        setItem(21, Material.APPLE, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.seasonal.title"),
                Arrays.asList(SMessages.getFormattedMessage(player, "gui.achievementMainScreen.seasonal.description").split("\n")));
        setItem(23, Material.DIAMOND_BLOCK, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.legacy.title"),
                Arrays.asList(SMessages.getFormattedMessage(player, "gui.achievementMainScreen.legacy.description").split("\n")));

        ItemStack stack = createItem(Material.SKULL_ITEM, 3, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.player.title", PlayerUtil.getFullDisplayName(player)),
                Arrays.asList(SMessages.getFormattedMessage(player, "gui.achievementMainScreen.player.description",
                        unlockedPoints, allPoints, unlockedPointsPercentage, unlocked.size(), all.size(), unlockedPercentage).split("\n")));
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(player);
        stack.setItemMeta(meta);
        setItem(22, stack);
    }

    public static boolean filterHiddenAchievements(@NotNull AchievementData data, @NotNull Map<Key, Long> counts) {
        if (data.getHidden() == AchievementHideFlags.NEVER) {
            return true;
        }
        if (data.getHidden() == AchievementHideFlags.UNLESS_PROGRESS && counts.getOrDefault(data.getKey(), 0L) > 0) {
            return true;
        }
        return data.getHidden() == AchievementHideFlags.UNLESS_UNLOCKED && counts.getOrDefault(data.getKey(), 0L) >= data.getCount();
    }

    @Contract(pure = true)
    public static int compareAchievementData(@NotNull Map<Key, Long> counts, @NotNull AchievementData a, @NotNull AchievementData b) {
        if (a.getFlags().contains(AchievementFlags.CATEGORY) && !b.getFlags().contains(AchievementFlags.CATEGORY)) {
            return -1;
        }
        if (!a.getFlags().contains(AchievementFlags.CATEGORY) && b.getFlags().contains(AchievementFlags.CATEGORY)) {
            return 1;
        }
        if (counts.getOrDefault(a.getKey(), 0L) >= a.getCount() &&
                counts.getOrDefault(b.getKey(), 0L) < b.getCount()) {
            return -1;
        }
        if (counts.getOrDefault(a.getKey(), 0L) < a.getCount() &&
                counts.getOrDefault(b.getKey(), 0L) >= b.getCount()) {
            return 1;
        }
        return a.getKey().toString().compareTo(b.getKey().toString());
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
            Consumer<String> handle = what -> {
                List<TranslatedAchievement> list =
                        screen.achievements
                                .stream()
                                .filter(achievement -> !achievement.getData().getFlags().contains(AchievementFlags.SEASONAL) &&
                                        !achievement.getData().getFlags().contains(AchievementFlags.UNOBTAINABLE) &&
                                        ("azisaba:" + what).equals(achievement.getData().getKey().parent().toString()) &&
                                        filterHiddenAchievements(achievement.getData(), screen.counts)
                                )
                                .sorted((a, b) -> compareAchievementData(screen.counts, a.getData(), b.getData()))
                                .collect(Collectors.toList());
                String title = SMessages.getFormattedMessage(screen.player, "gui.achievementMainScreen." + what + ".title");
                screen.player.openInventory(new AchievementListScreen(screen.inventory, screen.player, list, screen.counts, title).getInventory());
            };
            if (e.getSlot() == 10) handle.accept("general");
            if (e.getSlot() == 11) handle.accept("life");
            if (e.getSlot() == 12) handle.accept("lgw");
            if (e.getSlot() == 13) handle.accept("diverse");
            if (e.getSlot() == 14) handle.accept("sclat");
            if (e.getSlot() == 15) handle.accept("vanilife");
            if (e.getSlot() == 16) handle.accept("despawn");
            if (e.getSlot() == 21) {
                List<TranslatedAchievement> list =
                        screen.achievements
                                .stream()
                                .filter(achievement -> achievement.getData().getFlags().contains(AchievementFlags.SEASONAL) &&
                                        !achievement.getData().getFlags().contains(AchievementFlags.UNOBTAINABLE) &&
                                        filterHiddenAchievements(achievement.getData(), screen.counts)
                                )
                                .sorted((a, b) -> compareAchievementData(screen.counts, a.getData(), b.getData()))
                                .collect(Collectors.toList());
                String title = SMessages.getFormattedMessage(screen.player, "gui.achievementMainScreen.seasonal.title");
                screen.player.openInventory(new AchievementListScreen(screen.inventory, screen.player, list, screen.counts, title).getInventory());
            }
            if (e.getSlot() == 23) {
                List<TranslatedAchievement> list =
                        screen.achievements
                                .stream()
                                .filter(achievement -> achievement.getData().getFlags().contains(AchievementFlags.UNOBTAINABLE) &&
                                        filterHiddenAchievements(achievement.getData(), screen.counts)
                                )
                                .sorted((a, b) -> compareAchievementData(screen.counts, a.getData(), b.getData()))
                                .collect(Collectors.toList());
                String title = SMessages.getFormattedMessage(screen.player, "gui.achievementMainScreen.legacy.title");
                screen.player.openInventory(new AchievementListScreen(screen.inventory, screen.player, list, screen.counts, title).getInventory());
            }
        }
    }
}
