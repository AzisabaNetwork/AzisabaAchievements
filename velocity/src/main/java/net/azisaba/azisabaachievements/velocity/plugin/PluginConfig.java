package net.azisaba.azisabaachievements.velocity.plugin;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginConfig {
    public final String redisHost;
    public final int redisPort;
    public final String redisUsername;
    public final String redisPassword;

    public PluginConfig(@NotNull Path configPath) throws IOException {
        if (Files.notExists(configPath)) {
            Files.createFile(configPath);
        }
        ConfigurationNode node = YAMLConfigurationLoader.builder().setPath(configPath).build().load();
        this.redisHost = node.getNode("redis", "host").getString("localhost");
        this.redisPort = node.getNode("redis", "port").getInt(6379);
        this.redisUsername = node.getNode("redis", "username").getString();
        this.redisPassword = node.getNode("redis", "password").getString();
    }
}
