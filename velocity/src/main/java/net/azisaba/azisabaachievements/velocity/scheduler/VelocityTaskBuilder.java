package net.azisaba.azisabaachievements.velocity.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.ScheduledTask;
import net.azisaba.azisabaachievements.common.scheduler.AbstractTaskBuilder;
import net.azisaba.azisabaachievements.velocity.plugin.VelocityPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class VelocityTaskBuilder extends AbstractTaskBuilder {
    private final VelocityPlugin plugin;

    public VelocityTaskBuilder(@NotNull Runnable runnable, @NotNull VelocityPlugin plugin) {
        super(runnable);
        this.plugin = plugin;
    }

    @Override
    public @NotNull ScheduledTask schedule() {
        // there is no "sync" in velocity
        return new VelocityScheduledTask(
                plugin.getServer().getScheduler().buildTask(plugin, runnable)
                        .delay(delay, TimeUnit.MILLISECONDS)
                        .repeat(interval, TimeUnit.MILLISECONDS)
                        .schedule()
        );
    }
}
