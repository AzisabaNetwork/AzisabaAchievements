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
import java.util.concurrent.atomic.AtomicReference;

public final class AchievementDataCache {
    private final AtomicReference<CompletableFuture<Void>> callback = new AtomicReference<>(null);
    private final Map<Key, TranslatedAchievement> achievements = new ConcurrentHashMap<>();
    private final Map<UUID, Set<PlayerAchievementData>> playerAchievements = new ConcurrentHashMap<>();
    private final PacketSender packetSender;

    public AchievementDataCache(@NotNull PacketSender packetSender) {
        this.packetSender = packetSender;
    }

    @Contract
    public @NotNull CompletableFuture<Void> requestRefresh() {
        CompletableFuture<Void> future;
        if (callback.compareAndSet(null, new CompletableFuture<>())) {
            future = callback.get();
            packetSender.sendPacket(new PacketProxyRequestData());
        } else {
            future = callback.get();
            if (future == null) {
                return CompletableFuture.completedFuture(null);
            }
        }
        return future;
    }

    public boolean isPending() {
        return callback.get() != null;
    }

    public void load(@NotNull PacketServerDataResult packet) {
        Map<Key, TranslatedAchievement> map = new HashMap<>();
        Map<Key, Map<String, AchievementTranslationData>> translations = new HashMap<>();
        for (AchievementTranslationData translation : packet.getAchievementTranslations()) {
            translations.computeIfAbsent(translation.getAchievementKey(), k -> new HashMap<>()).put(translation.getLanguage(), translation);
        }
        for (AchievementData achievement : packet.getAchievements()) {
            map.put(achievement.getKey(), new TranslatedAchievement(achievement, translations.get(achievement.getKey())));
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

    public void add(@NotNull TranslatedAchievement achievement) {
        achievements.put(achievement.getData().getKey(), achievement);
    }

    public void add(@NotNull PlayerAchievementData data) {
        Set<PlayerAchievementData> set = playerAchievements.computeIfAbsent(data.getPlayerId(), k -> Collections.synchronizedSet(new HashSet<>()));
        set.removeIf(d -> d.getAchievementKey().equals(data.getAchievementKey()));
        set.add(data);
    }

    @Contract(pure = true)
    public @NotNull Map<Key, TranslatedAchievement> getAchievements() {
        return achievements;
    }

    @Contract(" -> new")
    public @NotNull List<TranslatedAchievement> getAchievementsAsList() {
        return new ArrayList<>(achievements.values());
    }

    @Contract(pure = true)
    public @NotNull Set<PlayerAchievementData> getPlayerAchievements(@NotNull UUID uuid) {
        return playerAchievements.getOrDefault(uuid, Collections.emptySet());
    }

    @Contract(pure = true)
    public @NotNull TranslatedAchievement getAchievement(@NotNull Key key) {
        return Objects.requireNonNull(achievements.get(key), "Achievement for key " + key + " is missing");
    }
}
