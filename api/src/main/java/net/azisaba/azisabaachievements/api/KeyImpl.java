package net.azisaba.azisabaachievements.api;

import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

final class KeyImpl implements Key {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z0-9._\\-]+$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^[a-z0-9/._\\-]+$");
    @Subst("minecraft")
    private final String namespace;
    @Subst("foo/bar/baz")
    private final String path;

    KeyImpl(@NotNull String namespace, @NotNull String path) {
        if (!NAMESPACE_PATTERN.matcher(namespace).matches() ||
                path.endsWith(".") || path.endsWith("_") || path.endsWith("-") ||
                path.startsWith(".") || path.startsWith("_") || path.startsWith("-")) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }
        if (!PATH_PATTERN.matcher(path).matches() ||
                path.endsWith(".") || path.endsWith("_") || path.endsWith("-") ||
                path.startsWith(".") || path.startsWith("_") || path.startsWith("-")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        this.namespace = namespace;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        this.path = path;
    }

    @org.intellij.lang.annotations.Pattern("^[a-z0-9._\\-]+$")
    @Contract(pure = true)
    @Override
    public @NotNull String namespace() {
        return namespace;
    }

    @org.intellij.lang.annotations.Pattern("^[a-z0-9/._\\-]+$")
    @Contract(pure = true)
    @Override
    public @NotNull String path() {
        return path;
    }

    @Override
    public @NotNull Key parent() {
        int index = path.lastIndexOf('/');
        if (index == -1) {
            return this;
        } else {
            return new KeyImpl(namespace, path.substring(0, index));
        }
    }

    @org.intellij.lang.annotations.Pattern("^[a-z0-9._\\-]+:[a-z0-9/._\\-]+$")
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
