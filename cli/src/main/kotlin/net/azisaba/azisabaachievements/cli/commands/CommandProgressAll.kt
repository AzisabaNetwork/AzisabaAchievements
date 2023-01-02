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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCli::class)
object CommandProgressAll : Subcommand("progressAll", "Updates the progress of an achievement but affects everyone in the database") {
    private val key by option(ArgType.String, shortName = "k", description = "The key of the achievement").required()
    private val count by option(ExtraArgType.Long, shortName = "c", description = "The amount of progress").required()

    override fun execute() {
        CLIMain.init()
        val keyKey = Key.key(key)
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
        val start = System.nanoTime()
        val players = DataProvider.getAllPlayers(CLIAchievementManager.queryExecutor)
        players.forEach { pd ->
            executor.execute {
                CLIAchievementManager.progressAchievement(pd.id, keyKey, count)
                    .whenComplete { result: Boolean?, throwable ->
                        if (result == null || throwable != null) {
                            Log.error("Failed to update progress for ${pd.name}: ${throwable?.message}")
                            return@whenComplete
                        }
                        Log.info("Successfully progressed achievement for ${pd.name}: ${Colors.YELLOW}$key${Colors.RESET} (Unlocked: ${result.toColored()})")
                        val playerAchievementData =
                            DataProvider.getPlayerAchievement(CLIAchievementManager.queryExecutor, pd.id, keyKey)
                        if (playerAchievementData != null) {
                            AzisabaAchievementsProvider.get().packetSender
                                .sendPacket(PacketServerPlayerData(DataProvider.getPlayerCount(CLIAchievementManager.queryExecutor), setOf(playerAchievementData)))
                        }
                        if (result) {
                            val achievement =
                                DataProvider.getAchievementByKey(CLIAchievementManager.queryExecutor, keyKey)
                                    ?: throw AssertionError("Achievement $keyKey is missing")
                            AzisabaAchievementsProvider.get().packetSender
                                .sendPacket(PacketCommonAchievementUnlocked(pd.id, achievement))
                        }
                    }
            }
        }
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.MINUTES)
        val time = (System.nanoTime() - start) / 1_000_000.0
        Log.info("Processed ${players.size} players in $time ms")
    }
}
