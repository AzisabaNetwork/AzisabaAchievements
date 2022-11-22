package net.azisaba.azisabaachievements.api.network;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PacketRegistry {
    /**
     * Register a packet to the registry.
     * @param packetClass The packet class to register.
     * @return The packet id.
     */
    int registerPacket(@NotNull Class<? extends Packet<?>> packetClass);

    /**
     * Get packet class by its id. Returns null if the id is not registered.
     * @param id Packet id
     * @return packet class or null if the id is not registered.
     */
    @Nullable
    Class<? extends Packet<?>> getById(int id);

    /**
     * Creates a packet by its id and a byte buffer.
     * @param id Packet id
     * @param buf Byte buffer
     * @return the packet instance
     * @throws IllegalArgumentException If the packet is not registered with the id
     */
    @NotNull
    Packet<?> createPacket(int id, @NotNull ByteBuf buf) throws IllegalArgumentException;

    /**
     * Get packet id by its class. Returns -1 if the class is not registered.
     * @param packetClass Packet class
     * @return packet id or -1 if the class is not registered.
     */
    <T extends Packet<?>> int getId(@NotNull Class<T> packetClass);
}
