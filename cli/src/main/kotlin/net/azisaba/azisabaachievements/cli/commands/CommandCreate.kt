package net.azisaba.azisabaachievements.cli.commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import net.azisaba.azisabaachievements.api.Key
import net.azisaba.azisabaachievements.cli.CLIMain
import net.azisaba.azisabaachievements.cli.Colors
import net.azisaba.azisabaachievements.cli.ExtraArgType
import net.azisaba.azisabaachievements.cli.Log
import net.azisaba.azisabaachievements.cli.achievement.CLIAchievementManager

@OptIn(ExperimentalCli::class)
object CommandCreate : Subcommand("create", "Creates a new achievement") {
    private val key by option(ArgType.String, shortName = "k", description = "The key of the achievement").required()
    private val count by option(ExtraArgType.Long, shortName = "c", description = "The number of progress required to unlock the achievement").required()
    private val point by option(ArgType.Int, shortName = "p", description = "The point of the achievement").required()

    override fun execute() {
        CLIMain.init()
        val keyKey = Key.key(key)
        CLIAchievementManager.createAchievement(keyKey, count, point)
            .whenComplete { data, throwable ->
                if (throwable != null) {
                    Log.error("Failed to create achievement: ${throwable.message}")
                    return@whenComplete
                }
                Log.info("Created achievement: ${Colors.YELLOW}${data.key}${Colors.RESET} (ID: ${Colors.YELLOW}${data.id})")
            }
    }
}
