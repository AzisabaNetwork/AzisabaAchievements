package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketProxyProgressAchievement extends Packet<ProxyPacketListener> {
    private final UUID uuid;
    private final Key key;
    private final long count;

    public PacketProxyProgressAchievement(@NotNull PacketByteBuf buf) {
        super(buf);
        this.uuid = buf.readUUID();
        this.key = buf.readKey();
        this.count = buf.readLong();
    }

    public PacketProxyProgressAchievement(@NotNull UUID uuid, @NotNull Key key, long count) {
        super(PacketByteBuf.EMPTY);
        this.uuid = uuid;
        this.key = key;
        this.count = count;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeKey(key);
        buf.writeLong(count);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    /**
     * Returns the player uuid.
     * @return the uuid
     */
    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Returns the achievement key.
     * @return the key
     */
    @NotNull
    public Key getKey() {
        return key;
    }

    /**
     * Returns the progress count.
     * @return the count
     */
    public long getCount() {
        return count;
    }
}
