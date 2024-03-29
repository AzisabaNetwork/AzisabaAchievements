package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class AchievementData {
    public static final Codec<AchievementData> CODEC =
            Codec.<AchievementData>builder()
                    .group(
                            Codec.LONG.fieldOf("id").getter(AchievementData::getId),
                            Key.CODEC.fieldOf("key").getter(AchievementData::getKey),
                            Codec.LONG.fieldOf("count").getter(AchievementData::getCount),
                            Codec.INT.fieldOf("point").getter(AchievementData::getPoint),
                            AchievementHideFlags.CODEC.fieldOf("hidden").getter(AchievementData::getHidden),
                            MagicConstantBitField.codec(AchievementFlags.class).fieldOf("flags").getter(AchievementData::getFlags)
                    )
                    .build(AchievementData::new)
                    .named("AchievementData");
    public static final Codec<AchievementData> NETWORK_CODEC =
            Codec.<AchievementData>builder()
                    .group(
                            Key.CODEC.fieldOf("key").getter(AchievementData::getKey),
                            Codec.LONG.fieldOf("count").getter(AchievementData::getCount),
                            Codec.INT.fieldOf("point").getter(AchievementData::getPoint),
                            AchievementHideFlags.CODEC.fieldOf("hidden").getter(AchievementData::getHidden),
                            MagicConstantBitField.codec(AchievementFlags.class).fieldOf("flags").getter(AchievementData::getFlags)
                    )
                    .build((key, count, point, hidden, flags) -> new AchievementData(-1, key, count, point, hidden, flags))
                    .named("AchievementData[Network]");

    private final long id;
    private final Key key;
    private final long count;
    private final int point;
    private final AchievementHideFlags hidden;
    private final MagicConstantBitField<AchievementFlags> flags;

    @Contract(pure = true)
    public AchievementData(
            long id,
            @NotNull Key key,
            long count,
            int point,
            @NotNull AchievementHideFlags hidden,
            @NotNull MagicConstantBitField<AchievementFlags> flags
    ) {
        this.id = id;
        this.key = Objects.requireNonNull(key, "key");
        this.count = count;
        this.point = point;
        this.hidden = Objects.requireNonNull(hidden, "hidden");
        this.flags = Objects.requireNonNull(flags, "flags");
    }

    @Contract(pure = true)
    public long getId() {
        return id;
    }

    @Contract(pure = true)
    @NotNull
    public Key getKey() {
        return key;
    }

    @Contract(pure = true)
    public long getCount() {
        return count;
    }

    @Contract(pure = true)
    public int getPoint() {
        return point;
    }

    @Contract(pure = true)
    public @NotNull AchievementHideFlags getHidden() {
        return hidden;
    }

    @Contract(pure = true)
    public @NotNull MagicConstantBitField<AchievementFlags> getFlags() {
        return flags;
    }

    @Contract(pure = true)
    public @NotNull CompletableFuture<Set<AchievementData>> getChildren() {
        return AzisabaAchievementsProvider.get().getAchievementManager().getChildAchievements(key);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AchievementData)) return false;
        AchievementData that = (AchievementData) o;
        return getId() == that.getId() && getCount() == that.getCount() && getPoint() == that.getPoint() &&
                getKey().equals(that.getKey()) && getHidden() == that.getHidden() && getFlags().equals(that.getFlags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getKey(), getCount(), getPoint(), getHidden(), getFlags());
    }

    @Override
    public @NotNull String toString() {
        return "AchievementData{" +
                "id=" + id +
                ", key=" + key +
                ", count=" + count +
                ", point=" + point +
                ", hidden=" + hidden +
                ", flags=[" + String.join(",", flags) + "]" +
                '}';
    }
}
