package net.azisaba.azisabaachievements.spigot.event;

import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when an achievement is unlocked.
 */
public final class AchievementUnlockedEvent extends AsyncEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final UUID uuid;
    private final AchievementData achievement;

    public AchievementUnlockedEvent(@NotNull UUID uuid, @NotNull AchievementData achievement) {
        this.uuid = uuid;
        this.achievement = achievement;
    }

    /**
     * Returns the unique id of the player.
     * @return the uuid
     */
    @Contract(pure = true)
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    /**
     * Returns the unlocked achievement.
     * @return the achievement
     */
    @Contract(pure = true)
    public @NotNull AchievementData getAchievement() {
        return achievement;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Contract(pure = true)
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
