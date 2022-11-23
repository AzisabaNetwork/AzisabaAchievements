package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AchievementManager {
    @NotNull
    CompletableFuture<@NotNull AchievementData> createAchievement(@NotNull Key key, int count, int point);

    @NotNull
    CompletableFuture<@NotNull Optional<AchievementData>> getAchievement(@NotNull Key key);
}
