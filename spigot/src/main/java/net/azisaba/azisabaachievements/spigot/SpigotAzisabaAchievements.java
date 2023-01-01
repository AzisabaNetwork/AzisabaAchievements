package net.azisaba.azisabaachievements.spigot;

import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.common.AbstractAzisabaAchievements;
import net.azisaba.azisabaachievements.spigot.achievement.SpigotAchievementManager;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import net.azisaba.azisabaachievements.spigot.scheduler.SpigotTaskScheduler;
import org.jetbrains.annotations.NotNull;

public class SpigotAzisabaAchievements extends AbstractAzisabaAchievements {
    private final TaskScheduler scheduler;
    private final SpigotAchievementManager achievementManager;

    public SpigotAzisabaAchievements(@NotNull SpigotPlugin plugin) {
        super(Logger.createFromJavaLogger(plugin.getLogger()), plugin, plugin.getJedisBox().getPubSubHandler());
        this.scheduler = new SpigotTaskScheduler(plugin);
        this.achievementManager = new SpigotAchievementManager(getPacketSender(), plugin.getAchievementDataCache());
    }

    @NotNull
    @Override
    public TaskScheduler getScheduler() {
        return scheduler;
    }

    @NotNull
    @Override
    public SpigotAchievementManager getAchievementManager() {
        return achievementManager;
    }
}
