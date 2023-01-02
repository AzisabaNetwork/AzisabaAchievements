package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This packet is sent from proxy to server to send the updated achievement progression data of a player.
 * @see PacketProxyRequestPlayerData
 */
public class PacketServerPlayerData extends Packet<ServerPacketListener> {
    private final int playerCount;
    private final List<PlayerAchievementData> data;

    public PacketServerPlayerData(@NotNull PacketByteBuf buf) {
        super(buf);
        this.playerCount = buf.readVarInt();
        this.data = buf.readWithCodec(PlayerAchievementData.CODEC.list());
    }

    public PacketServerPlayerData(int playerCount, @NotNull Collection<PlayerAchievementData> data) {
        super(PacketByteBuf.EMPTY);
        this.playerCount = playerCount;
        this.data = new ArrayList<>(data);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeVarInt(playerCount);
        buf.writeWithCodec(data, PlayerAchievementData.CODEC.list());
    }

    @Override
    public void handle(@NotNull ServerPacketListener packetListener) {
        packetListener.handle(this);
    }

    /**
     * Returns the number of players that have the player data in the database.
     * @return the player count
     */
    public int getPlayerCount() {
        return playerCount;
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
