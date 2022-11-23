package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerFetchAchievementCallback;
import org.jetbrains.annotations.NotNull;

public interface ServerPacketListener extends PacketListener {
    void handle(@NotNull PacketServerCreateAchievementCallback packet);
    void handle(@NotNull PacketServerFetchAchievementCallback packet);
}
