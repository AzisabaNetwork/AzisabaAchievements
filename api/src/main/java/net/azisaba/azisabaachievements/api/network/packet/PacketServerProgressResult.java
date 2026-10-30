package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.util.Either;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This packet is sent from proxy to server to return the progress of a player stored in the database.
 * @see PacketProxyRequestProgress
 */
public class PacketServerProgressResult extends Packet<ServerPacketListener> {
    private final UUID seq;
    private final Either<String, List<PlayerAchievementData>> result;

    public PacketServerProgressResult(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.result = buf.readEither(
                PacketByteBuf::readString,
                b -> b.readWithCodec(PlayerAchievementData.CODEC.list()));
    }

    public PacketServerProgressResult(
            @NotNull UUID seq,
            @NotNull Either<@NotNull String, @NotNull List<PlayerAchievementData>> result
    ) {
        super(PacketByteBuf.EMPTY);
        this.seq = seq;
        this.result = result;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(seq);
        buf.writeEither(
                result,
                (s, b) -> b.writeString(s),
                (data, b) -> b.writeWithCodec(data, PlayerAchievementData.CODEC.list()));
    }

    @Override
    public void handle(@NotNull ServerPacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public UUID getSeq() {
        return seq;
    }

    @NotNull
    public Either<@NotNull String, @NotNull List<PlayerAchievementData>> getResult() {
        return result;
    }
}
