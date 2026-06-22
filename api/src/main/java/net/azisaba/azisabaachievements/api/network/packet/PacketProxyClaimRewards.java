package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PacketProxyClaimRewards extends Packet<ProxyPacketListener> {
    private final UUID playerId;
    private final String serverId;

    public PacketProxyClaimRewards(@NotNull PacketByteBuf buf) {
        super(buf);
        playerId = buf.readUUID();
        serverId = buf.readString();
    }

    public PacketProxyClaimRewards(@NotNull UUID playerId, @NotNull String serverId) {
        super(PacketByteBuf.EMPTY);
        this.playerId = playerId;
        this.serverId = serverId;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeString(serverId);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getServerId() {
        return serverId;
    }
}
