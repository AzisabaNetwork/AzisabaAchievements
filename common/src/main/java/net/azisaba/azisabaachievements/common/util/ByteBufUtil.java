package net.azisaba.azisabaachievements.common.util;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public final class ByteBufUtil {
    private ByteBufUtil() { throw new AssertionError(); }

    /**
     * Copies the content of the given {@link ByteBuf} into a new byte array. The given {@link ByteBuf} will be released
     * (decrements the reference count by 1).
     * @param buf The {@link ByteBuf} to copy.
     * @return The copied byte array.
     */
    public static byte @NotNull [] toByteArray(@NotNull ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        buf.release();
        return bytes;
    }
}
