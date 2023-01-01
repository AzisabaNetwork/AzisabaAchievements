package net.azisaba.azisabaachievements.velocity.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class ExtraArgType {
    private ExtraArgType() {}

    public static @NotNull Key getKey(@NotNull CommandContext<?> ctx, @NotNull String name) {
        @Subst("minecraft:foo") String stringKey = StringArgumentType.getString(ctx, name);
        return Key.key(stringKey);
    }

    public static <E extends Enum<E>> @NotNull E getEnum(@NotNull CommandContext<?> ctx, @NotNull String name, @NotNull Class<E> achievementHideFlagsClass) {
        String string = StringArgumentType.getString(ctx, name);
        return Enum.valueOf(achievementHideFlagsClass, string.toUpperCase());
    }

    public static <T> @NotNull MagicConstantBitField<T> getMagicConstant(@NotNull CommandContext<?> ctx, @NotNull String name, @NotNull Class<T> clazz) {
        String string = StringArgumentType.getString(ctx, name);
        int value;
        try {
            value = (int) clazz.getField(string.toUpperCase(Locale.ROOT)).get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return MagicConstantBitField.of(clazz, value);
    }
}
