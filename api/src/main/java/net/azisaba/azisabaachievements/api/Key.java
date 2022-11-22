package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Key {
    @NotNull
    String namespace();

    @NotNull
    String path();

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Key key(@NotNull String namespace, @NotNull String path) {
        return new KeyImpl(namespace, path);
    }
}
