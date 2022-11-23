package net.azisaba.azisabaachievements.api.network.packet;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.PacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketCommonAchievementUnlocked extends Packet<PacketListener> {
    private final UUID uuid;
    private final Key achievementKey;

    public PacketCommonAchievementUnlocked(@NotNull PacketByteBuf buf) {
        super(buf);
        this.uuid = buf.readUUID();
        this.achievementKey = buf.readKey();
    }

    public PacketCommonAchievementUnlocked(@NotNull UUID uuid, @NotNull Key achievementKey) {
        super(PacketByteBuf.EMPTY);
        this.uuid = uuid;
        this.achievementKey = achievementKey;
    }

    @Override
    public void write(@NotNull PacketByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeKey(achievementKey);
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
    public Key getAchievementKey() {
        return achievementKey;
    }
}
