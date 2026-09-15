package net.azisaba.azisabaachievements.velocity.reward;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.reward.RewardDelivery;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import net.azisaba.azisabaachievements.common.sql.DatabaseManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RewardService {
    public static final String ORIGIN_TARGET = "@origin";

    private final DatabaseManager databaseManager;

    public RewardService(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public @NotNull List<ProgressResult> unlock(
            @NotNull UUID playerId,
            @NotNull Collection<Key> keys,
            @NotNull String originServerId
    ) throws SQLException {
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }
        return databaseManager.use(connection -> {
            connection.setAutoCommit(false);
            try {
                List<ProgressResult> results = new ArrayList<>();
                for (Key key : keys) {
                    AchievementData achievement = getAchievementForUpdate(connection, key);
                    if (achievement == null) {
                        continue;
                    }
                    if (achievement.getFlags().contains(AchievementFlags.CATEGORY)
                            || achievement.getFlags().contains(AchievementFlags.UNOBTAINABLE)) {
                        continue;
                    }
                    long currentCount = getUnlockCountForUpdate(connection, playerId, achievement.getId());
                    if (currentCount >= achievement.getCount()) {
                        continue;
                    }
                    saveProgress(connection, playerId, achievement.getId(), achievement.getCount());
                    Set<String> notifiedServers = createDeliveries(connection, playerId, achievement, originServerId);
                    results.add(new ProgressResult(achievement, true, notifiedServers));
                }
                connection.commit();
                return results;
            } catch (SQLException | RuntimeException throwable) {
                connection.rollback();
                throw throwable;
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }

    private AchievementData getAchievementForUpdate(@NotNull Connection connection, @NotNull Key key) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT `id`, `key`, `count`, `point`, `hidden`, `flags` FROM `achievements` WHERE `key` = ? FOR UPDATE")) {
            statement.setString(1, key.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AchievementData(
                        rs.getLong("id"),
                        Key.key(rs.getString("key")),
                        rs.getLong("count"),
                        rs.getInt("point"),
                        AchievementHideFlags.values()[rs.getInt("hidden")],
                        MagicConstantBitField.of(AchievementFlags.class, rs.getInt("flags"))
                );
            }
        }
    }

    private long getUnlockCountForUpdate(
            @NotNull Connection connection,
            @NotNull UUID playerId,
            long achievementId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT `count` FROM `player_achievements` WHERE `player_id` = ? AND `achievement_id` = ? FOR UPDATE")) {
            statement.setString(1, playerId.toString());
            statement.setLong(2, achievementId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong("count") : 0L;
            }
        }
    }

    private void saveProgress(
            @NotNull Connection connection,
            @NotNull UUID playerId,
            long achievementId,
            long count
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO `player_achievements` (`player_id`, `achievement_id`, `count`) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE `count` = VALUES(`count`)")) {
            statement.setString(1, playerId.toString());
            statement.setLong(2, achievementId);
            statement.setLong(3, count);
            statement.executeUpdate();
        }
    }

    private @NotNull Set<String> createDeliveries(
            @NotNull Connection connection,
            @NotNull UUID playerId,
            @NotNull AchievementData achievement,
            @NotNull String originServerId
    ) throws SQLException {
        Set<String> targetServers = new LinkedHashSet<>();
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT r.`id`, r.`command`, t.`server_id` FROM `achievement_rewards` r " +
                        "JOIN `achievement_reward_targets` t ON t.`reward_id` = r.`id` " +
                        "WHERE r.`achievement_id` = ? AND r.`enabled` = 1 " +
                        "ORDER BY r.`execution_order`, r.`id`");
             PreparedStatement insert = connection.prepareStatement(
                     "INSERT IGNORE INTO `achievement_reward_deliveries` " +
                             "(`reward_id`, `player_id`, `achievement_key`, `target_server`, `command`) " +
                             "VALUES (?, ?, ?, ?, ?)")) {
            select.setLong(1, achievement.getId());
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    String configuredTarget = rs.getString("server_id");
                    String target = ORIGIN_TARGET.equals(configuredTarget) ? originServerId : configuredTarget;
                    if (target.isEmpty()) {
                        continue;
                    }
                    insert.setLong(1, rs.getLong("id"));
                    insert.setString(2, playerId.toString());
                    insert.setString(3, achievement.getKey().toString());
                    insert.setString(4, target);
                    insert.setString(5, rs.getString("command"));
                    if (insert.executeUpdate() > 0) {
                        targetServers.add(target);
                    }
                }
            }
        }
        return targetServers;
    }

    public @NotNull List<RewardDelivery> claim(
            @NotNull UUID playerId,
            @NotNull String serverId
    ) throws SQLException {
        return databaseManager.use(connection -> {
            connection.setAutoCommit(false);
            try {
                List<RewardDelivery> deliveries = new ArrayList<>();
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT `id`, `achievement_key`, `command` FROM `achievement_reward_deliveries` " +
                                "WHERE `player_id` = ? AND `target_server` = ? AND `status` = 'PENDING' " +
                                "ORDER BY `id` FOR UPDATE")) {
                    select.setString(1, playerId.toString());
                    select.setString(2, serverId);
                    try (ResultSet rs = select.executeQuery()) {
                        while (rs.next()) {
                            deliveries.add(new RewardDelivery(
                                    rs.getLong("id"),
                                    Key.key(rs.getString("achievement_key")),
                                    rs.getString("command")
                            ));
                        }
                    }
                }
                if (!deliveries.isEmpty()) {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE `achievement_reward_deliveries` SET `status` = 'CLAIMED', `claimed_at` = CURRENT_TIMESTAMP " +
                                    "WHERE `id` = ? AND `status` = 'PENDING'")) {
                        for (RewardDelivery delivery : deliveries) {
                            update.setLong(1, delivery.getId());
                            update.addBatch();
                        }
                        update.executeBatch();
                    }
                }
                connection.commit();
                return deliveries;
            } catch (SQLException | RuntimeException throwable) {
                connection.rollback();
                throw throwable;
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }

    public void complete(long deliveryId, boolean successful, @NotNull String error) throws SQLException {
        String storedError = error.length() > 1024 ? error.substring(0, 1024) : error;
        databaseManager.queryVoid(
                "UPDATE `achievement_reward_deliveries` SET `status` = ?, `error` = ?, `completed_at` = CURRENT_TIMESTAMP " +
                        "WHERE `id` = ? AND `status` = 'CLAIMED'",
                statement -> {
                    statement.setString(1, successful ? "SUCCEEDED" : "FAILED");
                    statement.setString(2, storedError.isEmpty() ? null : storedError);
                    statement.setLong(3, deliveryId);
                    statement.executeUpdate();
                }
        );
    }

    public long addReward(@NotNull Key key, @NotNull CollectionInput input) throws SQLException {
        return databaseManager.use(connection -> {
            connection.setAutoCommit(false);
            try {
                long achievementId;
                try (PreparedStatement find = connection.prepareStatement("SELECT `id` FROM `achievements` WHERE `key` = ?")) {
                    find.setString(1, key.toString());
                    try (ResultSet rs = find.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Achievement does not exist: " + key);
                        }
                        achievementId = rs.getLong("id");
                    }
                }
                long rewardId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO `achievement_rewards` (`achievement_id`, `command`) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, achievementId);
                    insert.setString(2, normalizeCommand(input.command));
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to obtain reward id");
                        }
                        rewardId = keys.getLong(1);
                    }
                }
                replaceTargets(connection, rewardId, input.targets);
                connection.commit();
                return rewardId;
            } catch (SQLException | RuntimeException throwable) {
                connection.rollback();
                throw throwable;
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }

    public void removeReward(long rewardId) throws SQLException {
        databaseManager.queryVoid("DELETE FROM `achievement_rewards` WHERE `id` = ?", statement -> {
            statement.setLong(1, rewardId);
            statement.executeUpdate();
        });
    }

    public void setEnabled(long rewardId, boolean enabled) throws SQLException {
        databaseManager.queryVoid("UPDATE `achievement_rewards` SET `enabled` = ? WHERE `id` = ?", statement -> {
            statement.setBoolean(1, enabled);
            statement.setLong(2, rewardId);
            statement.executeUpdate();
        });
    }

    public void setTargets(long rewardId, @NotNull Set<String> targets) throws SQLException {
        databaseManager.use(connection -> {
            connection.setAutoCommit(false);
            try {
                replaceTargets(connection, rewardId, targets);
                connection.commit();
            } catch (SQLException | RuntimeException throwable) {
                connection.rollback();
                throw throwable;
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }

    private void replaceTargets(
            @NotNull Connection connection,
            long rewardId,
            @NotNull Set<String> targets
    ) throws SQLException {
        validateTargets(targets);
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM `achievement_reward_targets` WHERE `reward_id` = ?")) {
            delete.setLong(1, rewardId);
            delete.executeUpdate();
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO `achievement_reward_targets` (`reward_id`, `server_id`) VALUES (?, ?)")) {
            for (String target : targets) {
                insert.setLong(1, rewardId);
                insert.setString(2, target);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    public @NotNull List<String> listRewards(@NotNull Key key) throws SQLException {
        return databaseManager.query(
                "SELECT r.`id`, r.`enabled`, r.`command`, GROUP_CONCAT(t.`server_id` ORDER BY t.`server_id`) AS targets " +
                        "FROM `achievement_rewards` r " +
                        "JOIN `achievements` a ON a.`id` = r.`achievement_id` " +
                        "LEFT JOIN `achievement_reward_targets` t ON t.`reward_id` = r.`id` " +
                        "WHERE a.`key` = ? GROUP BY r.`id`, r.`enabled`, r.`command` ORDER BY r.`execution_order`, r.`id`",
                statement -> {
                    statement.setString(1, key.toString());
                    List<String> rows = new ArrayList<>();
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            rows.add("#" + rs.getLong("id") + " [" + (rs.getBoolean("enabled") ? "enabled" : "disabled")
                                    + "] targets=" + rs.getString("targets") + " command=" + rs.getString("command"));
                        }
                    }
                    return rows;
                }
        );
    }

    public @NotNull List<String> listDeliveries(@NotNull String playerName) throws SQLException {
        return databaseManager.query(
                "SELECT d.`id`, d.`achievement_key`, d.`target_server`, d.`status`, d.`error` " +
                        "FROM `achievement_reward_deliveries` d JOIN `players` p ON p.`id` = d.`player_id` " +
                        "WHERE p.`name` = ? ORDER BY d.`id` DESC LIMIT 50",
                statement -> {
                    statement.setString(1, playerName);
                    List<String> rows = new ArrayList<>();
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            String error = rs.getString("error");
                            rows.add("#" + rs.getLong("id") + " " + rs.getString("achievement_key")
                                    + " server=" + rs.getString("target_server")
                                    + " status=" + rs.getString("status")
                                    + (error == null ? "" : " error=" + error));
                        }
                    }
                    return rows;
                }
        );
    }

    public void requeue(long deliveryId) throws SQLException {
        databaseManager.queryVoid(
                "UPDATE `achievement_reward_deliveries` SET `status` = 'PENDING', `error` = NULL, " +
                        "`claimed_at` = NULL, `completed_at` = NULL WHERE `id` = ? AND `status` IN ('CLAIMED', 'FAILED')",
                statement -> {
                    statement.setLong(1, deliveryId);
                    statement.executeUpdate();
                }
        );
    }

    public static @NotNull Set<String> parseTargets(@NotNull String value) {
        return new LinkedHashSet<>(Arrays.asList(value.split(",")));
    }

    private static void validateTargets(@NotNull Set<String> targets) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("At least one target is required");
        }
        for (String target : targets) {
            if (!target.equals(ORIGIN_TARGET) && !target.matches("^[a-zA-Z0-9._-]{1,64}$")) {
                throw new IllegalArgumentException("Invalid target server id: " + target);
            }
        }
    }

    private static @NotNull String normalizeCommand(@NotNull String command) {
        String normalized = command.startsWith("/") ? command.substring(1) : command;
        if (normalized.trim().isEmpty()) {
            throw new IllegalArgumentException("Command must not be empty");
        }
        return normalized;
    }

    public static final class CollectionInput {
        private final Set<String> targets;
        private final String command;

        public CollectionInput(@NotNull Set<String> targets, @NotNull String command) {
            this.targets = targets;
            this.command = command;
        }
    }

    public static final class ProgressResult {
        private final AchievementData achievement;
        private final boolean unlocked;
        private final Set<String> notifiedServers;

        public ProgressResult(
                @NotNull AchievementData achievement,
                boolean unlocked,
                @NotNull Set<String> notifiedServers
        ) {
            this.achievement = achievement;
            this.unlocked = unlocked;
            this.notifiedServers = notifiedServers;
        }

        public @NotNull AchievementData getAchievement() {
            return achievement;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public @NotNull Set<String> getNotifiedServers() {
            return notifiedServers;
        }
    }
}
