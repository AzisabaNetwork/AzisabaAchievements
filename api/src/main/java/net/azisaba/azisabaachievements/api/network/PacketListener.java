package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderChanged;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderLeave;
import org.jetbrains.annotations.NotNull;

public interface PacketListener {
    default void handle(@NotNull PacketCommonAchievementUnlocked packet) {}
    default void handle(@NotNull PacketCommonProxyLeaderLeave packet) {}
    default void handle(@NotNull PacketCommonProxyLeaderChanged packet) {}
}
