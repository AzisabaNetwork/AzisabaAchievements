package net.azisaba.azisabaachievements.api.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.TimeUnit;

/**
 * Represents a task builder which can be used to build and schedule a task.
 */
public interface TaskBuilder {
    /**
     * Sets the task to run synchronously.
     * @return this builder
     */
    @Contract(pure = true)
    TaskBuilder sync();

    /**
     * Sets the task to run asynchronously.
     * @return this builder
     */
    @Contract(pure = true)
    TaskBuilder async();

    /**
     * Sets the delay of the task. If the task is running in the minecraft thread, the delay is converted to ticks.
     * The unit of the delay is always converted to milliseconds, thus {@link TimeUnit#NANOSECONDS} and
     * {@link TimeUnit#MICROSECONDS} are not supported.
     * @param delay The delay in milliseconds
     * @param unit The unit of the delay
     * @return this builder
     * @throws IllegalArgumentException If the delay is negative
     */
    @Contract(pure = true)
    TaskBuilder delay(@Range(from = 0, to = Long.MAX_VALUE) long delay, @NotNull TimeUnit unit);

    /**
     * Sets the interval of the task. If the task is running in the minecraft thread, the interval is converted to ticks.
     * The unit of the interval is always converted to milliseconds, thus {@link TimeUnit#NANOSECONDS} and
     * {@link TimeUnit#MICROSECONDS} are not supported.
     * @param interval The interval in milliseconds
     * @param unit The unit of the interval
     * @return this builder
     * @throws IllegalArgumentException If the interval is negative
     */
    @Contract(pure = true)
    TaskBuilder repeat(@Range(from = 0, to = Long.MAX_VALUE) long interval, @NotNull TimeUnit unit);

    /**
     * Schedules the task.
     * @return The scheduled task
     */
    @Contract
    @NotNull
    ScheduledTask schedule();
}
