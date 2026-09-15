package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Sent from a Spigot server to the proxy when external progress has met the unlock criteria.
 */
public class PacketProxyUnlockAchievements extends Packet<ProxyPacketListener> {
    private final UUID playerId;
    private final String originServerId;
    private final List<Key> keys;

    public PacketProxyUnlockAchievements(@NotNull PacketByteBuf buf) {
        super(buf);
        this.playerId = buf.readUUID();
        this.originServerId = buf.readString();
        this.keys = buf.readCollection(ArrayList::new, PacketByteBuf::readKey);
    }

    public PacketProxyUnlockAchievements(
            @NotNull UUID playerId,
            @NotNull String originServerId,
            @NotNull Collection<Key> keys
    ) {
        super(PacketByteBuf.EMPTY);
        this.playerId = playerId;
        this.originServerId = originServerId;
        this.keys = new ArrayList<>(keys);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeString(originServerId);
        buf.writeCollection(keys, (key, b) -> b.writeKey(key));
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getOriginServerId() {
        return originServerId;
    }

    public @NotNull List<Key> getKeys() {
        return keys;
    }
}
