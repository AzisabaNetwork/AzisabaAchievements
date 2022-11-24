package net.azisaba.azisabaachievements.velocity.achievement;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.common.sql.DataProvider;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityAchievementManager implements AchievementManager {
    private final QueryExecutor queryExecutor;
    private final TaskScheduler scheduler;

    public VelocityAchievementManager(@NotNull QueryExecutor queryExecutor, @NotNull TaskScheduler scheduler) {
        this.queryExecutor = queryExecutor;
        this.scheduler = scheduler;
    }

    @Override
    public @NotNull CompletableFuture<AchievementData> createAchievement(@NotNull Key key, long count, int point) {
        CompletableFuture<AchievementData> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                queryExecutor.queryVoid("INSERT INTO `achievements` (`key`, `count`, `point`) VALUES (?, ?, ?)", ps -> {
                    ps.setString(1, key.toString());
                    ps.setLong(2, count);
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

    @Override
    public @NotNull CompletableFuture<@NotNull Optional<AchievementData>> getAchievement(@NotNull Key key) {
        CompletableFuture<Optional<AchievementData>> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                queryExecutor.queryVoid("SELECT `count`, `point` FROM `achievements` WHERE `key` = ?", ps -> {
                    ps.setString(1, key.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long count = rs.getLong("count");
                            int point = rs.getInt("point");
                            future.complete(Optional.of(new AchievementData(-1, key, count, point)));
                        } else {
                            future.complete(Optional.empty());
                        }
                    }
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> progressAchievement(@NotNull UUID uuid, @NotNull Key key, long count) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                AchievementData data = DataProvider.getAchievementByKey(queryExecutor, key);
                if (data == null) {
                    future.completeExceptionally(new IllegalStateException("Achievement does not exist"));
                    return;
                }
                long achievementId = data.getId();
                queryExecutor.queryVoid("INSERT INTO `progress` (`player_id`, `achievement_id`, `count`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `count` = `count` + VALUES(`count`)", ps -> {
                    ps.setString(1, uuid.toString());
                    ps.setLong(2, achievementId);
                    ps.setLong(3, count);
                    ps.executeUpdate();
                    future.complete(count >= data.getCount());
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }
}
