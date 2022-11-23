package net.azisaba.azisabaachievements.velocity.plugin;

import net.azisaba.azisabaachievements.common.sql.DatabaseConfig;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PluginConfig {
    public final String redisHost;
    public final int redisPort;
    public final String redisUsername;
    public final String redisPassword;
    public final DatabaseConfig databaseConfig;

    public PluginConfig(@NotNull Path configPath) throws IOException {
        if (Files.notExists(configPath)) {
            try (InputStream configStream = VelocityPlugin.class.getClassLoader().getResourceAsStream("/config.yml")) {
                if (configStream != null) {
                    Files.copy(configStream, configPath);
                }
            }
        }
        ConfigurationNode node = YAMLConfigurationLoader.builder().setPath(configPath).build().load();
        this.redisHost = node.getNode("redis", "host").getString("localhost");
        this.redisPort = node.getNode("redis", "port").getInt(6379);
        this.redisUsername = node.getNode("redis", "username").getString();
        this.redisPassword = node.getNode("redis", "password").getString();
        this.databaseConfig = loadDatabaseConfig(node.getNode("database"));
    }

    @Contract("_ -> new")
    private @NotNull DatabaseConfig loadDatabaseConfig(@NotNull ConfigurationNode node) {
        String driver = node.getNode("driver").getString();
        String scheme = node.getNode("scheme").getString("jdbc:mariadb");
        String hostname = node.getNode("hostname").getString("localhost");
        int port = node.getNode("port").getInt(3306);
        String name = node.getNode("name").getString("azisabaachievements");
        String username = node.getNode("username").getString();
        String password = node.getNode("password").getString();
        Properties properties = new Properties();
        node.getNode("properties").getChildrenMap()
                .forEach((key, value) -> properties.setProperty(String.valueOf(key), value.getString()));
        return new DatabaseConfig(driver, scheme, hostname, port, name, username, password, properties);
    }
}
