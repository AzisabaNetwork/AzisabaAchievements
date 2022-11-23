package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.util.Either;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PacketServerFetchAchievementCallback extends Packet<ServerPacketListener> {
    private final UUID seq;
    private final Either<String, Optional<AchievementData>> result;

    public PacketServerFetchAchievementCallback(@NotNull PacketByteBuf buf) {
        super(buf);
        this.seq = buf.readUUID();
        this.result = buf.readEither(PacketByteBuf::readString, b -> {
            if (b.readBoolean()) {
                Key key = b.readKey();
                int count = b.readInt();
                int point = b.readInt();
                return Optional.of(new AchievementData(0, key, count, point));
            } else {
                return Optional.empty();
            }
        });
    }

    public PacketServerFetchAchievementCallback(@NotNull UUID seq, @NotNull Either<@NotNull String, @NotNull Optional<AchievementData>> result) {
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
                (data, b) -> {
                    if (data.isPresent()) {
                        b.writeBoolean(true);
                        b.writeKey(data.get().getKey());
                        b.writeInt(data.get().getCount());
                        b.writeInt(data.get().getPoint());
                    } else {
                        b.writeBoolean(false);
                    }
                }
        );
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
    public Either<@NotNull String, @NotNull Optional<AchievementData>> getResult() {
        return result;
    }
}
