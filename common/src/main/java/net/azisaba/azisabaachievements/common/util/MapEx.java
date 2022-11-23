package net.azisaba.azisabaachievements.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class MapEx<K, V> extends AbstractMap<K, V> {
    private final Map<K, V> map;

    public MapEx(@NotNull Map<K, V> map) {
        this.map = map;
    }

    @Contract(pure = true)
    public V get(@Nullable Object key) {
        return map.get(key);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return map.containsValue(value);
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return map.containsKey(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        return map.remove(key, value);
    }

    @Override
    public V remove(@Nullable Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Contract(value = "null -> false", pure = true)
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Nullable
    public <K2, V2> MapEx<K2, V2> getMap(@Nullable K key) {
        return getMap(key, null);
    }

    @SuppressWarnings("unchecked")
    @Contract("_, !null -> !null")
    public <K2, V2> MapEx<K2, V2> getMap(@Nullable K key, @Nullable MapEx<K2, V2> def) {
        V value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof MapEx) {
            return (MapEx<K2, V2>) value;
        } else if (value instanceof Map<?, ?>) {
            return new MapEx<>((Map<K2, V2>) value);
        } else {
            return null;
        }
    }

    @Nullable
    public String getString(@Nullable K key) {
        return getString(key, null);
    }

    @Contract("_, !null -> !null")
    public String getString(@Nullable K key, @Nullable String def) {
        V value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            return (String) value;
        } else {
            return value.toString();
        }
    }

    public int getInt(@Nullable K key) {
        return getInt(key, 0);
    }

    public int getInt(@Nullable K key, int def) {
        V value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else {
            return def;
        }
    }

    public boolean getBoolean(@Nullable K key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(@Nullable K key, boolean def) {
        V value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else {
            return def;
        }
    }

    public double getDouble(@Nullable K key) {
        return getDouble(key, 0.0);
    }

    public double getDouble(@Nullable K key, double def) {
        V value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else {
            return def;
        }
    }

    public long getLong(@Nullable K key) {
        return getLong(key, 0L);
    }

    public long getLong(@Nullable K key, long def) {
        V value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        } else {
            return def;
        }
    }
}
