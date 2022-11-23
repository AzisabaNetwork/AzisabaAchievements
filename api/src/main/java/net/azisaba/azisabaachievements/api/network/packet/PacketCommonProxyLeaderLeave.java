package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.PacketListener;
import org.jetbrains.annotations.NotNull;

public class PacketCommonProxyLeaderLeave extends Packet<PacketListener> {
    private final String serverId;

    public PacketCommonProxyLeaderLeave(@NotNull PacketByteBuf buf) {
        super(buf);
        this.serverId = buf.readString();
    }

    public PacketCommonProxyLeaderLeave(@NotNull String serverId) {
        super(PacketByteBuf.EMPTY);
        this.serverId = serverId;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeString(serverId);
    }

    @Override
    public void handle(@NotNull PacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public String getServerId() {
        return serverId;
    }
}
