package net.azisaba.azisabaachievements.api;

import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface AzisabaAchievements {
    @Contract(pure = true)
    @NotNull
    Logger getLogger();

    @Contract(pure = true)
    @NotNull
    PacketRegistryPair getPacketRegistryPair();
}
