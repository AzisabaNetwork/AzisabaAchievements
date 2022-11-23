package net.azisaba.azisabaachievements.common.sql;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataProvider {
    @Contract(pure = true)
    @Nullable
    public static AchievementData getById(@NotNull QueryExecutor queryExecutor, long id) {
        try {
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point` FROM `achievements` WHERE `id` = ?", ps -> {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getInt("count"),
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
    public static AchievementData getByKey(@NotNull QueryExecutor queryExecutor, @NotNull Key key) {
        try {
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point` FROM `achievements` WHERE `key` = ?", ps -> {
                ps.setString(1, key.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getInt("count"),
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
}
