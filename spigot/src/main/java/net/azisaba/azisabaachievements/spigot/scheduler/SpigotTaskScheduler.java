package net.azisaba.azisabaachievements.spigot.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.TaskBuilder;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.jetbrains.annotations.NotNull;

public class SpigotTaskScheduler implements TaskScheduler {
    private final SpigotPlugin plugin;

    public SpigotTaskScheduler(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull TaskBuilder builder(@NotNull Runnable runnable) {
        return new SpigotTaskBuilder(runnable, plugin);
    }
}
