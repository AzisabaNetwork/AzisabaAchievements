package net.azisaba.azisabaachievements.api;

import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface AzisabaAchievements {
    /**
     * Returns the logger instance. The logger can also be obtained by {@link Logger#getCurrentLogger()}.
     * @return the logger
     */
    @Contract(pure = true)
    @NotNull
    Logger getLogger();

    /**
     * Returns the packet registry pair. The server registry is used for packets sent from the server, and the client
     * registry is used for processing packets sent from somewhere.
     * @return the packet registry pair
     */
    @Contract(pure = true)
    @NotNull
    PacketRegistryPair getPacketRegistryPair();
}
