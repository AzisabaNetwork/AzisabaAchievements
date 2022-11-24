package net.azisaba.azisabaachievements.spigot.data;

import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class TranslatedAchievement {
    private final AchievementData data;
    private final Map<String, AchievementTranslationData> translation;

    public TranslatedAchievement(@NotNull AchievementData data, @NotNull Map<String, AchievementTranslationData> translation) {
        this.data = data;
        this.translation = translation;
    }

    @NotNull
    public AchievementData getData() {
        return data;
    }

    @NotNull
    public Map<String, AchievementTranslationData> getTranslation() {
        return translation;
    }

    @Nullable
    public AchievementTranslationData getTranslationForLocale(@NotNull String locale) {
        // try to get the exact locale
        if (translation.containsKey(locale)) {
            return translation.get(locale);
        }
        // try locale without country code
        AchievementTranslationData data = translation.get(locale.replaceAll("_.*", ""));
        if (data != null) {
            return data;
        }
        // try en
        data = translation.get("en");
        if (data != null) {
            return data;
        }
        // try en_us
        data = translation.get("en_us");
        if (data != null) {
            return data;
        }
        // try to find any translation or return null
        return translation.values().stream().findAny().orElse(null);
    }
}
