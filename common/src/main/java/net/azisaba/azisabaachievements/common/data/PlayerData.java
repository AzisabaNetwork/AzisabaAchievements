package net.azisaba.azisabaachievements.common.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.Objects;
import java.util.UUID;

public final class PlayerData {
    public static final Codec<PlayerData> CODEC =
            Codec.<PlayerData>builder()
                    .group(
                            Codec.UUID.fieldOf("id").getter(PlayerData::getId),
                            Codec.STRING.fieldOf("name").getter(PlayerData::getName)
                    )
                    .build(PlayerData::new)
                    .named("PlayerData");

    private final UUID id;
    private final String name;

    public PlayerData(@NotNull UUID id, @NotNull String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Contract(pure = true)
    @NotNull
    public UUID getId() {
        return id;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return name;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerData)) return false;
        PlayerData that = (PlayerData) o;
        return id.equals(that.id) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "PlayerData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
