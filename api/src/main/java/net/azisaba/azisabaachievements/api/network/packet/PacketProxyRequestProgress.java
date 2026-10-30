package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This packet is sent from server to proxy to read the progress of a player stored in the database. Only used by the
 * built-in progress provider.
 * @see PacketServerProgressResult
 */
public class PacketProxyRequestProgress extends Packet<ProxyPacketListener> {
    private final UUID seq;
    private final UUID playerId;

    public PacketProxyRequestProgress(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.playerId = buf.readUUID();
    }

    public PacketProxyRequestProgress(@NotNull UUID playerId) {
        super(PacketByteBuf.EMPTY);
        this.seq = UUID.randomUUID();
        this.playerId = playerId;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(seq);
        buf.writeUUID(playerId);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public UUID getSeq() {
        return seq;
    }

    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }
}
