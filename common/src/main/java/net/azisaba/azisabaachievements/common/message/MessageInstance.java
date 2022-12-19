package net.azisaba.azisabaachievements.common.message;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class MessageInstance {
    public static final MessageInstance FALLBACK = createSimple(Function.identity());

    public abstract @NotNull String get(@NotNull String key);

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MessageInstance createSimple(@NotNull Function<@NotNull String, @NotNull String> getter) {
        return new MessageInstance() {
            private final Map<String, String> cache = new ConcurrentHashMap<>();

            @Override
            public @NotNull String get(@NotNull String key) {
                return cache.computeIfAbsent(key, getter);
            }
        };
    }
}
