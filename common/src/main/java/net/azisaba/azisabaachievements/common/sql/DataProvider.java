package net.azisaba.azisabaachievements.common.sql;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.common.data.PlayerData;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DataProvider {
    @Contract(pure = true)
    @Nullable
    public static AchievementData getAchievementById(@NotNull QueryExecutor queryExecutor, long id) {
        try {
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point` FROM `achievements` WHERE `id` = ?", ps -> {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point")
                        );
                    } else {
                        return null;
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @Nullable
    public static AchievementData getAchievementByKey(@NotNull QueryExecutor queryExecutor, @NotNull Key key) {
        try {
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point` FROM `achievements` WHERE `key` = ?", ps -> {
                ps.setString(1, key.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point")
                        );
                    } else {
                        return null;
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @NotNull
    public static PlayerData getPlayerById(@NotNull QueryExecutor queryExecutor, @NotNull UUID id) {
        try {
            return queryExecutor.query("SELECT `name` FROM `players` WHERE `id` = ? LIMIT 1", ps -> {
                ps.setString(1, id.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return new PlayerData(id, rs.getString("name"));
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @NotNull
    public static Set<PlayerData> getPlayerByName(@NotNull QueryExecutor queryExecutor, @NotNull String name) {
        try {
            return queryExecutor.query("SELECT `id`, `name` FROM `players` WHERE `name` = ?", ps -> {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    Set<PlayerData> set = new HashSet<>();
                    while (rs.next()) {
                        set.add(new PlayerData(UUID.fromString(rs.getString("id")), rs.getString("name")));
                    }
                    return set;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @NotNull
    public static Set<AchievementData> getAllAchievements(@NotNull QueryExecutor queryExecutor) {
        try {
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point` FROM `achievements`", ps -> {
                try (ResultSet rs = ps.executeQuery()) {
                    Set<AchievementData> set = new HashSet<>();
                    while (rs.next()) {
                        set.add(new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point")
                        ));
                    }
                    return set;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @NotNull
    public static Set<AchievementTranslationData> getAllTranslations(@NotNull QueryExecutor queryExecutor) {
        try {
            return queryExecutor.query("SELECT `achievement_translations`.*, `achievements`.`key` FROM `achievement_translations` " +
                    "LEFT JOIN `achievements` ON `achievement_translations`.`id` = `achievements`.`id`", ps -> {
                try (ResultSet rs = ps.executeQuery()) {
                    Set<AchievementTranslationData> set = new HashSet<>();
                    while (rs.next()) {
                        set.add(new AchievementTranslationData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getString("lang"),
                                rs.getString("name"),
                                rs.getString("description")
                        ));
                    }
                    return set;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
