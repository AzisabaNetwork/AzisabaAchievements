package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This packet is sent from proxy to server to return all achievement data from the proxy.
 * @see PacketProxyRequestData
 */
public class PacketServerDataResult extends Packet<ServerPacketListener> {
    private final Collection<AchievementData> achievements;
    private final Collection<AchievementTranslationData> achievementTranslations;

    public PacketServerDataResult(@NotNull PacketByteBuf buf) {
        super(buf);
        this.achievements = buf.readCollection(ArrayList::new, PacketByteBuf::readAchievementData);
        this.achievementTranslations = buf.readCollection(ArrayList::new, PacketByteBuf::readAchievementTranslationData);
    }

    public PacketServerDataResult(
            @NotNull Collection<AchievementData> achievements,
            @NotNull Collection<AchievementTranslationData> achievementTranslations
    ) {
        super(PacketByteBuf.EMPTY);
        this.achievements = achievements;
        this.achievementTranslations = achievementTranslations;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeCollection(this.achievements, (data, b) -> b.writeAchievementData(data));
        buf.writeCollection(this.achievementTranslations, (data, b) -> b.writeAchievementTranslationData(data));
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
