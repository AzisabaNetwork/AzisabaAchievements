package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class AzisabaAchievementsProvider {
    private static final IllegalStateException NOT_INITIALIZED_ERROR =
            new IllegalStateException("API singleton not defined! Possible reasons:\n" +
                    "- You forgot to \"depend\" on AzisabaAchievements in your plugin.yml (or its equivalent)\n" +
                    "- Tried to access the API before the plugin was loaded");
    private static final AtomicReference<AzisabaAchievements> instance = new AtomicReference<>();

    @ApiStatus.Internal
    static void set(@NotNull AzisabaAchievements instance) {
        if (AzisabaAchievementsProvider.instance.get() != null) {
            throw new IllegalStateException("Cannot redefine API singleton");
        }
        AzisabaAchievementsProvider.instance.set(instance);
    }

    @Contract(pure = true)
    public static @NotNull AzisabaAchievements get() {
        AzisabaAchievements api = instance.get();
        if (api == null) {
            throw NOT_INITIALIZED_ERROR;
        }
        return api;
    }
}
