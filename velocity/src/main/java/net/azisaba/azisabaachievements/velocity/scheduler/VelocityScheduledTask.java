package net.azisaba.azisabaachievements.velocity.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

public class VelocityScheduledTask implements ScheduledTask {
    private final com.velocitypowered.api.scheduler.ScheduledTask task;

    public VelocityScheduledTask(@NotNull com.velocitypowered.api.scheduler.ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}
