package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AzisabaAchievementsProviderSetter {
    public static void setInstance(@NotNull AzisabaAchievements instance) {
        AzisabaAchievementsProvider.set(instance);
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public static <T extends AzisabaAchievements> @NotNull T getInstance() {
        return (T) AzisabaAchievementsProvider.get();
    }
}
