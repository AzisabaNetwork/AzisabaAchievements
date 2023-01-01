package net.azisaba.azisabaachievements.api.util;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MagicConstantBitFieldImpl<T> extends AbstractSet<String> implements MagicConstantBitField<T> {
    static final Map<Class<?>, Map<Integer, String>> CACHE = new ConcurrentHashMap<>();
    private final Class<T> clazz;
    private final int value;
    private final LazyInitValue<Set<String>> values;

    @Contract(pure = true)
    MagicConstantBitFieldImpl(@NotNull Class<T> clazz, int value) {
        this.clazz = clazz;
        this.value = value;
        this.values = new LazyInitValue<>(() -> fetchValue(clazz, value));
    }

    @Contract(pure = true)
    @Override
    public @NotNull Class<T> getClazz() {
        return clazz;
    }

    @Contract(pure = true)
    @Override
    public int getValue() {
        return value;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MagicConstantBitFieldImpl)) return false;
        MagicConstantBitFieldImpl<?> that = (MagicConstantBitFieldImpl<?>) o;
        return getValue() == that.getValue() && clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, getValue());
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "MagicConstantBitField(clazz = " + clazz.getTypeName() + ", value = " + value + ")";
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Contract(pure = true)
    @Override
    public boolean isEmpty() {
        return values.get().isEmpty();
    }

    @Contract(pure = true)
    @Override
    public boolean contains(Object o) {
        return values.get().contains(o);
    }

    @NotNull
    @Override
    public String @NotNull [] toArray() {
        return values.get().toArray(new String[0]);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return super.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends String> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return values.get().iterator();
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return values.get().size();
    }

    @Contract("_ -> fail")
    @Override
    public boolean add(String s) {
        throw new UnsupportedOperationException();
    }

    @Contract(value = "_ -> fail", pure = true)
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Contract(value = "_ -> fail", pure = true)
    @Override
    public boolean removeIf(@NotNull Predicate<? super String> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Spliterator<String> spliterator() {
        return values.get().spliterator();
    }

    @NotNull
    @Override
    public <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        return values.get().toArray(a);
    }

    @Override
    public @NotNull Stream<String> stream() {
        return values.get().stream();
    }

    @Override
    public @NotNull Stream<String> parallelStream() {
        return values.get().parallelStream();
    }

    @Override
    public void forEach(@NotNull Consumer<? super String> action) {
        values.get().forEach(action);
    }

    @Override
    public @NotNull Set<String> getNames() {
        return values.get();
    }

    private static boolean isPowerOfTwo(int n) {
        return (n & (n - 1)) == 0;
    }

    private static void cacheClass(@NotNull Class<?> clazz) {
        if (CACHE.containsKey(clazz)) {
            return;
        }
        Map<Integer, String> objects;
        try {
            objects = AzisabaAchievementsProvider.get().createInt2ObjectHashMap();
        } catch (UnsupportedOperationException e) {
            objects = new HashMap<>();
        }
        if (clazz.isEnum()) {
            for (Object o : clazz.getEnumConstants()) {
                int key = 1 << ((Enum<?>) o).ordinal();
                if (key < 0) {
                    throw new IllegalArgumentException("Too many enum constants in " + clazz.getTypeName());
                }
                objects.put(key, ((Enum<?>) o).name());
            }
        } else {
            for (Field f : clazz.getFields()) {
                if (!Modifier.isPublic(f.getModifiers())) continue;
                if (!Modifier.isStatic(f.getModifiers())) continue;
                if (!Modifier.isFinal(f.getModifiers())) continue;
                if (f.getType() != int.class) continue;
                try {
                    int key = (int) f.get(null);
                    if (!isPowerOfTwo(key)) continue; // invalid
                    objects.put(key, f.getName());
                } catch (ReflectiveOperationException ignore) {}
            }
        }
        CACHE.put(clazz, objects);
    }

    static @NotNull Set<String> fetchValue(@NotNull Class<?> clazz, int value) {
        cacheClass(clazz);
        Map<Integer, String> map = CACHE.get(clazz);
        if (map == null) {
            throw new IllegalStateException("Class " + clazz.getTypeName() + " is not cached");
        }
        Set<String> set = new HashSet<>();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if ((value & entry.getKey()) != 0) {
                set.add(entry.getValue());
            }
        }
        return set;
    }

    @Contract("_ -> new")
    static @NotNull Set<String> getNames(@NotNull Class<?> clazz) {
        cacheClass(clazz);
        Map<Integer, String> map = CACHE.get(clazz);
        if (map == null) {
            throw new IllegalStateException("Class " + clazz.getTypeName() + " is not cached");
        }
        return new HashSet<>(map.values());
    }
}
