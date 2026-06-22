package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.reward.RewardDelivery;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class PacketServerRewardDeliveries extends Packet<ServerPacketListener> {
    private final UUID playerId;
    private final String serverId;
    private final List<RewardDelivery> deliveries;

    public PacketServerRewardDeliveries(@NotNull PacketByteBuf buf) {
        super(buf);
        playerId = buf.readUUID();
        serverId = buf.readString();
        deliveries = buf.readCollection(ArrayList::new, b ->
                new RewardDelivery(b.readLong(), b.readKey(), b.readString()));
    }

    public PacketServerRewardDeliveries(
            @NotNull UUID playerId,
            @NotNull String serverId,
            @NotNull Collection<RewardDelivery> deliveries
    ) {
        super(PacketByteBuf.EMPTY);
        this.playerId = playerId;
        this.serverId = serverId;
        this.deliveries = new ArrayList<>(deliveries);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeString(serverId);
        buf.writeCollection(deliveries, (delivery, b) -> {
            b.writeLong(delivery.getId());
            b.writeKey(delivery.getAchievementKey());
            b.writeString(delivery.getCommand());
        });
    }

    @Override
    public void handle(@NotNull ServerPacketListener packetListener) {
        packetListener.handle(this);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getServerId() {
        return serverId;
    }

    public @NotNull List<RewardDelivery> getDeliveries() {
        return deliveries;
    }
}
