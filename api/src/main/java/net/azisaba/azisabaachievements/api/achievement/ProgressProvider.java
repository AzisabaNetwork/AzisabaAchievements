package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Source of per-player achievement progress. Register an implementation with Bukkit Services:
 * {@code Bukkit.getServicesManager().register(ProgressProvider.class, impl, plugin, ServicePriority.Normal)}.
 * Keys are this plugin's achievement keys (for example {@code azisaba:general/foo}). Missing keys are treated as 0.
 */
public interface ProgressProvider {
    @NotNull Map<Key, Long> getProgress(@NotNull UUID playerId);

    /**
     * Adds to the progress of an achievement. Called from
     * {@link AchievementManager#progressAchievement(UUID, Key, long)}. The default implementation does not support
     * writes.
     * @param playerId the player uuid
     * @param key the achievement key
     * @param amount the amount to add
     * @throws UnsupportedOperationException if this provider does not support writes
     */
    default void addProgress(@NotNull UUID playerId, @NotNull Key key, long amount) {
        throw new UnsupportedOperationException("This ProgressProvider does not support adding progress");
    }

    /**
     * Sets the progress of an achievement. Called from
     * {@link AchievementManager#setAchievementProgress(UUID, Key, long)}. The default implementation does not support
     * writes.
     * @param playerId the player uuid
     * @param key the achievement key
     * @param count the new progress count (never negative)
     * @throws UnsupportedOperationException if this provider does not support writes
     */
    default void setProgress(@NotNull UUID playerId, @NotNull Key key, long count) {
        throw new UnsupportedOperationException("This ProgressProvider does not support setting progress");
    }
}
