package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
}
