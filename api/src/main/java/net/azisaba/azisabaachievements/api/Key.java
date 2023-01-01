package net.azisaba.azisabaachievements.api;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

public interface Key {
    Codec<Key> CODEC =
            Codec.<Key>builder()
                    .group(Codec.STRING.fieldOf("namespace").getter(Key::namespace), Codec.STRING.fieldOf("path").getter(Key::path))
                    .build(Key::key)
                    .named("Key");

    @Pattern("^[a-z0-9._\\-]+$")
    @NotNull
    String namespace();

    @Pattern("^[a-z0-9/._\\-]+$")
    @NotNull
    String path();

    /**
     * Returns the key at parent path. For example, if the key is "minecraft:foo/bar/baz", this method returns "minecraft:foo/bar".
     * This method may return the same key if the key is at the root path.
     * @return the key at parent path
     */
    @NotNull
    Key parent();

    @Pattern("^[a-z0-9._\\-]+:[a-z0-9/._\\-]+$")
    @NotNull
    String toString();

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Key key(@NotNull @Pattern("^[a-z0-9._\\-]+$") String namespace, @NotNull @Pattern("^[a-z0-9/._\\-]+$") String path) {
        return new KeyImpl(namespace, path);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull Key key(@NotNull @Pattern("^[a-z0-9._\\-]+:[a-z0-9/._\\-]+$|^[a-z0-9/._\\-]+$") String key) {
        @Subst("minecraft") String[] split = key.split(":");
        if (split.length == 1) {
            return key("minecraft", split[0]);
        } else if (split.length == 2) {
            return key(split[0], split[1]);
        } else {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
    }
}
