package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

public final class ExtraCodecs {
    @Contract("_, _ -> new")
    public static <K, V> @NotNull Codec<Map.Entry<K, V>> entry(@NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        return Codec.<Map.Entry<K, V>>builder()
                .group(
                        keyCodec.optionalFieldOf("first").getter(e -> Optional.ofNullable(e.getKey())),
                        valueCodec.optionalFieldOf("second").getter(e -> Optional.ofNullable(e.getValue()))
                )
                .build(AbstractMap.SimpleEntry::new);
    }
}
