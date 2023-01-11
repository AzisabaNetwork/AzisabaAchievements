package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This packet is sent from proxy to server to return all achievement data from the proxy.
 * @see PacketProxyRequestData
 */
public class PacketServerDataResult extends Packet<ServerPacketListener> {
    private final List<AchievementData> achievements;
    private final List<AchievementTranslationData> achievementTranslations;
    private final List<Map.Entry<Key, Long>> unlockedPlayers;

    public PacketServerDataResult(@NotNull PacketByteBuf buf) {
        super(buf);
        this.achievements = buf.readWithCodec(AchievementData.NETWORK_CODEC.list());
        this.achievementTranslations = buf.readWithCodec(AchievementTranslationData.NETWORK_CODEC.list());
        this.unlockedPlayers = buf.readWithCodec(ExtraCodecs.entry(Key.CODEC, Codec.LONG).list());
    }

    public PacketServerDataResult(
            @NotNull Collection<AchievementData> achievements,
            @NotNull Collection<AchievementTranslationData> achievementTranslations,
            @NotNull List<Map.Entry<Key, Long>> unlockedPlayers
    ) {
        super(PacketByteBuf.EMPTY);
        this.achievements = new ArrayList<>(achievements);
        this.achievementTranslations = new ArrayList<>(achievementTranslations);
        this.unlockedPlayers = unlockedPlayers;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeWithCodec(this.achievements, AchievementData.NETWORK_CODEC.list());
        buf.writeWithCodec(this.achievementTranslations, AchievementTranslationData.NETWORK_CODEC.list());
        buf.writeWithCodec(this.unlockedPlayers, ExtraCodecs.entry(Key.CODEC, Codec.LONG).list());
    }

    @Override
    public void handle(@NotNull ServerPacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public Collection<AchievementData> getAchievements() {
        return achievements;
    }

    @NotNull
    public Collection<AchievementTranslationData> getAchievementTranslations() {
        return achievementTranslations;
    }

    public @NotNull List<Map.Entry<Key, Long>> getUnlockedPlayers() {
        return unlockedPlayers;
    }
}
