package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketProxyFetchAchievement extends Packet<ProxyPacketListener> {
    private final UUID seq;
    private final Key key;

    public PacketProxyFetchAchievement(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.key = buf.readKey();
    }

    public PacketProxyFetchAchievement(@NotNull Key key) {
        super(PacketByteBuf.EMPTY);
        this.seq = UUID.randomUUID();
        this.key = key;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeKey(key);
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
    public Key getKey() {
        return key;
    }
}
