package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketProxyAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyFetchAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestData;
import org.jetbrains.annotations.NotNull;

public interface ProxyPacketListener extends PacketListener {
    void handle(@NotNull PacketProxyRequestData packet);
    void handle(@NotNull PacketProxyCreateAchievement packet);
    void handle(@NotNull PacketProxyFetchAchievement packet);
    void handle(@NotNull PacketProxyProgressAchievement packet);
    void handle(@NotNull PacketProxyAddAchievementTranslation packet);
}
