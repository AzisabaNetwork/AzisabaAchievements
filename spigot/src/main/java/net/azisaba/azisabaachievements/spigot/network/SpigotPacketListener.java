package net.azisaba.azisabaachievements.spigot.network;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.spigot.achievement.SpigotAchievementManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SpigotPacketListener implements ServerPacketListener {
    @Override
    public void handle(@NotNull PacketServerCreateAchievementCallback packet) {
        CompletableFuture<AchievementData> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .achievementDataCallback
                .remove(packet.getSeq());
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException(packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }
}
