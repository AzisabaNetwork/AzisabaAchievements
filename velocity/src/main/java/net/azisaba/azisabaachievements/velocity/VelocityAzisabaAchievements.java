package net.azisaba.azisabaachievements.velocity;

import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.common.AbstractAzisabaAchievements;
import net.azisaba.azisabaachievements.velocity.achievement.VelocityAchievementManager;
import net.azisaba.azisabaachievements.velocity.plugin.VelocityPlugin;
import net.azisaba.azisabaachievements.velocity.scheduler.VelocityTaskScheduler;
import org.jetbrains.annotations.NotNull;

public class VelocityAzisabaAchievements extends AbstractAzisabaAchievements {
    private final TaskScheduler scheduler;
    private final VelocityAchievementManager achievementManager;

    public VelocityAzisabaAchievements(@NotNull VelocityPlugin plugin) {
        super(Logger.createByProxy(plugin.getLogger()), plugin, plugin.getJedisBox().getPubSubHandler());
        this.scheduler = new VelocityTaskScheduler(plugin);
        this.achievementManager = new VelocityAchievementManager(plugin.getDatabaseManager(), scheduler);
    }

    @NotNull
    @Override
    public TaskScheduler getScheduler() {
        return scheduler;
    }

    @NotNull
    @Override
    public VelocityAchievementManager getAchievementManager() {
        return achievementManager;
    }
}
