package net.azisaba.azisabaachievements.spigot;

import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.common.AbstractAzisabaAchievements;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.jetbrains.annotations.NotNull;

public class SpigotAzisabaAchievements extends AbstractAzisabaAchievements {
    public SpigotAzisabaAchievements(@NotNull SpigotPlugin plugin) {
        super(Logger.createFromJavaLogger(plugin.getLogger()), plugin, plugin.getJedisBox().getPubSubHandler());
    }
}
