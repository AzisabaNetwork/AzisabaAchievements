package net.azisaba.azisabaachievements.api.achievement;

import xyz.acrylicstyle.util.serialization.codec.Codec;

public enum AchievementHideFlags {
    NEVER, // 0; never hidden
    UNLESS_PROGRESS, // 1; hidden unless a player has progress on the achievement at least once
    UNLESS_UNLOCKED, // 2; hidden unless a player has unlocked the achievement
    ALWAYS, // 3; always hidden even if a player has unlocked the achievement (they won't be notified at all)
    ;

    public static final Codec<AchievementHideFlags> CODEC =
            Codec.INT.xmap(i -> AchievementHideFlags.values()[i], AchievementHideFlags::ordinal)
                    .named("AchievementHideFlags");
}
