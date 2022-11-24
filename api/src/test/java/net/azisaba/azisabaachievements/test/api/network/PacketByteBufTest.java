package net.azisaba.azisabaachievements.test.api.network;

import io.netty.buffer.Unpooled;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class PacketByteBufTest {
    @Contract(" -> new")
    private static @NotNull PacketByteBuf create() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    @Test
    public void testVarInt() {
        PacketByteBuf buf = create();
        buf.writeVarInt(11);
        int read = buf.readVarInt();
        assert read == 11 : read;
    }

    @Test
    public void testKey() {
        PacketByteBuf buf = create();
        buf.writeKey(Key.key("minecraft", "example"));
        Key read = buf.readKey();
        assert read.equals(Key.key("minecraft", "example")) : read;
    }
}
