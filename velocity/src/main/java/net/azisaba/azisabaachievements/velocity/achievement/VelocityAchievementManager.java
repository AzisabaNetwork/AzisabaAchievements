package net.azisaba.azisabaachievements.velocity.achievement;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public class VelocityAchievementManager implements AchievementManager {
    private final QueryExecutor queryExecutor;
    private final TaskScheduler scheduler;

    public VelocityAchievementManager(@NotNull QueryExecutor queryExecutor, @NotNull TaskScheduler scheduler) {
        this.queryExecutor = queryExecutor;
        this.scheduler = scheduler;
    }

    @Override
    public @NotNull CompletableFuture<AchievementData> createAchievement(@NotNull Key key, int count, int point) {
        CompletableFuture<AchievementData> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                queryExecutor.queryVoid("INSERT INTO `achievements` (`key`, `count`, `point`) VALUES (?, ?, ?)", ps -> {
                    ps.setString(1, key.toString());
                    ps.setInt(2, count);
                    ps.setInt(3, point);
                    ps.executeUpdate();
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long id = generatedKeys.getLong(1);
                            future.complete(new AchievementData(id, key, count, point));
                        } else {
                            future.completeExceptionally(new IllegalStateException("Failed to create achievement"));
                        }
                    }
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }
}
