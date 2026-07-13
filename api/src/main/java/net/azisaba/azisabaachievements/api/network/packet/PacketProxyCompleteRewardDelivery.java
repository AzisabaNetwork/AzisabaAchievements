package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

public final class PacketProxyCompleteRewardDelivery extends Packet<ProxyPacketListener> {
    private final long deliveryId;
    private final boolean successful;
    private final String error;

    public PacketProxyCompleteRewardDelivery(@NotNull PacketByteBuf buf) {
        super(buf);
        deliveryId = buf.readLong();
        successful = buf.readBoolean();
        error = buf.readString();
    }

    public PacketProxyCompleteRewardDelivery(long deliveryId, boolean successful, @NotNull String error) {
        super(PacketByteBuf.EMPTY);
        this.deliveryId = deliveryId;
        this.successful = successful;
        this.error = error;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeLong(deliveryId);
        buf.writeBoolean(successful);
        buf.writeString(error);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    public long getDeliveryId() {
        return deliveryId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public @NotNull String getError() {
        return error;
    }
}
