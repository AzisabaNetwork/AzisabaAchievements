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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AchievementsMainScreen extends Screen {
    private final Player player;
    private final List<TranslatedAchievement> achievements;
    private final Map<Key, Long> counts = new HashMap<>();
    private final MainMenuConfig config;

    public AchievementsMainScreen(@NotNull Player player, @NotNull List<TranslatedAchievement> achievements, @NotNull Set<PlayerAchievementData> playerAchievements) {
        this(player, achievements, playerAchievements, MainMenuConfig.get());
    }

    private AchievementsMainScreen(@NotNull Player player, @NotNull List<TranslatedAchievement> achievements, @NotNull Set<PlayerAchievementData> playerAchievements, @NotNull MainMenuConfig config) {
        super(null, config.rows * 9, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.title"));
        this.player = player;
        this.config = config;
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
                .filter(data -> !data.getFlags().contains(AchievementFlags.CATEGORY))
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
        if (config.filler != null) {
            ItemStack filler = createItem(config.filler, 0, " ", null);
            for (int i = 0; i < inventory.getSize(); i++) {
                setItem(i, filler);
            }
        }
        for (MainMenuConfig.Button button : config.buttons) {
            setItem(button.slot, button.item, 0, button.getName(player), button.getLore(player));
        }
        if (config.playerHeadSlot < 0) {
            return;
        }
        Set<AchievementData> obtainable = getObtainableAchievements();
        Set<AchievementData> unlocked = getUnlockedAchievements();
        // obtainable + unlocked (may include unobtainable achievements)
        Set<AchievementData> all = Stream.concat(obtainable.stream(), unlocked.stream()).collect(Collectors.toSet());
        long allPoints = all.stream().mapToLong(AchievementData::getPoint).sum();
        long unlockedPoints = unlocked.stream().mapToLong(AchievementData::getPoint).sum();
        double unlockedPointsPercentage = (double) unlockedPoints / allPoints * 100;
        double unlockedPercentage = (double) unlocked.size() / all.size() * 100;

        ItemStack stack = createItem(Material.PLAYER_HEAD, 0, SMessages.getFormattedMessage(player, "gui.achievementMainScreen.player.title", PlayerUtil.getFullDisplayName(player)),
                Arrays.asList(SMessages.getFormattedMessage(player, "gui.achievementMainScreen.player.description",
                        unlockedPoints, allPoints, unlockedPointsPercentage, unlocked.size(), all.size(), unlockedPercentage).split("\n")));
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(player);
        stack.setItemMeta(meta);
        setItem(config.playerHeadSlot, stack);
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
        int diffProgress = Long.compare(counts.getOrDefault(b.getKey(), 0L), counts.getOrDefault(a.getKey(), 0L));
        if (diffProgress != 0) {
            return diffProgress;
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
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof AchievementsMainScreen)) {
                return;
            }
            AchievementsMainScreen screen = (AchievementsMainScreen) e.getInventory().getHolder();
            if (!e.getWhoClicked().equals(screen.player)) {
                // wrong player
                e.getWhoClicked().closeInventory();
                return;
            }
            if (e.getCurrentItem() == null) {
                return;
            }
            MainMenuConfig.Button button = screen.config.getButton(e.getSlot());
            if (button == null) {
                return;
            }
            Predicate<AchievementData> filter;
            switch (button.type) {
                case SEASONAL:
                    filter = data -> data.getFlags().contains(AchievementFlags.SEASONAL) &&
                            !data.getFlags().contains(AchievementFlags.UNOBTAINABLE);
                    break;
                case LEGACY:
                    filter = data -> data.getFlags().contains(AchievementFlags.UNOBTAINABLE);
                    break;
                case CATEGORY:
                default:
                    filter = data -> !data.getFlags().contains(AchievementFlags.SEASONAL) &&
                            !data.getFlags().contains(AchievementFlags.UNOBTAINABLE) &&
                            data.getKey().toString().startsWith("azisaba:" + button.category + '/');
                    break;
            }
            List<TranslatedAchievement> list =
                    screen.achievements
                            .stream()
                            .filter(achievement -> filter.test(achievement.getData()) &&
                                    filterHiddenAchievements(achievement.getData(), screen.counts))
                            .sorted((a, b) -> compareAchievementData(screen.counts, a.getData(), b.getData()))
                            .collect(Collectors.toList());
            String title = button.getName(screen.player);
            screen.player.openInventory(new AchievementListScreen(screen.inventory, screen.player, list, screen.counts, title).getInventory());
        }
    }
}
