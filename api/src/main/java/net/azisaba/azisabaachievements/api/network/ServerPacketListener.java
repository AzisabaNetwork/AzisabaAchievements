package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketServerAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerDataResult;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerFetchAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerPlayerData;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressAchievementCallback;
import org.jetbrains.annotations.NotNull;

public interface ServerPacketListener extends PacketListener {
    void handle(@NotNull PacketServerDataResult packet);
    void handle(@NotNull PacketServerCreateAchievementCallback packet);
    void handle(@NotNull PacketServerFetchAchievementCallback packet);
    void handle(@NotNull PacketServerProgressAchievementCallback packet);
    void handle(@NotNull PacketServerAddAchievementTranslation packet);
    void handle(@NotNull PacketServerPlayerData packet);
}
