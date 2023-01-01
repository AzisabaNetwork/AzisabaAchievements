package net.azisaba.azisabaachievements.api.achievement;

public final class AchievementFlags {
    public static final int CATEGORY = 1;
    public static final int UNOBTAINABLE = 1 << 1; // "count" value does NOT count towards total points
    public static final int SEASONAL = 1 << 2;
}
