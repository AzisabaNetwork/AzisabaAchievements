package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import org.jetbrains.annotations.NotNull;

/**
 * This packet is sent from server to proxy to add a new translation for an achievement.
 * @see PacketServerAddAchievementTranslation
 */
public class PacketProxyAddAchievementTranslation extends Packet<ProxyPacketListener> {
    private final AchievementTranslationData data;

    public PacketProxyAddAchievementTranslation(@NotNull PacketByteBuf buf) {
        super(buf);
        data = buf.readWithCodec(AchievementTranslationData.NETWORK_CODEC);
    }

    public PacketProxyAddAchievementTranslation(
            @NotNull Key achievementKey,
            @NotNull String language,
            @NotNull String name,
            @NotNull String description
    ) {
        super(PacketByteBuf.EMPTY);
        data = new AchievementTranslationData(-1, achievementKey, language, name, description);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeWithCodec(data, AchievementTranslationData.NETWORK_CODEC);
    }

    @Override
    public void handle(@NotNull ProxyPacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public AchievementTranslationData getData() {
        return data;
    }
}
