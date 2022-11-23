package net.azisaba.azisabaachievements.api.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TaskScheduler {
    @Contract(pure = true)
    @NotNull
    TaskBuilder builder(@NotNull Runnable runnable);
}
