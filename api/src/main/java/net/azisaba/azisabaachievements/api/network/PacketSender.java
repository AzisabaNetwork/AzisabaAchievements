package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.NotNull;

public interface PacketSender {
    /**
     * Sends the packet.
     * @param packet the packet
     */
    void sendPacket(@NotNull Packet<?> packet);
}
