package net.azisaba.azisabaachievements.velocity.achievement;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerDataResult;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import net.azisaba.azisabaachievements.common.sql.DataProvider;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityAchievementManager implements AchievementManager {
    public static void sendServerData(@NotNull QueryExecutor queryExecutor) {
        AzisabaAchievementsProvider.get()
                .getScheduler()
                .builder(() -> {
                    Set<AchievementData> achievements = DataProvider.getAllAchievements(queryExecutor);
                    Set<AchievementTranslationData> translations = DataProvider.getAllTranslations(queryExecutor);
                    List<Map.Entry<Key, Long>> unlockedCounts = DataProvider.getUnlockedPlayerCounts(queryExecutor);
                    AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketServerDataResult(achievements, translations, unlockedCounts));
                }).async().schedule();
    }

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
                            future.complete(new AchievementData(id, key, count, point, AchievementHideFlags.NEVER, MagicConstantBitField.of(AchievementFlags.class, 0)));
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

    @Override
    public @NotNull CompletableFuture<Set<AchievementData>> getChildAchievements(@NotNull Key key) {
        CompletableFuture<Set<AchievementData>> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                future.complete(DataProvider.getChildAchievements(queryExecutor, key));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }

    @Override
    public void deleteAchievementBlocking(@NotNull Key key) {
        long id = DataProvider.getAchievementIdByKey(queryExecutor, key);
        try {
            queryExecutor.queryVoid("DELETE FROM `player_achievements` WHERE `achievement_id` = ?", ps -> {
                ps.setLong(1, id);
                ps.executeUpdate();
            });
            queryExecutor.queryVoid("DELETE FROM `achievement_translations` WHERE `id` = ?", ps -> {
                ps.setLong(1, id);
                ps.executeUpdate();
            });
            queryExecutor.queryVoid("DELETE FROM `achievements` WHERE `id` = ?", ps -> {
                ps.setLong(1, id);
                ps.executeUpdate();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAchievementAsync(@NotNull Key key) {
        scheduler.builder(() -> {
            try {
                deleteAchievementBlocking(key);
            } catch (Exception e) {
                Logger.getCurrentLogger().warn("", e);
            }
        }).async().schedule();
    }
}
