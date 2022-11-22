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
import java.util.function.Function;

public class PacketRegistryImpl implements PacketRegistry {
    private final Int2ObjectMap<PacketData> idToPacket = new Int2ObjectOpenHashMap<>();
    private final Map<Class<? extends Packet<?>>, Integer> packetToId = new Object2IntArrayMap<>();

    @Override
    public <T extends Packet<?>> int registerPacket(@NotNull Class<T> packetClass, @NotNull Function<PacketByteBuf, T> packetConstructor) {
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
        PacketData packetData = new PacketData(packetClass, packetConstructor);
        int id = packetToId.size();
        packetToId.put(packetClass, id);
        idToPacket.put(id, packetData);
        return id;
    }

    @Override
    public @Nullable Class<? extends Packet<?>> getById(int id) {
        PacketData packetData = idToPacket.get(id);
        return packetData == null ? null : packetData.packetClass;
    }

    @Override
    public @NotNull Packet<?> createPacket(int id, @NotNull ByteBuf buf) throws IllegalArgumentException {
        PacketData packetData = idToPacket.get(id);
        if (packetData == null) {
            throw new IllegalArgumentException("Packet id " + id + " is missing");
        }
        PacketByteBuf packetByteBuf;
        if (buf instanceof PacketByteBuf) {
            packetByteBuf = (PacketByteBuf) buf;
        } else {
            packetByteBuf = new PacketByteBuf(buf);
        }
        return packetData.packetConstructor.apply(packetByteBuf);
    }

    @Override
    public <T extends Packet<?>> int getId(@NotNull Class<T> packetClass) {
        return packetToId.getOrDefault(packetClass, -1);
    }

    public static class PacketData {
        private final Class<? extends Packet<?>> packetClass;
        private final Function<PacketByteBuf, ? extends Packet<?>> packetConstructor;

        public PacketData(@NotNull Class<? extends Packet<?>> packetClass, @NotNull Function<PacketByteBuf, ? extends Packet<?>> packetConstructor) {
            this.packetClass = packetClass;
            this.packetConstructor = packetConstructor;
        }
    }
}
