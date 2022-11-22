package net.azisaba.azisabaachievements.spigot.plugin;

import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;

public class PluginConfig {
    public final String redisHost;
    public final int redisPort;
    public final String redisUsername;
    public final String redisPassword;

    public PluginConfig(@NotNull MemoryConfiguration config) {
        this.redisHost = config.getString("redis.host", "localhost");
        this.redisPort = config.getInt("redis.port", 6379);
        this.redisUsername = config.getString("redis.username");
        this.redisPassword = config.getString("redis.password");
    }
}
