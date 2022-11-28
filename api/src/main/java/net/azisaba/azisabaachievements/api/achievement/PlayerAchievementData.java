package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class PlayerAchievementData {
    private final UUID playerId;
    private final Key achievementKey;
    private final long count;

    public PlayerAchievementData(@NotNull UUID playerId, @NotNull Key achievementKey, long count) {
        this.playerId = playerId;
        this.achievementKey = achievementKey;
        this.count = count;
    }

    @Contract(pure = true)
    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }

    @Contract(pure = true)
    @NotNull
    public Key getAchievementKey() {
        return achievementKey;
    }

    @Contract(pure = true)
    public long getCount() {
        return count;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerAchievementData)) return false;
        PlayerAchievementData that = (PlayerAchievementData) o;
        return getCount() == that.getCount() && getPlayerId().equals(that.getPlayerId()) && getAchievementKey().equals(that.getAchievementKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerId(), getAchievementKey(), getCount());
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "PlayerAchievementData{" +
                "playerId=" + playerId +
                ", achievementKey=" + achievementKey +
                ", count=" + count +
                '}';
    }
}
