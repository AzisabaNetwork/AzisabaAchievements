package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface AchievementManager {
    @NotNull
    CompletableFuture<AchievementData> createAchievement(@NotNull Key key, int count, int point);
}
