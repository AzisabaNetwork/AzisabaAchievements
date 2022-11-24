package net.azisaba.azisabaachievements.api.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TaskScheduler {
    /**
     * Creates the task builder.
     * @param runnable The task to run.
     * @return The task builder.
     * @see TaskBuilder
     */
    @Contract(pure = true)
    @NotNull
    TaskBuilder builder(@NotNull Runnable runnable);
}
