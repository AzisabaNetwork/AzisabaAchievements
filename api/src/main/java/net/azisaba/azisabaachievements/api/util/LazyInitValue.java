package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class LazyInitValue<T> {
    private T value;
    private boolean initialized;
    private final Supplier<T> initializer;

    @Contract(pure = true)
    public LazyInitValue(@NotNull Supplier<T> initializer) {
        this.initializer = initializer;
    }

    public T get() {
        if (!initialized) {
            value = initializer.get();
            initialized = true;
        }
        return value;
    }
}
