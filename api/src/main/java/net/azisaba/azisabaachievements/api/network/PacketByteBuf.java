package net.azisaba.azisabaachievements.api.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PacketByteBuf extends ByteBuf {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    protected final ByteBuf buf;

    public PacketByteBuf(@NotNull ByteBuf buf) {
        this.buf = Objects.requireNonNull(buf, "buf");
    }

    public void writeString(@NotNull String str) {
        if (str.length() * 2 < 0) {
            throw new IllegalArgumentException("String too long");
        }
        writeInt(str.length());
        writeBytes(str.getBytes(StandardCharsets.UTF_16));
    }

    public @NotNull String readString() {
        int length = readInt();
        if (length < 0) {
            throw new IllegalArgumentException("String length is negative");
        }
        if (length == 0) {
            return "";
        }
        if (length * 2 < 0) {
            throw new IllegalArgumentException("String too long");
        }
        byte[] bytes = new byte[length * 2];
        readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_16);
    }

    public int readVarInt() {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = readByte();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) break;
            position += 7;
            if (position >= 32) throw new RuntimeException("VarInt is too big");
        }
        return value;
    }

    public void writeVarInt(int value) {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                writeByte(value);
                return;
            }
            writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }
    }

    public long readVarLong() {
        long value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            currentByte = readByte();
            value |= (long) (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) break;
            position += 7;
            if (position >= 64) throw new RuntimeException("VarLong is too big");
        }
        return value;
    }

    public void writeVarLong(long value) {
        while (true) {
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                writeByte((int) value);
                return;
            }
            writeByte((int) ((value & SEGMENT_BITS) | CONTINUE_BIT));
            value >>>= 7;
        }
    }

    // overrides

    @Override
    public final boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }

    @Override
    public boolean isContiguous() {
        return buf.isContiguous();
    }

    @Override
    public final long memoryAddress() {
        return buf.memoryAddress();
    }

    @Override
    public final int capacity() {
        return buf.capacity();
    }

    @Contract("_, -> this")
    @Override
    public ByteBuf capacity(int newCapacity) {
        buf.capacity(newCapacity);
        return this;
    }

    @Override
    public final int maxCapacity() {
        return buf.maxCapacity();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public final ByteBufAllocator alloc() {
        return buf.alloc();
    }

    @Deprecated
    @Contract(pure = true)
    @NotNull
    @Override
    public final ByteOrder order() {
        return buf.order();
    }

    @Deprecated
    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf order(@NotNull ByteOrder endianness) {
        return buf.order(endianness);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public final ByteBuf unwrap() {
        return buf;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf asReadOnly() {
        return buf.asReadOnly();
    }

    @Override
    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    @Override
    public final boolean isDirect() {
        return buf.isDirect();
    }

    @Override
    public final int readerIndex() {
        return buf.readerIndex();
    }

    @Contract("_ -> this")
    @Override
    public final ByteBuf readerIndex(int readerIndex) {
        buf.readerIndex(readerIndex);
        return this;
    }

    @Override
    public final int writerIndex() {
        return buf.writerIndex();
    }

    @Contract("_ -> this")
    @Override
    public final ByteBuf writerIndex(int writerIndex) {
        buf.writerIndex(writerIndex);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        buf.setIndex(readerIndex, writerIndex);
        return this;
    }

    @Override
    public final int readableBytes() {
        return buf.readableBytes();
    }

    @Override
    public final int writableBytes() {
        return buf.writableBytes();
    }

    @Override
    public final int maxWritableBytes() {
        return buf.maxWritableBytes();
    }

    @Override
    public int maxFastWritableBytes() {
        return buf.maxFastWritableBytes();
    }

    @Override
    public final boolean isReadable() {
        return buf.isReadable();
    }

    @Override
    public final boolean isWritable() {
        return buf.isWritable();
    }

    @Contract(" -> this")
    @Override
    public final ByteBuf clear() {
        buf.clear();
        return this;
    }

    @Contract(" -> this")
    @Override
    public final ByteBuf markReaderIndex() {
        buf.markReaderIndex();
        return this;
    }

    @Contract(" -> this")
    @Override
    public final ByteBuf resetReaderIndex() {
        buf.resetReaderIndex();
        return this;
    }

    @Contract(" -> this")
    @Override
    public final ByteBuf markWriterIndex() {
        buf.markWriterIndex();
        return this;
    }

    @Contract(" -> this")
    @Override
    public final ByteBuf resetWriterIndex() {
        buf.resetWriterIndex();
        return this;
    }

    @Contract("-> this")
    @Override
    public ByteBuf discardReadBytes() {
        buf.discardReadBytes();
        return this;
    }

    @Contract("-> this")
    @Override
    public ByteBuf discardSomeReadBytes() {
        buf.discardSomeReadBytes();
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf ensureWritable(int minWritableBytes) {
        buf.ensureWritable(minWritableBytes);
        return this;
    }

    @Override
    public int ensureWritable(int minWritableBytes, boolean force) {
        return buf.ensureWritable(minWritableBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return buf.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return buf.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return buf.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return buf.getShort(index);
    }

    @Override
    public short getShortLE(int index) {
        return buf.getShortLE(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return buf.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return buf.getUnsignedShortLE(index);
    }

    @Override
    public int getMedium(int index) {
        return buf.getMedium(index);
    }

    @Override
    public int getMediumLE(int index) {
        return buf.getMediumLE(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return buf.getUnsignedMedium(index);
    }

    @Override
    public int getUnsignedMediumLE(int index) {
        return buf.getUnsignedMediumLE(index);
    }

    @Override
    public int getInt(int index) {
        return buf.getInt(index);
    }

    @Override
    public int getIntLE(int index) {
        return buf.getIntLE(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return buf.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return buf.getUnsignedIntLE(index);
    }

    @Override
    public long getLong(int index) {
        return buf.getLong(index);
    }

    @Override
    public long getLongLE(int index) {
        return buf.getLongLE(index);
    }

    @Override
    public char getChar(int index) {
        return buf.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return buf.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return buf.getDouble(index);
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf getBytes(int index, ByteBuf dst) {
        buf.getBytes(index, dst);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf getBytes(int index, @NotNull ByteBuf dst, int length) {
        buf.getBytes(index, dst, length);
        return this;
    }

    @Contract("_, _, _, _ -> this")
    @Override
    public ByteBuf getBytes(int index, @NotNull ByteBuf dst, int dstIndex, int length) {
        buf.getBytes(index, dst, dstIndex, length);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf getBytes(int index, byte @NotNull [] dst) {
        buf.getBytes(index, dst);
        return this;
    }

    @Contract("_, _, _, _ -> this")
    @Override
    public ByteBuf getBytes(int index, byte @NotNull [] dst, int dstIndex, int length) {
        buf.getBytes(index, dst, dstIndex, length);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf getBytes(int index, @NotNull ByteBuffer dst) {
        buf.getBytes(index, dst);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf getBytes(int index, @NotNull OutputStream out, int length) throws IOException {
        buf.getBytes(index, out, length);
        return this;
    }

    @Override
    public int getBytes(int index, @NotNull GatheringByteChannel out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, @NotNull FileChannel out, long position, int length) throws IOException {
        return buf.getBytes(index, out, position, length);
    }

    @NotNull
    @Override
    public CharSequence getCharSequence(int index, int length, @NotNull Charset charset) {
        return buf.getCharSequence(index, length, charset);
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        buf.setBoolean(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setByte(int index, int value) {
        buf.setByte(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setShort(int index, int value) {
        buf.setShort(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setShortLE(int index, int value) {
        buf.setShortLE(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setMedium(int index, int value) {
        buf.setMedium(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setMediumLE(int index, int value) {
        buf.setMediumLE(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setInt(int index, int value) {
        buf.setInt(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setIntLE(int index, int value) {
        buf.setIntLE(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setLong(int index, long value) {
        buf.setLong(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setLongLE(int index, long value) {
        buf.setLongLE(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setChar(int index, int value) {
        buf.setChar(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setFloat(int index, float value) {
        buf.setFloat(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setDouble(int index, double value) {
        buf.setDouble(index, value);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setBytes(int index, @NotNull ByteBuf src) {
        buf.setBytes(index, src);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf setBytes(int index, @NotNull ByteBuf src, int length) {
        buf.setBytes(index, src, length);
        return this;
    }

    @Contract("_, _, _, _ -> this")
    @Override
    public ByteBuf setBytes(int index, @NotNull ByteBuf src, int srcIndex, int length) {
        buf.setBytes(index, src, srcIndex, length);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setBytes(int index, byte @NotNull [] src) {
        buf.setBytes(index, src);
        return this;
    }

    @Contract("_, _, _, _ -> this")
    @Override
    public ByteBuf setBytes(int index, byte @NotNull [] src, int srcIndex, int length) {
        buf.setBytes(index, src, srcIndex, length);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setBytes(int index, @NotNull ByteBuffer src) {
        buf.setBytes(index, src);
        return this;
    }

    @Override
    public int setBytes(int index, @NotNull InputStream in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, @NotNull ScatteringByteChannel in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, @NotNull FileChannel in, long position, int length) throws IOException {
        return buf.setBytes(index, in, position, length);
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf setZero(int index, int length) {
        buf.setZero(index, length);
        return this;
    }

    @Override
    public int setCharSequence(int index, @NotNull CharSequence sequence, @NotNull Charset charset) {
        return buf.setCharSequence(index, sequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public short readShortLE() {
        return buf.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return buf.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return buf.readMedium();
    }

    @Override
    public int readMediumLE() {
        return buf.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return buf.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public int readIntLE() {
        return buf.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return buf.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public long readLongLE() {
        return buf.readLongLE();
    }

    @Override
    public char readChar() {
        return buf.readChar();
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Contract
    @NotNull
    @Override
    public ByteBuf readBytes(int length) {
        return buf.readBytes(length);
    }

    @Contract
    @NotNull
    @Override
    public ByteBuf readSlice(int length) {
        return buf.readSlice(length);
    }

    @Contract
    @NotNull
    @Override
    public ByteBuf readRetainedSlice(int length) {
        return buf.readRetainedSlice(length);
    }

    @Contract
    @NotNull
    @Override
    public ByteBuf readBytes(ByteBuf dst) {
        buf.readBytes(dst);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf readBytes(@NotNull ByteBuf dst, int length) {
        buf.readBytes(dst, length);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf readBytes(@NotNull ByteBuf dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf readBytes(byte @NotNull [] dst) {
        buf.readBytes(dst);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf readBytes(byte @NotNull [] dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf readBytes(@NotNull ByteBuffer dst) {
        buf.readBytes(dst);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf readBytes(@NotNull OutputStream out, int length) throws IOException {
        buf.readBytes(out, length);
        return this;
    }

    @Override
    public int readBytes(@NotNull GatheringByteChannel out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    @Override
    public int readBytes(@NotNull FileChannel out, long position, int length) throws IOException {
        return buf.readBytes(out, position, length);
    }

    @NotNull
    @Override
    public CharSequence readCharSequence(int length, @NotNull Charset charset) {
        return buf.readCharSequence(length, charset);
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf skipBytes(int length) {
        buf.skipBytes(length);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeBoolean(boolean value) {
        buf.writeBoolean(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeByte(int value) {
        buf.writeByte(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeShort(int value) {
        buf.writeShort(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeShortLE(int value) {
        buf.writeShortLE(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeMedium(int value) {
        buf.writeMedium(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeMediumLE(int value) {
        buf.writeMediumLE(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeInt(int value) {
        buf.writeInt(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeIntLE(int value) {
        buf.writeIntLE(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeLong(long value) {
        buf.writeLong(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeLongLE(long value) {
        buf.writeLongLE(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeChar(int value) {
        buf.writeChar(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeFloat(float value) {
        buf.writeFloat(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeDouble(double value) {
        buf.writeDouble(value);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeBytes(@NotNull ByteBuf src) {
        buf.writeBytes(src);
        return this;
    }

    @Contract("_, _ -> this")
    @Override
    public ByteBuf writeBytes(@NotNull ByteBuf src, int length) {
        buf.writeBytes(src, length);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf writeBytes(@NotNull ByteBuf src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeBytes(byte @NotNull [] src) {
        buf.writeBytes(src);
        return this;
    }

    @Contract("_, _, _ -> this")
    @Override
    public ByteBuf writeBytes(byte @NotNull [] src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeBytes(@NotNull ByteBuffer src) {
        buf.writeBytes(src);
        return this;
    }

    @Override
    public int writeBytes(@NotNull InputStream in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes(@NotNull ScatteringByteChannel in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes(@NotNull FileChannel in, long position, int length) throws IOException {
        return buf.writeBytes(in, position, length);
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf writeZero(int length) {
        buf.writeZero(length);
        return this;
    }

    @Override
    public int writeCharSequence(@NotNull CharSequence sequence, @NotNull Charset charset) {
        return buf.writeCharSequence(sequence, charset);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return buf.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return buf.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return buf.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return buf.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(@NotNull ByteProcessor processor) {
        return buf.forEachByte(processor);
    }

    @Override
    public int forEachByte(int index, int length, @NotNull ByteProcessor processor) {
        return buf.forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(@NotNull ByteProcessor processor) {
        return buf.forEachByteDesc(processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, @NotNull ByteProcessor processor) {
        return buf.forEachByteDesc(index, length, processor);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf copy() {
        return buf.copy();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf copy(int index, int length) {
        return buf.copy(index, length);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf slice() {
        return buf.slice();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf retainedSlice() {
        return buf.retainedSlice();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf slice(int index, int length) {
        return buf.slice(index, length);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return buf.retainedSlice(index, length);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf duplicate() {
        return buf.duplicate();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuf retainedDuplicate() {
        return buf.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return buf.nioBufferCount();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuffer nioBuffer() {
        return buf.nioBuffer();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return buf.nioBuffer(index, length);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuffer @NotNull [] nioBuffers() {
        return buf.nioBuffers();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuffer @NotNull [] nioBuffers(int index, int length) {
        return buf.nioBuffers(index, length);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return buf.internalNioBuffer(index, length);
    }

    @Override
    public boolean hasArray() {
        return buf.hasArray();
    }

    @Override
    public byte @NotNull [] array() {
        return buf.array();
    }

    @Override
    public int arrayOffset() {
        return buf.arrayOffset();
    }

    @NotNull
    @Override
    public String toString(@NotNull Charset charset) {
        return buf.toString(charset);
    }

    @NotNull
    @Override
    public String toString(int index, int length, @NotNull Charset charset) {
        return buf.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return buf.hashCode();
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(@Nullable Object obj) {
        return buf.equals(obj);
    }

    @Contract(pure = true)
    @Override
    public int compareTo(@NotNull ByteBuf buffer) {
        return buf.compareTo(buffer);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + buf + ')';
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf retain(int increment) {
        buf.retain(increment);
        return this;
    }

    @Contract("-> this")
    @Override
    public ByteBuf retain() {
        buf.retain();
        return this;
    }

    @Contract("-> this")
    @Override
    public ByteBuf touch() {
        buf.touch();
        return this;
    }

    @Contract("_ -> this")
    @Override
    public ByteBuf touch(@NotNull Object hint) {
        buf.touch(hint);
        return this;
    }

    @Override
    public final boolean isReadable(int size) {
        return buf.isReadable(size);
    }

    @Override
    public final boolean isWritable(int size) {
        return buf.isWritable(size);
    }

    @Override
    public final int refCnt() {
        return buf.refCnt();
    }

    @Override
    public boolean release() {
        return buf.release();
    }

    @Override
    public boolean release(int decrement) {
        return buf.release(decrement);
    }
}
