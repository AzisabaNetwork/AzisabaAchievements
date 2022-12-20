package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

public interface Key {
    Codec<Key> CODEC =
            Codec.<Key>builder()
                    .group(Codec.STRING.fieldOf("namespace").getter(Key::namespace), Codec.STRING.fieldOf("path").getter(Key::path))
                    .build(Key::key)
                    .named("Key");

    @NotNull
    String namespace();

    @NotNull
    String path();

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Key key(@NotNull String namespace, @NotNull String path) {
        return new KeyImpl(namespace, path);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull Key key(@NotNull String key) {
        String[] split = key.split(":");
        if (split.length == 1) {
            return key("minecraft", split[0]);
        } else if (split.length == 2) {
            return key(split[0], split[1]);
        } else {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
    }
}
