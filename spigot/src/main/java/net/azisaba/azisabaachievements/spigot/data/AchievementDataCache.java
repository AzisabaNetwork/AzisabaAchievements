package net.azisaba.azisabaachievements.spigot.data;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestData;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerDataResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a cache of achievement data. All methods are thread-safe, but the returned collections might not be.
 */
public final class AchievementDataCache {
    private final AtomicReference<CompletableFuture<Void>> callback = new AtomicReference<>(null);
    private final Map<Key, TranslatedAchievement> achievements = new ConcurrentHashMap<>();
    private final Map<UUID, Set<PlayerAchievementData>> playerAchievements = new ConcurrentHashMap<>();
    private final AtomicInteger playerCount = new AtomicInteger();
    private final PacketSender packetSender;

    /**
     * Creates a new achievement data cache.
     * @param packetSender the packet sender
     */
    public AchievementDataCache(@NotNull PacketSender packetSender) {
        this.packetSender = packetSender;
    }

    /**
     * Requests the achievement data from the proxy. This sets the {@link #isPending()} to true. This operation
     * cannot be cancelled.
     * @return a future that completes when the achievement data is received. The future might end up being stuck
     *         forever if the data is never received from the proxy, so it is recommended to set a timeout using
     *         {@link CompletableFuture#get(long, TimeUnit)}. The future will never complete exceptionally.
     */
    @Contract
    public @NotNull CompletableFuture<Void> requestRefresh() {
        CompletableFuture<Void> future;
        if (callback.compareAndSet(null, new CompletableFuture<>())) {
            future = Objects.requireNonNull(callback.get(), "callback reference modified by another thread");
            packetSender.sendPacket(new PacketProxyRequestData());
        } else {
            future = callback.get();
            if (future == null) {
                return CompletableFuture.completedFuture(null);
            }
        }
        return future;
    }

    /**
     * Returns whether the achievement data is being updated in background (thus pending).
     * @return true if the achievement data is being updated (pending), false otherwise.
     */
    public boolean isPending() {
        return callback.get() != null;
    }

    /**
     * Syncs the achievement data with the data received from the proxy. This means that the data that was NOT received
     * from the proxy will be removed.
     * @param packet the packet
     */
    public void load(@NotNull PacketServerDataResult packet) {
        Map<Key, TranslatedAchievement> map = new HashMap<>();
        Map<Key, Map<String, AchievementTranslationData>> translations = new HashMap<>();
        for (AchievementTranslationData translation : packet.getAchievementTranslations()) {
            translations.computeIfAbsent(translation.getAchievementKey(), k -> new HashMap<>()).put(translation.getLanguage(), translation);
        }
        for (AchievementData achievement : packet.getAchievements()) {
            long unlockedCount =
                    packet.getUnlockedPlayers()
                            .stream()
                            .filter(e -> e.getKey().equals(achievement.getKey()))
                            .findAny()
                            .map(Map.Entry::getValue)
                            .orElse(0L);
            map.put(achievement.getKey(), new TranslatedAchievement(achievement, translations.getOrDefault(achievement.getKey(), Collections.emptyMap()), unlockedCount));
        }
        achievements.putAll(map);
        achievements.forEach((key, value) -> {
            if (!map.containsKey(key)) {
                achievements.remove(key);
            }
        });
        CompletableFuture<Void> future = callback.getAndSet(null);
        if (future != null) {
            future.complete(null);
        }
    }

    /**
     * Adds a single achievement to the cache.
     * @param achievement the achievement
     */
    public void add(@NotNull TranslatedAchievement achievement) {
        achievements.put(achievement.getData().getKey(), achievement);
    }

    /**
     * Adds a single player achievement data to the cache.
     * @param data the player achievement data
     */
    public void add(@NotNull PlayerAchievementData data) {
        Set<PlayerAchievementData> set = playerAchievements.computeIfAbsent(data.getPlayerId(), k -> Collections.synchronizedSet(new HashSet<>()));
        set.removeIf(d -> d.getAchievementKey().equals(data.getAchievementKey()));
        set.add(data);
    }

    /**
     * Returns the raw achievement data. The map is mutable.
     * @return the map
     */
    @Contract(pure = true)
    public @NotNull Map<Key, TranslatedAchievement> getAchievements() {
        return achievements;
    }

    /**
     * Returns the achievement with translation data. The list is mutable but the changes will <b>not</b> be reflected
     * in the cache.
     * @return the list
     */
    @Contract(" -> new")
    public @NotNull List<TranslatedAchievement> getAchievementsAsList() {
        return new ArrayList<>(achievements.values());
    }

    /**
     * Returns the player achievement data. The set is mutable and the changes will be reflected in the cache.
     * The returned set is a synchronized set.
     * @param uuid the player UUID
     * @return the set
     */
    @Contract(pure = true)
    public @NotNull Set<PlayerAchievementData> getPlayerAchievements(@NotNull UUID uuid) {
        return playerAchievements.computeIfAbsent(uuid, k -> Collections.synchronizedSet(new HashSet<>()));
    }

    /**
     * Returns a single achievement with translation data.
     * @param key the achievement key
     * @return the achievement
     * @throws NullPointerException if the achievement is not found
     */
    @Contract(pure = true)
    public @NotNull TranslatedAchievement getAchievement(@NotNull Key key) {
        return Objects.requireNonNull(achievements.get(key), "Achievement for key " + key + " is missing");
    }

    @Contract(pure = true)
    public int getPlayerCount() {
        return playerCount.get();
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount.set(playerCount);
    }
}
