package net.azisaba.azisabaachievements.common.data;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class AchievementData {
    private final long id;
    private final Key key;
    private final int count;
    private final int point;

    @Contract(pure = true)
    public AchievementData(long id, @NotNull Key key, int count, int point) {
        this.id = id;
        this.key = Objects.requireNonNull(key, "key");
        this.count = count;
        this.point = point;
    }

    @Contract(pure = true)
    public long getId() {
        return id;
    }

    @Contract(pure = true)
    @NotNull
    public Key getKey() {
        return key;
    }

    @Contract(pure = true)
    public int getCount() {
        return count;
    }

    @Contract(pure = true)
    public int getPoint() {
        return point;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AchievementData)) return false;
        AchievementData that = (AchievementData) o;
        return id == that.id && count == that.count && point == that.point && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, count, point);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "AchievementData{" +
                "id=" + id +
                ", key=" + key +
                ", count=" + count +
                ", point=" + point +
                '}';
    }

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
