package net.azisaba.azisabaachievements.velocity;

import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.common.AbstractAzisabaAchievements;
import net.azisaba.azisabaachievements.velocity.plugin.VelocityPlugin;
import org.jetbrains.annotations.NotNull;

public class VelocityAzisabaAchievements extends AbstractAzisabaAchievements {
    public VelocityAzisabaAchievements(@NotNull VelocityPlugin plugin) {
        super(Logger.createByProxy(plugin.getLogger()), plugin, plugin.getJedisBox().getPubSubHandler());
    }
}
