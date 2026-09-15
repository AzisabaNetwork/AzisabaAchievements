package net.azisaba.azisabaachievements.spigot.achievement;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.ProgressProvider;
import net.azisaba.azisabaachievements.api.achievement.UnlockEvaluator;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyUnlockAchievements;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import net.azisaba.azisabaachievements.spigot.event.AsyncEvent;
import net.azisaba.azisabaachievements.spigot.event.PlayerDataUpdatedEvent;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class ProgressSynchronizer {
    private final SpigotPlugin plugin;
    private final AtomicBoolean missingProviderWarned = new AtomicBoolean();

    public ProgressSynchronizer(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    public void sync(@NotNull UUID playerId) {
        ProgressProvider provider = Bukkit.getServicesManager().load(ProgressProvider.class);
        Map<Key, Long> progress;
        if (provider == null) {
            if (missingProviderWarned.compareAndSet(false, true)) {
                plugin.getLogger().warning("No ProgressProvider registered; player progress will be 0.");
            }
            progress = Collections.emptyMap();
        } else {
            Map<Key, Long> loaded = provider.getProgress(playerId);
            progress = loaded == null ? Collections.emptyMap() : loaded;
        }
        plugin.getAchievementDataCache().replacePlayerProgress(playerId, progress);
        List<AchievementData> definitions = plugin.getAchievementDataCache().getAchievementsAsList()
                .stream()
                .map(TranslatedAchievement::getData)
                .collect(Collectors.toList());
        List<Key> met = UnlockEvaluator.metKeys(definitions, progress);
        if (!met.isEmpty()) {
            plugin.getJedisBox().getPubSubHandler().sendPacket(new PacketProxyUnlockAchievements(
                    playerId,
                    plugin.getPluginConfig().rewardServerId,
                    met
            ));
        }
        AsyncEvent.call(new PlayerDataUpdatedEvent(new ArrayList<>(
                plugin.getAchievementDataCache().getPlayerAchievements(playerId))));
    }

    public void syncAsync(@NotNull UUID playerId) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> sync(playerId));
    }
}
