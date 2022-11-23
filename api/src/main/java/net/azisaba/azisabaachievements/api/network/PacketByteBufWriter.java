package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PacketByteBufWriter<T> {
    void write(T value, @NotNull PacketByteBuf buf);
}
