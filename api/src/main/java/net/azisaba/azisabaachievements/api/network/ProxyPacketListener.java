package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import org.jetbrains.annotations.NotNull;

public interface ProxyPacketListener extends PacketListener {
    void handle(@NotNull PacketProxyCreateAchievement packet);
}
