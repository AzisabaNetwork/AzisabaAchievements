package net.azisaba.azisabaachievements.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default
import net.azisaba.azisabaachievements.api.Side
import net.azisaba.azisabaachievements.api.network.PacketRegistry
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair
import net.azisaba.azisabaachievements.cli.achievement.CLIAchievementManager
import net.azisaba.azisabaachievements.cli.commands.CommandCreate
import net.azisaba.azisabaachievements.cli.commands.CommandDumpProtocol
import net.azisaba.azisabaachievements.cli.commands.CommandProgress
import net.azisaba.azisabaachievements.cli.commands.CommandProgressAll
import net.azisaba.azisabaachievements.cli.config.PluginConfig
import net.azisaba.azisabaachievements.cli.network.CLIPacketListener
import net.azisaba.azisabaachievements.common.network.PacketRegistryImpl
import net.azisaba.azisabaachievements.common.redis.JedisBox
import net.azisaba.azisabaachievements.common.sql.DatabaseManager
import java.nio.file.Paths

@OptIn(ExperimentalCli::class)
object CLIMain {
    private val parser = ArgParser("AzisabaAchievements", prefixStyle = ArgParser.OptionPrefixStyle.GNU)
    private val configPath by parser.option(ArgType.String, shortName = "c", description = "Config file path").default("config.yml")
    lateinit var api : CLIAzisabaAchievements

    init {
        Log
        parser.subcommands(
            CommandDumpProtocol,
            CommandCreate,
            CommandProgress,
            CommandProgressAll,
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // execute command
            parser.parse(args)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun init() {
        val config = PluginConfig(Paths.get(configPath))
        val clientRegistry = PacketRegistryImpl().apply {
            registerCommonPackets()
            registerProxyBoundPackets()
        }
        val serverRegistry = PacketRegistryImpl().apply {
            registerCommonPackets()
            registerServerBoundPackets()
        }
        val pair = object : PacketRegistryPair {
            override fun getClientRegistry(): PacketRegistry = clientRegistry

            override fun getServerRegistry(): PacketRegistry = serverRegistry
        }
        val jedisBox =
            JedisBox(
                Side.PROXY,
                Log,
                CLIPacketListener,
                pair,
                config.redisHost,
                config.redisPort,
                config.redisUsername,
                config.redisPassword,
            )
        val databaseManager = DatabaseManager(config.databaseConfig.createDataSource())
        CLIAchievementManager.queryExecutor = databaseManager
        api = CLIAzisabaAchievements(pair, jedisBox.pubSubHandler)
    }
}
