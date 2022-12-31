package net.azisaba.azisabaachievements.cli.config

import net.azisaba.azisabaachievements.cli.Log
import net.azisaba.azisabaachievements.common.sql.DatabaseConfig
import org.jetbrains.annotations.Contract
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.absolutePathString

class PluginConfig(configPath: Path) {
    val redisHost: String
    val redisPort: Int
    val redisUsername: String?
    val redisPassword: String?
    val databaseConfig: DatabaseConfig

    init {
        Log.debug("Attempting to load config from ${configPath.absolutePathString()}")
        if (Files.notExists(configPath)) {
            PluginConfig::class.java.classLoader.getResourceAsStream("/config.yml").use { configStream ->
                if (configStream != null) {
                    Files.copy(configStream, configPath)
                    Log.info("Created config file at ${configPath.absolutePathString()}")
                } else {
                    Log.warn("Default config.yml is missing")
                }
            }
        }
        val node = YamlConfigurationLoader.builder().path(configPath).build().load()
        redisHost = node.node("redis", "host").getString("localhost")
        redisPort = node.node("redis", "port").getInt(6379)
        redisUsername = node.node("redis", "username").string
        redisPassword = node.node("redis", "password").string
        databaseConfig = loadDatabaseConfig(node.node("database"))
    }

    @Contract("_ -> new")
    private fun loadDatabaseConfig(node: ConfigurationNode): DatabaseConfig {
        val driver = node.node("driver").string
        val scheme = node.node("scheme").getString("jdbc:mariadb")
        val hostname = node.node("hostname").getString("localhost")
        val port = node.node("port").getInt(3306)
        val name = node.node("name").getString("azisabaachievements")
        val username = node.node("username").string
        val password = node.node("password").string
        val properties = Properties()
        node.node("properties")
            .childrenMap()
            .forEach { (key: Any, value: ConfigurationNode) -> properties.setProperty(key.toString(), value.string) }
        return DatabaseConfig(driver, scheme, hostname, port, name, username, password, properties)
    }
}
