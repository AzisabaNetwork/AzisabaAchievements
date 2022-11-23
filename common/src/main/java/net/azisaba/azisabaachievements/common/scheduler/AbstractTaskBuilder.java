package net.azisaba.azisabaachievements.common.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.TaskBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public abstract class AbstractTaskBuilder implements TaskBuilder {
    protected final Runnable runnable;
    protected SyncMode syncMode = SyncMode.NOT_SET;
    protected long delay = 0;
    protected long interval = 0;

    public AbstractTaskBuilder(@NotNull Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public TaskBuilder sync() {
        syncMode = SyncMode.SYNC;
        return this;
    }

    @Override
    public TaskBuilder async() {
        syncMode = SyncMode.ASYNC;
        return this;
    }

    @Override
    public TaskBuilder delay(long delay, @NotNull TimeUnit unit) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must be positive");
        }
        this.delay = unit.toMillis(delay);
        return this;
    }

    @Override
    public TaskBuilder repeat(long interval, @NotNull TimeUnit unit) {
        if (interval < 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }
        this.interval = unit.toMillis(interval);
        return this;
    }

    public enum SyncMode {
        NOT_SET,
        SYNC,
        ASYNC,
    }
}
