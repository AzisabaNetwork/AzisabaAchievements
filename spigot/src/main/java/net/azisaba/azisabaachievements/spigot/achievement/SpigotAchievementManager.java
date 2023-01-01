package net.azisaba.azisabaachievements.spigot.achievement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyFetchAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpigotAchievementManager implements AchievementManager {
    public final Map<UUID, CompletableFuture<AchievementData>> achievementDataCallback = new Object2ObjectOpenHashMap<>();
    public final Map<UUID, CompletableFuture<Optional<AchievementData>>> optionalAchievementDataCallback = new Object2ObjectOpenHashMap<>();
    public final Map<UUID, CompletableFuture<Boolean>> progressAchievementCallback = new Object2ObjectOpenHashMap<>();
    private final PacketSender packetSender;
    private final AchievementDataCache cache;

    public SpigotAchievementManager(@NotNull PacketSender packetSender, @NotNull AchievementDataCache cache) {
        this.packetSender = packetSender;
        this.cache = cache;
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
        PacketProxyProgressAchievement packet = new PacketProxyProgressAchievement(uuid, key, count);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        progressAchievementCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        return future;
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
