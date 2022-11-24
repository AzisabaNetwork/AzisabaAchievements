package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

public class PacketProxyRequestData extends Packet<ProxyPacketListener> {
    public PacketProxyRequestData(@NotNull PacketByteBuf buf) {
        super(buf);
    }

    public PacketProxyRequestData() {
        super(PacketByteBuf.EMPTY);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }
}
