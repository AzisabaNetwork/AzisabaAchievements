package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a packet.
 * @param <T> the receiver packet listener type
 */
public abstract class Packet<T extends PacketListener> {
    /**
     * Constructs a packet using the given buffer. The implementation is <b>abstract</b> and should be implemented by the
     * subclass.
     * @param buf the buffer
     */
    public Packet(@NotNull PacketByteBuf buf) {
        Objects.requireNonNull(buf, "buf");
        // abstract
    }

    /**
     * Writes the packet to the buffer.
     * @param buf the buffer
     */
    public abstract void write(@NotNull PacketByteBuf buf);

    /**
     * Handles the packet.
     * @param packetListener the packet listener
     */
    public abstract void handle(@NotNull T packetListener);
}
