package net.azisaba.azisabaachievements.cli.commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider
import net.azisaba.azisabaachievements.api.Key
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked
import net.azisaba.azisabaachievements.api.network.packet.PacketServerPlayerData
import net.azisaba.azisabaachievements.cli.CLIMain
import net.azisaba.azisabaachievements.cli.Colors
import net.azisaba.azisabaachievements.cli.ExtraArgType
import net.azisaba.azisabaachievements.cli.Log
import net.azisaba.azisabaachievements.cli.achievement.CLIAchievementManager
import net.azisaba.azisabaachievements.cli.toColored
import net.azisaba.azisabaachievements.common.sql.DataProvider
import java.util.UUID

@OptIn(ExperimentalCli::class)
object CommandProgress : Subcommand("progress", "Updates the progress of an achievement") {
    private val uuid by option(ArgType.String, shortName = "p", description = "The UUID of the player").required()
    private val key by option(ArgType.String, shortName = "k", description = "The key of the achievement").required()
    private val count by option(ExtraArgType.Long, shortName = "c", description = "The amount of progress").required()

    override fun execute() {
        CLIMain.init()
        val uniqueId = UUID.fromString(uuid)
        val keyKey = Key.key(key)
        CLIAchievementManager.progressAchievement(uniqueId, keyKey, count)
            .whenComplete { result: Boolean?, throwable ->
                if (result == null || throwable != null) {
                    Log.error("Failed to update progress: ${throwable?.message}")
                    return@whenComplete
                }
                Log.info("Successfully progressed achievement: ${Colors.YELLOW}$key${Colors.RESET} (Unlocked: ${result.toColored()})")
                val playerAchievementData = DataProvider.getPlayerAchievement(CLIAchievementManager.queryExecutor, uniqueId, keyKey)
                if (playerAchievementData != null) {
                    AzisabaAchievementsProvider.get().packetSender.sendPacket(PacketServerPlayerData(setOf(playerAchievementData)))
                }
                if (result) {
                    val achievement = DataProvider.getAchievementByKey(CLIAchievementManager.queryExecutor, keyKey)
                        ?: throw AssertionError("Achievement $keyKey is missing")
                    AzisabaAchievementsProvider.get().packetSender
                        .sendPacket(PacketCommonAchievementUnlocked(uniqueId, achievement))
                }
            }
    }
}
