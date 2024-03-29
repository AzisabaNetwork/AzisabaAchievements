package net.azisaba.azisabaachievements.common.sql;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import net.azisaba.azisabaachievements.common.data.PlayerData;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("PatternValidation") // Adding @Subst at every method is annoying
public class DataProvider {
    @Contract(pure = true)
    public static @Nullable Key getAchievementKeyById(@NotNull QueryExecutor queryExecutor, long id) {
        try {
            return queryExecutor.query("SELECT `key` FROM `achievements` WHERE `id` = ?", ps -> {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Key.key(rs.getString("key"));
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Contract(pure = true)
    public static long getAchievementIdByKey(@NotNull QueryExecutor queryExecutor, @NotNull Key key) {
        try {
            return queryExecutor.query("SELECT `id` FROM `achievements` WHERE `key` = ?", ps -> {
                ps.setString(1, key.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                    return -1L;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @Nullable
    public static AchievementData getAchievementById(@NotNull QueryExecutor queryExecutor, long id) {
        try {
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point`, `hidden`, `flags` FROM `achievements` WHERE `id` = ?", ps -> {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point"),
                                AchievementHideFlags.values()[rs.getInt("hidden")],
                                MagicConstantBitField.of(AchievementFlags.class, rs.getInt("flags"))
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
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point`, `hidden`, `flags` FROM `achievements` WHERE `key` = ?", ps -> {
                ps.setString(1, key.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point"),
                                AchievementHideFlags.values()[rs.getInt("hidden")],
                                MagicConstantBitField.of(AchievementFlags.class, rs.getInt("flags"))
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
    public static Set<PlayerData> getAllPlayers(@NotNull QueryExecutor queryExecutor) {
        try {
            return queryExecutor.query("SELECT `id`, `name` FROM `players`", ps -> {
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
            return queryExecutor.query("SELECT `id`, `key`, `count`, `point`, `hidden`, `flags` FROM `achievements`", ps -> {
                try (ResultSet rs = ps.executeQuery()) {
                    Set<AchievementData> set = new HashSet<>();
                    while (rs.next()) {
                        set.add(new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point"),
                                AchievementHideFlags.values()[rs.getInt("hidden")],
                                MagicConstantBitField.of(AchievementFlags.class, rs.getInt("flags"))
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
                        AchievementTranslationData data = new AchievementTranslationData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getString("lang"),
                                rs.getString("name"),
                                rs.getString("description")
                        );
                        set.add(data);
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
    public static Set<PlayerAchievementData> getPlayerAchievements(@NotNull QueryExecutor queryExecutor, @NotNull UUID uuid) {
        try {
            return queryExecutor.query("SELECT `player_achievements`.*, `achievements`.`key` FROM `player_achievements` " +
                    "LEFT JOIN `achievements` ON `player_achievements`.`achievement_id` = `achievements`.`id` " +
                    "WHERE `player_achievements`.`player_id` = ?", ps -> {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    Set<PlayerAchievementData> set = new HashSet<>();
                    while (rs.next()) {
                        set.add(new PlayerAchievementData(
                                uuid,
                                Key.key(rs.getString("key")),
                                rs.getLong("count")
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
    @Nullable
    public static PlayerAchievementData getPlayerAchievement(@NotNull QueryExecutor queryExecutor, @NotNull UUID uuid, @NotNull Key key) {
        try {
            return queryExecutor.query("SELECT `player_achievements`.count FROM `player_achievements` " +
                    "LEFT JOIN `achievements` ON `player_achievements`.`achievement_id` = `achievements`.`id` " +
                    "WHERE `player_achievements`.`player_id` = ? AND `achievements`.`key` = ?", ps -> {
                ps.setString(1, uuid.toString());
                ps.setString(2, key.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerAchievementData(uuid, key, rs.getLong("count"));
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
    public static @NotNull Set<@NotNull AchievementData> getChildAchievements(@NotNull QueryExecutor queryExecutor, @NotNull Key key) {
        try {
            return queryExecutor.query("SELECT * FROM `achievements` WHERE `key` LIKE ?", ps -> {
                ps.setString(1, key + "/%%");
                try (ResultSet rs = ps.executeQuery()) {
                    Set<AchievementData> set = new HashSet<>();
                    while (rs.next()) {
                        set.add(new AchievementData(
                                rs.getLong("id"),
                                Key.key(rs.getString("key")),
                                rs.getLong("count"),
                                rs.getInt("point"),
                                AchievementHideFlags.values()[rs.getInt("hidden")],
                                MagicConstantBitField.of(AchievementFlags.class, rs.getInt("flags"))
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
    public static int getPlayerCount(@NotNull QueryExecutor queryExecutor) {
        try {
            return queryExecutor.query("SELECT COUNT(*) FROM `players`", ps -> {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    public static int getUnlockedPlayerCount(@NotNull QueryExecutor queryExecutor, long achievementId) {
        try {
            return queryExecutor.query("SELECT COUNT(*) FROM `player_achievements` WHERE `achievement_id` = ? AND `count` >= (SELECT `count` FROM `achievements` WHERE `id` = ?)", ps -> {
                ps.setLong(1, achievementId);
                ps.setLong(2, achievementId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    public static @NotNull List<Map.Entry<Key, Long>> getUnlockedPlayerCounts(@NotNull QueryExecutor queryExecutor) {
        List<Map.Entry<Key, Long>> list = new ArrayList<>();
        try {
            queryExecutor.queryVoid("SELECT achievements.key, COUNT(*) FROM player_achievements " +
                    "LEFT JOIN achievements ON achievements.id = player_achievements.achievement_id " +
                    "WHERE player_achievements.count >= achievements.count GROUP BY achievement_id", ps -> {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new AbstractMap.SimpleImmutableEntry<>(Key.key(rs.getString(1)), rs.getLong(2)));
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
