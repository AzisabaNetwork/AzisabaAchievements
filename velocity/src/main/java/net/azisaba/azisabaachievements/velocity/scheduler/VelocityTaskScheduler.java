package net.azisaba.azisabaachievements.velocity.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.TaskBuilder;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.velocity.plugin.VelocityPlugin;
import org.jetbrains.annotations.NotNull;

public class VelocityTaskScheduler implements TaskScheduler {
    private final VelocityPlugin plugin;

    public VelocityTaskScheduler(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull TaskBuilder builder(@NotNull Runnable runnable) {
        return new VelocityTaskBuilder(runnable, plugin);
    }
}
