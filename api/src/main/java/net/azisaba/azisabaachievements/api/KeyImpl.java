package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull String namespace() {
        return namespace;
    }

    @Override
    public @NotNull String path() {
        return path;
    }
}
