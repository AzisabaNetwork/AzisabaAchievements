package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AchievementManager {
    /**
     * Creates a new achievement. If the achievement already exists, the future will be completed exceptionally.
     * @param key the achievement key
     * @param count the achievement count required to unlock
     * @param point the achievement point
     * @return newly created achievement data
     */
    @NotNull
    CompletableFuture<@NotNull AchievementData> createAchievement(@NotNull Key key, long count, int point);

    /**
     * Fetches the achievement data. If the achievement does not exist, the optional will be empty.
     * @param key the achievement key
     * @return the achievement data
     */
    @NotNull
    CompletableFuture<@NotNull Optional<AchievementData>> getAchievement(@NotNull Key key);

    /**
     * Progresses the achievement. If the achievement does not exist, the future will be completed exceptionally.
     * @param uuid the player uuid
     * @param key the achievement key
     * @param count the progress count
     * @return true if the achievement is unlocked (count &gt;= required count) by this progress
     */
    @NotNull
    CompletableFuture<Boolean> progressAchievement(@NotNull UUID uuid, @NotNull Key key, long count);
}
