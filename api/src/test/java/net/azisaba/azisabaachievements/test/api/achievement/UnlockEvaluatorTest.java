package net.azisaba.azisabaachievements.test.api.achievement;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.achievement.UnlockEvaluator;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnlockEvaluatorTest {
    @Test
    public void metKeysIgnoresUnobtainableAndUnmet() {
        AchievementData kill = achievement("azisaba:general/kill", 10, 0);
        AchievementData category = achievement("azisaba:general", 1, AchievementFlags.CATEGORY);
        AchievementData hidden = achievement("azisaba:legacy/old", 1, AchievementFlags.UNOBTAINABLE);
        AchievementData play = achievement("azisaba:general/play", 5, 0);

        Map<Key, Long> progress = new HashMap<>();
        progress.put(kill.getKey(), 10L);
        progress.put(category.getKey(), 1L);
        progress.put(hidden.getKey(), 1L);
        progress.put(play.getKey(), 4L);

        List<Key> met = UnlockEvaluator.metKeys(Arrays.asList(kill, category, hidden, play), progress);
        assertEquals(Collections.singletonList(kill.getKey()), met);
    }

    private static AchievementData achievement(String key, long count, int flags) {
        return new AchievementData(
                1,
                Key.key(key),
                count,
                0,
                AchievementHideFlags.NEVER,
                MagicConstantBitField.of(AchievementFlags.class, flags)
        );
    }
}
