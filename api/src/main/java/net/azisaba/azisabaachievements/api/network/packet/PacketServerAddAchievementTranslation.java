package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import org.jetbrains.annotations.NotNull;

/**
 * This packet is sent from proxy to server to notify the server that a new translation for an achievement has been added.
 * @see PacketProxyAddAchievementTranslation
 */
public class PacketServerAddAchievementTranslation extends Packet<ServerPacketListener> {
    private final AchievementTranslationData data;

    public PacketServerAddAchievementTranslation(@NotNull PacketByteBuf buf) {
        super(buf);
        data = buf.readAchievementTranslationData();
    }

    public PacketServerAddAchievementTranslation(@NotNull AchievementTranslationData data) {
        super(PacketByteBuf.EMPTY);
        this.data = data;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeAchievementTranslationData(data);
    }

    @Override
    public void handle(@NotNull ServerPacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public AchievementTranslationData getData() {
        return data;
    }
}
