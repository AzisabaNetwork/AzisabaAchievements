package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PacketByteBufReader<T> {
    T read(@NotNull PacketByteBuf buf);
}
