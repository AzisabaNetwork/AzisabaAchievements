package net.azisaba.azisabaachievements.spigot.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class SpigotScheduledTask implements ScheduledTask {
    private final BukkitTask task;

    public SpigotScheduledTask(@NotNull BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}
