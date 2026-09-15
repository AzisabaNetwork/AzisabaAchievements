package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Source of per-player achievement progress. Register an implementation with Bukkit Services:
 * {@code Bukkit.getServicesManager().register(ProgressProvider.class, impl, plugin, ServicePriority.Normal)}.
 * Keys are this plugin's achievement keys (for example {@code azisaba:general/foo}). Missing keys are treated as 0.
 */
public interface ProgressProvider {
    @NotNull Map<Key, Long> getProgress(@NotNull UUID playerId);
}
