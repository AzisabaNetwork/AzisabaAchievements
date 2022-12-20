package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.util.Either;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This packet is sent from proxy to server to return the result of creating an achievement.
 * @see PacketProxyCreateAchievement
 */
public class PacketServerCreateAchievementCallback extends Packet<ServerPacketListener> {
    private final UUID seq;
    private final Either<String, AchievementData> result;

    public PacketServerCreateAchievementCallback(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.result = buf.readEither(PacketByteBuf::readString, b -> b.readWithCodec(AchievementData.NETWORK_CODEC));
    }

    public PacketServerCreateAchievementCallback(@NotNull UUID seq, @NotNull Either<@NotNull String, @NotNull AchievementData> result) {
        super(PacketByteBuf.EMPTY);
        this.seq = seq;
        this.result = result;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(seq);
        buf.writeEither(result, (s, b) -> b.writeString(s), (data, b) -> b.writeWithCodec(data, AchievementData.NETWORK_CODEC));
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
    public Either<@NotNull String, @NotNull AchievementData> getResult() {
        return result;
    }
}
