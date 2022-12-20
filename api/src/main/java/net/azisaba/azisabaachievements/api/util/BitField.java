package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

/**
 * Represents a bit field.
 */
public interface BitField {
    Codec<BitField> CODEC = Codec.INT.xmap(BitField::of, BitField::getValue).named("BitField");

    @NotNull BitField ZERO = new BitFieldImpl(0);

    @Contract(pure = true)
    static @NotNull BitField of(int value) {
        if (value == 0) {
            return ZERO;
        }
        return new BitFieldImpl(value);
    }

    @Contract(pure = true)
    int getValue();

    @Contract(pure = true)
    default @NotNull BitField zero() {
        return ZERO;
    }

    @Contract(pure = true)
    default boolean contains(int flag) {
        return (getValue() & flag) == flag;
    }

    @Contract(pure = true)
    default boolean contains(@NotNull BitField field) {
        return contains(field.getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField or(int flag) {
        return of(getValue() | flag);
    }

    @Contract(pure = true)
    default @NotNull BitField or(@NotNull BitField field) {
        return or(field.getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField and(int flag) {
        return of(getValue() & flag);
    }

    @Contract(pure = true)
    default @NotNull BitField and(@NotNull BitField field) {
        return and(field.getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField xor(int flag) {
        return of(getValue() ^ flag);
    }

    @Contract(pure = true)
    default @NotNull BitField xor(@NotNull BitField field) {
        return xor(field.getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField not() {
        return of(~getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField andNot(int flag) {
        return of(getValue() & ~flag);
    }

    @Contract(pure = true)
    default @NotNull BitField andNot(@NotNull BitField field) {
        return andNot(field.getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField orNot(int flag) {
        return of(getValue() | ~flag);
    }

    @Contract(pure = true)
    default @NotNull BitField orNot(@NotNull BitField field) {
        return orNot(field.getValue());
    }

    @Contract(pure = true)
    default @NotNull BitField xorNot(int flag) {
        return of(getValue() ^ ~flag);
    }

    @Contract(pure = true)
    default @NotNull BitField xorNot(@NotNull BitField field) {
        return xorNot(field.getValue());
    }

    @Contract(pure = true)
    default int orI(int flag) {
        return getValue() | flag;
    }

    @Contract(pure = true)
    default int orI(@NotNull BitField field) {
        return orI(field.getValue());
    }

    @Contract(pure = true)
    default int andI(int flag) {
        return getValue() & flag;
    }

    @Contract(pure = true)
    default int andI(@NotNull BitField field) {
        return andI(field.getValue());
    }

    @Contract(pure = true)
    default int xorI(int flag) {
        return getValue() ^ flag;
    }

    @Contract(pure = true)
    default int xorI(@NotNull BitField field) {
        return xorI(field.getValue());
    }

    @Contract(pure = true)
    default int notI() {
        return ~getValue();
    }

    @Contract(pure = true)
    default int andNotI(int flag) {
        return getValue() & ~flag;
    }

    @Contract(pure = true)
    default int andNotI(@NotNull BitField field) {
        return andNotI(field.getValue());
    }

    @Contract(pure = true)
    default int orNotI(int flag) {
        return getValue() | ~flag;
    }

    @Contract(pure = true)
    default int orNotI(@NotNull BitField field) {
        return orNotI(field.getValue());
    }

    @Contract(pure = true)
    default int xorNotI(int flag) {
        return getValue() ^ ~flag;
    }

    @Contract(pure = true)
    default int xorNotI(@NotNull BitField field) {
        return xorNotI(field.getValue());
    }

    @Contract(pure = true)
    default boolean isZero() {
        return getValue() == 0;
    }
}
