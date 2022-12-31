package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.Objects;

public final class AchievementTranslationData {
    public static final Codec<AchievementTranslationData> CODEC =
            Codec.<AchievementTranslationData>builder()
                    .group(
                            Codec.LONG.fieldOf("achievementId").getter(AchievementTranslationData::getAchievementId),
                            Key.CODEC.fieldOf("achievementKey").getter(AchievementTranslationData::getAchievementKey),
                            Codec.STRING.fieldOf("language").getter(AchievementTranslationData::getLanguage),
                            Codec.STRING.fieldOf("name").getter(AchievementTranslationData::getName),
                            Codec.STRING.fieldOf("description").getter(AchievementTranslationData::getDescription)
                    )
                    .build(AchievementTranslationData::new)
                    .named("AchievementTranslationData");
    public static final Codec<AchievementTranslationData> NETWORK_CODEC =
            Codec.<AchievementTranslationData>builder()
                    .group(
                            Key.CODEC.fieldOf("achievementKey").getter(AchievementTranslationData::getAchievementKey),
                            Codec.STRING.fieldOf("language").getter(AchievementTranslationData::getLanguage),
                            Codec.STRING.fieldOf("name").getter(AchievementTranslationData::getName),
                            Codec.STRING.fieldOf("description").getter(AchievementTranslationData::getDescription)
                    )
                    .build((key, language, name, description) -> new AchievementTranslationData(-1, key, language, name, description))
                    .named("AchievementTranslationData[Network]");

    private final long achievementId;
    private final Key achievementKey;
    private final String language;
    private final String name;
    private final String description;

    @Contract(pure = true)
    public AchievementTranslationData(
            long achievementId,
            @NotNull Key achievementKey,
            @NotNull String language,
            @NotNull String name,
            @NotNull String description
    ) {
        this.achievementId = achievementId;
        this.achievementKey = Objects.requireNonNull(achievementKey, "achievementKey");
        this.language = Objects.requireNonNull(language, "language");
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNull(description, "description");
    }

    @Contract(pure = true)
    public long getAchievementId() {
        return achievementId;
    }

    @Contract(pure = true)
    @NotNull
    public Key getAchievementKey() {
        return achievementKey;
    }

    @Contract(pure = true)
    @NotNull
    public String getLanguage() {
        return language;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return name;
    }

    @Contract(pure = true)
    @NotNull
    public String getDescription() {
        return description;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AchievementTranslationData)) return false;
        AchievementTranslationData that = (AchievementTranslationData) o;
        return getAchievementId() == that.getAchievementId() && getAchievementKey().equals(that.getAchievementKey()) && getLanguage().equals(that.getLanguage()) && getName().equals(that.getName()) && getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAchievementId(), getAchievementKey(), getLanguage(), getName(), getDescription());
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "AchievementTranslationData{" +
                "achievementId=" + achievementId +
                ", achievementKey=" + achievementKey +
                ", language='" + language + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
