package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketProxyRequestPlayerData extends Packet<ProxyPacketListener> {
    private final UUID playerId;

    public PacketProxyRequestPlayerData(@NotNull PacketByteBuf buf) {
        super(buf);
        this.playerId = buf.readUUID();
    }

    public PacketProxyRequestPlayerData(@NotNull UUID playerId) {
        super(PacketByteBuf.EMPTY);
        this.playerId = playerId;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(this.playerId);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }
}
