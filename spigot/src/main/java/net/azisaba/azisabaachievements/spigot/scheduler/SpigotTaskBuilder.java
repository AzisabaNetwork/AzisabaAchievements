package net.azisaba.azisabaachievements.spigot.scheduler;

import net.azisaba.azisabaachievements.api.scheduler.ScheduledTask;
import net.azisaba.azisabaachievements.common.scheduler.AbstractTaskBuilder;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class SpigotTaskBuilder extends AbstractTaskBuilder {
    private final SpigotPlugin plugin;

    public SpigotTaskBuilder(@NotNull Runnable runnable, @NotNull SpigotPlugin plugin) {
        super(runnable);
        this.plugin = plugin;
    }

    @Override
    public @NotNull ScheduledTask schedule() {
        if (syncMode == SyncMode.NOT_SET) {
            syncMode = SyncMode.SYNC;
        }
        if (syncMode == SyncMode.SYNC) {
            if (interval == 0) {
                if (delay == 0) {
                    return new SpigotScheduledTask(Bukkit.getScheduler().runTask(plugin, runnable));
                } else {
                    return new SpigotScheduledTask(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay / 50));
                }
            } else {
                return new SpigotScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay / 50, interval / 50));
            }
        } else {
            if (interval == 0) {
                if (delay == 0) {
                    return new SpigotScheduledTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
                } else {
                    return new SpigotScheduledTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay / 50));
                }
            } else {
                return new SpigotScheduledTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay / 50, interval / 50));
            }
        }
    }
}
