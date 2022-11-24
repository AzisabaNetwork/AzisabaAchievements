package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.util.Either;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketServerProgressAchievementCallback extends Packet<ServerPacketListener> {
    private final UUID seq;
    private final Either<String, Boolean> result;

    public PacketServerProgressAchievementCallback(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.result = buf.readEither(PacketByteBuf::readString, PacketByteBuf::readBoolean);
    }

    public PacketServerProgressAchievementCallback(@NotNull UUID seq, @NotNull Either<@NotNull String, @NotNull Boolean> result) {
        super(PacketByteBuf.EMPTY);
        this.seq = seq;
        this.result = result;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(seq);
        buf.writeEither(result, (s, b) -> b.writeString(s), (data, b) -> b.writeBoolean(data));
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
    public Either<@NotNull String, @NotNull Boolean> getResult() {
        return result;
    }
}
