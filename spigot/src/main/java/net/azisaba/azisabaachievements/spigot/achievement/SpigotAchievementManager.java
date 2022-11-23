package net.azisaba.azisabaachievements.spigot.achievement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpigotAchievementManager implements AchievementManager {
    public final Map<UUID, CompletableFuture<AchievementData>> achievementDataCallback = new Object2ObjectOpenHashMap<>();
    private final PacketSender packetSender;

    public SpigotAchievementManager(@NotNull PacketSender packetSender) {
        this.packetSender = packetSender;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull AchievementData> createAchievement(@NotNull Key key, int count, int point) {
        PacketProxyCreateAchievement packet = new PacketProxyCreateAchievement(key, count, point);
        CompletableFuture<AchievementData> future = new CompletableFuture<>();
        achievementDataCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        return future;
    }
}
