package net.azisaba.azisabaachievements.velocity.achievement;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
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
                            future.complete(new AchievementData(id, key, count, point, AchievementHideFlags.NEVER, MagicConstantBitField.of(AchievementFlags.class, 0), 0));
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
                future.complete(Optional.ofNullable(DataProvider.getAchievementByKey(queryExecutor, key)));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> progressAchievement(@NotNull UUID uuid, @NotNull Key key, long count) {
        if (count == 0L) {
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                AchievementData data = DataProvider.getAchievementByKey(queryExecutor, key);
                if (data == null) {
                    future.completeExceptionally(new IllegalStateException("Achievement does not exist"));
                    return;
                }
                if (data.getFlags().contains(AchievementFlags.CATEGORY) || data.getFlags().contains(AchievementFlags.UNOBTAINABLE)) {
                    future.completeExceptionally(new IllegalStateException("Achievement is unobtainable"));
                    return;
                }
                long achievementId = data.getId();
                long currentCount = queryExecutor.query("SELECT `count` FROM `player_achievements` WHERE `player_id` = ? AND `achievement_id` = ?", ps -> {
                    ps.setString(1, uuid.toString());
                    ps.setLong(2, achievementId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getLong("count");
                        } else {
                            return 0L;
                        }
                    }
                });
                queryExecutor.queryVoid("INSERT INTO `player_achievements` (`player_id`, `achievement_id`, `count`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `count` = VALUES(`count`)", ps -> {
                    ps.setString(1, uuid.toString());
                    ps.setLong(2, achievementId);
                    ps.setLong(3, Math.min(data.getCount(), currentCount + count));
                    ps.executeUpdate();
                    future.complete(currentCount < data.getCount() && currentCount + count >= data.getCount());
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }
}
