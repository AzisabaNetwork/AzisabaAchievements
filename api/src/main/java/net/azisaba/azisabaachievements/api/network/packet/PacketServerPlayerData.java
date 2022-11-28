package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class PacketServerPlayerData extends Packet<ServerPacketListener> {
    private final Collection<PlayerAchievementData> data;

    public PacketServerPlayerData(@NotNull PacketByteBuf buf) {
        super(buf);
        this.data = buf.readCollection(ArrayList::new, PacketByteBuf::readPlayerAchievementData);
    }

    public PacketServerPlayerData(@NotNull Collection<PlayerAchievementData> data) {
        super(PacketByteBuf.EMPTY);
        this.data = data;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeCollection(this.data, (data, b) -> b.writePlayerAchievementData(data));
    }

    @Override
    public void handle(@NotNull ServerPacketListener packetListener) {
        packetListener.handle(this);
    }

    /**
     * Returns the list of player achievement data. This collection might not contain all the data, so you should not
     * "sync" the data with the data you have. Instead, you should add entries to the existing data set.
     * @return the list
     */
    public @NotNull Collection<PlayerAchievementData> getData() {
        return data;
    }
}
