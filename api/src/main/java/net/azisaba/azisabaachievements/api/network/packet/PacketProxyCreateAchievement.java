package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketProxyCreateAchievement extends Packet<ProxyPacketListener> {
    private final UUID seq;
    private final Key key;
    private final long count;
    private final int point;

    public PacketProxyCreateAchievement(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.key = buf.readKey();
        this.count = buf.readLong();
        this.point = buf.readInt();
    }

    public PacketProxyCreateAchievement(@NotNull Key key, long count, int point) {
        super(PacketByteBuf.EMPTY);
        this.seq = UUID.randomUUID();
        this.key = key;
        this.count = count;
        this.point = point;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeKey(key);
        buf.writeLong(count);
        buf.writeInt(point);
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

    public long getCount() {
        return count;
    }

    public int getPoint() {
        return point;
    }
}
