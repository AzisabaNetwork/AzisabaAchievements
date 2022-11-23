package net.azisaba.azisabaachievements.api.network;

import io.netty.buffer.ByteBuf;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderChanged;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderLeave;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface PacketRegistry {
    default void registerCommonPackets() {
        registerPacket(PacketCommonAchievementUnlocked.class, PacketCommonAchievementUnlocked::new);
        registerPacket(PacketCommonProxyLeaderLeave.class, PacketCommonProxyLeaderLeave::new);
        registerPacket(PacketCommonProxyLeaderChanged.class, PacketCommonProxyLeaderChanged::new);
    }

    default void registerProxyBoundPackets() {
        registerPacket(PacketProxyCreateAchievement.class, PacketProxyCreateAchievement::new);
    }

    default void registerServerBoundPackets() {
        registerPacket(PacketServerCreateAchievementCallback.class, PacketServerCreateAchievementCallback::new);
    }

    /**
     * Register a packet to the registry.
     * @param packetClass The packet class to register.
     * @param packetConstructor The packet constructor which takes a single {@link PacketByteBuf} as an argument.
     * @return The packet id.
     */
    <T extends Packet<?>> int registerPacket(@NotNull Class<T> packetClass, @NotNull Function<PacketByteBuf, T> packetConstructor);

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
