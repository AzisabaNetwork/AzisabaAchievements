package net.azisaba.azisabaachievements.api.network;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Packet<T extends PacketListener> {
    public Packet(@NotNull PacketByteBuf buf) {
        Objects.requireNonNull(buf, "buf");
        // abstract
    }

    public abstract void write(@NotNull PacketByteBuf buf);

    public abstract void handle(@NotNull T packetListener);
}
