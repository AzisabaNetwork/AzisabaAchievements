package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class BitFieldImpl implements BitField {
    private final int value;

    @Contract(pure = true)
    BitFieldImpl(int value) {
        this.value = value;
    }

    @Contract(pure = true)
    @Override
    public int getValue() {
        return value;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitFieldImpl)) return false;
        BitFieldImpl bitField = (BitFieldImpl) o;
        return getValue() == bitField.getValue();
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return getValue();
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "BitField(" + value + ")";
    }
}
