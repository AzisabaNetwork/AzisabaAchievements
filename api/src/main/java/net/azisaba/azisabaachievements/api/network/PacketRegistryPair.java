package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface PacketRegistryPair {
    @Contract(pure = true)
    @NotNull
    PacketRegistry getServerRegistry();

    @Contract(pure = true)
    @NotNull
    PacketRegistry getClientRegistry();
}
