package net.azisaba.azisabaachievements.api.reward;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RewardDelivery {
    private final long id;
    private final Key achievementKey;
    private final String command;

    public RewardDelivery(long id, @NotNull Key achievementKey, @NotNull String command) {
        this.id = id;
        this.achievementKey = Objects.requireNonNull(achievementKey, "achievementKey");
        this.command = Objects.requireNonNull(command, "command");
    }

    public long getId() {
        return id;
    }

    public @NotNull Key getAchievementKey() {
        return achievementKey;
    }

    public @NotNull String getCommand() {
        return command;
    }
}
