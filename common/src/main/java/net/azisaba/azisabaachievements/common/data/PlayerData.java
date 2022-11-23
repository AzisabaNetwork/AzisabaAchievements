package net.azisaba.azisabaachievements.common.data;

import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PlayerData {
    private final UUID id;
    private final String name;

    public PlayerData(@NotNull UUID id, @NotNull String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Contract(pure = true)
    @NotNull
    public UUID getId() {
        return id;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return name;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerData)) return false;
        PlayerData that = (PlayerData) o;
        return id.equals(that.id) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "PlayerData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Contract(pure = true)
    @NotNull
    public static PlayerData getById(@NotNull QueryExecutor queryExecutor, @NotNull UUID id) {
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
    public static Set<PlayerData> getByName(@NotNull QueryExecutor queryExecutor, @NotNull String name) {
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
}
