package net.azisaba.azisabaachievements.spigot.achievement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.achievement.ProgressProvider;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyFetchAchievement;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SpigotAchievementManager implements AchievementManager {
    public final Map<UUID, CompletableFuture<AchievementData>> achievementDataCallback = new Object2ObjectOpenHashMap<>();
    public final Map<UUID, CompletableFuture<Optional<AchievementData>>> optionalAchievementDataCallback = new Object2ObjectOpenHashMap<>();
    public final Map<UUID, CompletableFuture<Boolean>> progressAchievementCallback = new Object2ObjectOpenHashMap<>();
    private final PacketSender packetSender;
    private final AchievementDataCache cache;
    private final ProgressSynchronizer progressSynchronizer;
    private final TaskScheduler scheduler;

    public SpigotAchievementManager(
            @NotNull PacketSender packetSender,
            @NotNull AchievementDataCache cache,
            @NotNull ProgressSynchronizer progressSynchronizer,
            @NotNull TaskScheduler scheduler
    ) {
        this.packetSender = packetSender;
        this.cache = cache;
        this.progressSynchronizer = progressSynchronizer;
        this.scheduler = scheduler;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull AchievementData> createAchievement(@NotNull Key key, long count, int point) {
        PacketProxyCreateAchievement packet = new PacketProxyCreateAchievement(key, count, point);
        CompletableFuture<AchievementData> future = new CompletableFuture<>();
        achievementDataCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Optional<AchievementData>> getAchievement(@NotNull Key key) {
        PacketProxyFetchAchievement packet = new PacketProxyFetchAchievement(key);
        CompletableFuture<Optional<AchievementData>> future = new CompletableFuture<>();
        optionalAchievementDataCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> progressAchievement(@NotNull UUID uuid, @NotNull Key key, long count) {
        if (count == 0L) {
            return CompletableFuture.completedFuture(false);
        }
        return updateProgress(uuid, key, provider -> provider.addProgress(uuid, key, count));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> setAchievementProgress(@NotNull UUID uuid, @NotNull Key key, long count) {
        if (count < 0L) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("count must not be negative"));
        }
        return updateProgress(uuid, key, provider -> provider.setProgress(uuid, key, count));
    }

    /**
     * Writes progress through the registered {@link ProgressProvider}, then syncs the cache and sends unlocks.
     * @return true if the achievement went from not met to met by this update
     */
    private @NotNull CompletableFuture<Boolean> updateProgress(
            @NotNull UUID uuid,
            @NotNull Key key,
            @NotNull Consumer<ProgressProvider> action
    ) {
        TranslatedAchievement achievement = cache.getAchievements().get(key);
        if (achievement == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Achievement does not exist"));
        }
        AchievementData data = achievement.getData();
        if (data.getFlags().contains(AchievementFlags.CATEGORY) || data.getFlags().contains(AchievementFlags.UNOBTAINABLE)) {
            return CompletableFuture.failedFuture(new IllegalStateException("Achievement is unobtainable"));
        }
        ProgressProvider provider = Bukkit.getServicesManager().load(ProgressProvider.class);
        if (provider == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("No ProgressProvider registered"));
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.builder(() -> {
            try {
                long before = getProgress(provider, uuid, key);
                action.accept(provider);
                long after = getProgress(provider, uuid, key);
                progressSynchronizer.sync(uuid);
                future.complete(before < data.getCount() && after >= data.getCount());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }).async().schedule();
        return future;
    }

    private static long getProgress(@NotNull ProgressProvider provider, @NotNull UUID uuid, @NotNull Key key) {
        Map<Key, Long> progress = provider.getProgress(uuid);
        if (progress == null) {
            return 0L;
        }
        return progress.getOrDefault(key, 0L);
    }

    @Override
    public @NotNull CompletableFuture<Set<AchievementData>> getChildAchievements(@NotNull Key key) {
        if (!cache.getAchievements().containsKey(key)) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }
        Set<AchievementData> achievements = new HashSet<>();
        for (TranslatedAchievement achievement : cache.getAchievementsAsList()) {
            if (achievement.getData().getKey().parent().equals(key)) {
                achievements.add(achievement.getData());
            }
        }
        return CompletableFuture.completedFuture(achievements);
    }

    @Override
    public void deleteAchievementBlocking(@NotNull Key key) {
        throw new UnsupportedOperationException(); // implementing delete request would be too dangerous
    }

    @Override
    public void deleteAchievementAsync(@NotNull Key key) {
        throw new UnsupportedOperationException(); // implementing delete request would be too dangerous
    }
}
