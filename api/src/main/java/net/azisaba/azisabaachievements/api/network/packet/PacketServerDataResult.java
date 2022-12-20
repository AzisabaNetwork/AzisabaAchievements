package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This packet is sent from proxy to server to return all achievement data from the proxy.
 * @see PacketProxyRequestData
 */
public class PacketServerDataResult extends Packet<ServerPacketListener> {
    private final List<AchievementData> achievements;
    private final List<AchievementTranslationData> achievementTranslations;

    public PacketServerDataResult(@NotNull PacketByteBuf buf) {
        super(buf);
        this.achievements = buf.readWithCodec(AchievementData.NETWORK_CODEC.list());
        this.achievementTranslations = buf.readWithCodec(AchievementTranslationData.NETWORK_CODEC.list());
    }

    public PacketServerDataResult(
            @NotNull Collection<AchievementData> achievements,
            @NotNull Collection<AchievementTranslationData> achievementTranslations
    ) {
        super(PacketByteBuf.EMPTY);
        this.achievements = new ArrayList<>(achievements);
        this.achievementTranslations = new ArrayList<>(achievementTranslations);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeWithCodec(this.achievements, AchievementData.NETWORK_CODEC.list());
        buf.writeWithCodec(this.achievementTranslations, AchievementTranslationData.NETWORK_CODEC.list());
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
}
