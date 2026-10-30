package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This packet is sent from server to proxy to set the progress of an achievement for a player. Only used by the
 * built-in progress provider; the proxy stores the progress in the database.
 * @see net.azisaba.azisabaachievements.api.achievement.AchievementManager#setAchievementProgress(UUID, Key, long)
 * @see PacketServerProgressAchievementCallback
 */
public class PacketProxySetAchievementProgress extends Packet<ProxyPacketListener> {
    private final UUID seq;
    private final UUID uuid;
    private final Key key;
    private final long count;
    private final String originServerId;

    public PacketProxySetAchievementProgress(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.uuid = buf.readUUID();
        this.key = buf.readKey();
        this.count = buf.readLong();
        this.originServerId = buf.readString();
    }

    public PacketProxySetAchievementProgress(@NotNull UUID uuid, @NotNull Key key, long count, @NotNull String originServerId) {
        super(PacketByteBuf.EMPTY);
        this.seq = UUID.randomUUID();
        this.uuid = uuid;
        this.key = key;
        this.count = count;
        this.originServerId = originServerId;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(seq);
        buf.writeUUID(uuid);
        buf.writeKey(key);
        buf.writeLong(count);
        buf.writeString(originServerId);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public UUID getSeq() {
        return seq;
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
     * Returns the new progress count.
     * @return the count
     */
    public long getCount() {
        return count;
    }

    public @NotNull String getOriginServerId() {
        return originServerId;
    }
}
