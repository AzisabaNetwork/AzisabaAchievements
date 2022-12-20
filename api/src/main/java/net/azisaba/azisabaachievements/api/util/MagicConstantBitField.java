package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.Set;

public interface MagicConstantBitField<T> extends BitField, Set<String> {
    @Contract(value = "_ -> new", pure = true)
    static <A> @NotNull Codec<MagicConstantBitField<A>> codec(@NotNull Class<A> clazz) {
        return Codec.INT.xmap(i -> MagicConstantBitField.of(clazz, i), MagicConstantBitField::getValue).named("MagicConstantBitField(" + clazz.getTypeName() + ")");
    }

    @Contract(pure = true)
    static <T> @NotNull MagicConstantBitField<T> of(@NotNull Class<T> clazz, int value) {
        return new MagicConstantBitFieldImpl<>(clazz, value);
    }

    @Contract(pure = true)
    @NotNull Class<T> getClazz();
}
