package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface PacketRegistryPair {
    /**
     * Returns the client registry. Client registry is used for processing packets sent from somewhere.
     * @return the client registry
     */
    @Contract(pure = true)
    @NotNull
    PacketRegistry getClientRegistry();

    /**
     * Returns the server registry. Server registry is used for sending packets.
     * @return the server registry
     */
    @Contract(pure = true)
    @NotNull
    PacketRegistry getServerRegistry();
}
