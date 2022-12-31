package net.azisaba.azisabaachievements.api

import net.azisaba.azisabaachievements.cli.CLIAzisabaAchievements
import org.jetbrains.annotations.Contract

object AzisabaAchievementsProviderSetter {
    fun setInstance(instance: CLIAzisabaAchievements) {
        AzisabaAchievementsProvider.set(instance)
    }

    @Contract(pure = true)
    fun getInstance(): CLIAzisabaAchievements {
        return AzisabaAchievementsProvider.get() as CLIAzisabaAchievements
    }
}
