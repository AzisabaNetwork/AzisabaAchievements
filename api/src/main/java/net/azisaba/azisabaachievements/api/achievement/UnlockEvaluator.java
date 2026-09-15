package net.azisaba.azisabaachievements.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class UnlockEvaluator {
    private UnlockEvaluator() {
    }

    @NotNull
    public static List<Key> metKeys(
            @NotNull Collection<AchievementData> achievements,
            @NotNull Map<Key, Long> progress
    ) {
        List<Key> keys = new ArrayList<>();
        for (AchievementData data : achievements) {
            if (data.getFlags().contains(AchievementFlags.CATEGORY)
                    || data.getFlags().contains(AchievementFlags.UNOBTAINABLE)) {
                continue;
            }
            if (progress.getOrDefault(data.getKey(), 0L) >= data.getCount()) {
                keys.add(data.getKey());
            }
        }
        return keys;
    }
}
