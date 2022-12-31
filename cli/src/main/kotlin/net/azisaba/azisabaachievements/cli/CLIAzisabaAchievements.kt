package net.azisaba.azisabaachievements.cli

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProviderSetter
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair
import net.azisaba.azisabaachievements.api.network.PacketSender
import net.azisaba.azisabaachievements.cli.achievement.CLIAchievementManager
import net.azisaba.azisabaachievements.cli.scheduler.NoopTaskScheduler
import net.azisaba.azisabaachievements.common.AbstractAzisabaAchievements

class CLIAzisabaAchievements(
    packetRegistryPair: PacketRegistryPair,
    packetSender: PacketSender,
) : AbstractAzisabaAchievements(Log, packetRegistryPair, packetSender) {
    override fun getScheduler() = NoopTaskScheduler

    override fun getAchievementManager() = CLIAchievementManager

    init {
        AzisabaAchievementsProviderSetter.setInstance(this)
    }
}
