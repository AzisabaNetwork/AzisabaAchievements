package net.azisaba.azisabaachievements.spigot.event;

import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AchievementListScreenClickEvent extends AbstractCancellableEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final TranslatedAchievement achievement;
    private final ItemStack itemStack;

    public AchievementListScreenClickEvent(@NotNull Player player, @NotNull TranslatedAchievement achievement, @NotNull ItemStack itemStack) {
        this.player = Objects.requireNonNull(player, "player");
        this.achievement = Objects.requireNonNull(achievement, "achievement");
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
    }

    @Contract(pure = true)
    public @NotNull Player getPlayer() {
        return player;
    }

    @Contract(pure = true)
    public @NotNull TranslatedAchievement getAchievement() {
        return achievement;
    }

    @Contract(pure = true)
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    @Contract(pure = true)
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
