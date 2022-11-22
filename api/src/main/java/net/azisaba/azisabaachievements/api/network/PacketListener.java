package net.azisaba.azisabaachievements.api.network;

import net.azisaba.azisabaachievements.api.network.packet.PacketAchievementUnlocked;
import org.jetbrains.annotations.NotNull;

public interface PacketListener {
    default void handle(@NotNull PacketAchievementUnlocked packet) {}
}
