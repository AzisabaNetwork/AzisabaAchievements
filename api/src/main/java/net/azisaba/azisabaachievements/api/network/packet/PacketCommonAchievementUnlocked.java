package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.PacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This packet is sent from proxy to server to notify that an achievement is unlocked for a player.
 */
public class PacketCommonAchievementUnlocked extends Packet<PacketListener> {
    private final UUID uuid;
    private final AchievementData achievement;

    public PacketCommonAchievementUnlocked(@NotNull PacketByteBuf buf) {
        super(buf);
        this.uuid = buf.readUUID();
        this.achievement = buf.readWithCodec(AchievementData.NETWORK_CODEC);
    }

    public PacketCommonAchievementUnlocked(@NotNull UUID uuid, @NotNull AchievementData achievement) {
        super(PacketByteBuf.EMPTY);
        this.uuid = uuid;
        this.achievement = achievement;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeWithCodec(achievement, AchievementData.NETWORK_CODEC);
    }

    @Override
    public void handle(@NotNull PacketListener packetListener) {
        packetListener.handle(this);
    }

    @NotNull
    public UUID getPlayerUniqueId() {
        return uuid;
    }

    @NotNull
    public AchievementData getAchievement() {
        return achievement;
    }
}
