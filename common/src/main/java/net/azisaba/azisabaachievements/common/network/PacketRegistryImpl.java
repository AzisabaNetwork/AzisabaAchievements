package net.azisaba.azisabaachievements.common.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Map;

public class PacketRegistryImpl implements PacketRegistry {
    private final Int2ObjectMap<Class<? extends Packet<?>>> idToPacket = new Int2ObjectOpenHashMap<>();
    private final Map<Class<? extends Packet<?>>, Integer> packetToId = new Object2IntArrayMap<>();

    @Override
    public int registerPacket(@NotNull Class<? extends Packet<?>> packetClass) {
        if (Modifier.isAbstract(packetClass.getModifiers())) {
            throw new IllegalArgumentException("Cannot register abstract packet " + packetClass.getTypeName());
        }
        try {
            if (!Modifier.isPublic(packetClass.getConstructor(PacketByteBuf.class).getModifiers())) {
                throw new NoSuchMethodException("Constructor is present but is not public");
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Packet " + packetClass.getTypeName() + " is missing a constructor with a single PacketByteBuf argument", e);
        }
        if (packetToId.containsKey(packetClass)) {
            throw new IllegalArgumentException("Packet " + packetClass.getTypeName() + " is already registered");
        }
        int id = packetToId.size();
        packetToId.put(packetClass, id);
        idToPacket.put(id, packetClass);
        return id;
    }

    @Override
    public @Nullable Class<? extends Packet<?>> getById(int id) {
        return idToPacket.get(id);
    }

    @Override
    public @NotNull Packet<?> createPacket(int id, @NotNull ByteBuf buf) throws IllegalArgumentException {
        Class<? extends Packet<?>> packetClass = getById(id);
        if (packetClass == null) {
            throw new IllegalArgumentException("Packet id " + id + " is missing");
        }
        PacketByteBuf packetByteBuf;
        if (buf instanceof PacketByteBuf) {
            packetByteBuf = (PacketByteBuf) buf;
        } else {
            packetByteBuf = new PacketByteBuf(buf);
        }
        try {
            return packetClass.getConstructor(PacketByteBuf.class).newInstance(packetByteBuf);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create the instance of packet " + packetClass.getTypeName(), e);
        }
    }

    @Override
    public <T extends Packet<?>> int getId(@NotNull Class<T> packetClass) {
        return packetToId.getOrDefault(packetClass, -1);
    }
}
