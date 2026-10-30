package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketProxyAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyClaimRewards;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCompleteRewardDelivery;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyFetchAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestData;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestPlayerData;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestProgress;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxySetAchievementProgress;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyUnlockAchievements;
import org.jetbrains.annotations.NotNull;

public interface ProxyPacketListener extends PacketListener {
    void handle(@NotNull PacketProxyRequestData packet);
    void handle(@NotNull PacketProxyCreateAchievement packet);
    void handle(@NotNull PacketProxyFetchAchievement packet);
    void handle(@NotNull PacketProxyProgressAchievement packet);
    void handle(@NotNull PacketProxyAddAchievementTranslation packet);
    void handle(@NotNull PacketProxyRequestPlayerData packet);
    void handle(@NotNull PacketProxyClaimRewards packet);
    void handle(@NotNull PacketProxyCompleteRewardDelivery packet);
    void handle(@NotNull PacketProxyUnlockAchievements packet);
    void handle(@NotNull PacketProxyRequestProgress packet);
    void handle(@NotNull PacketProxySetAchievementProgress packet);
}
