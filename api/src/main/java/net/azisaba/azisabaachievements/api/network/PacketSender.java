package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.NotNull;

public interface PacketSender {
    void sendPacket(@NotNull Packet<?> packet);
}
