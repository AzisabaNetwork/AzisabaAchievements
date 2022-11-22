package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.NotNull;

public class AzisabaAchievementsProviderSetter {
    public static void setInstance(@NotNull AzisabaAchievements instance) {
        AzisabaAchievementsProvider.set(instance);
    }
}
