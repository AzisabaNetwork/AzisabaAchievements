package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

final class KeyImpl implements Key {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z0-9._\\-]+$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^[a-z0-9/._\\-]+$");
    private final String namespace;
    private final String path;

    KeyImpl(@NotNull String namespace, @NotNull String path) {
        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        this.namespace = namespace;
        this.path = path;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String namespace() {
        return namespace;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String path() {
        return path;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return namespace + ':' + path;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyImpl)) return false;
        KeyImpl key = (KeyImpl) o;
        return namespace.equals(key.namespace) && path.equals(key.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
}
