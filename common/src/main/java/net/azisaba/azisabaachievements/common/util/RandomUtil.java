package net.azisaba.azisabaachievements.common.util;

import org.jetbrains.annotations.NotNull;

public class RandomUtil {
    public static @NotNull String randomAlphanumeric(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(randomAlphanumeric());
        }
        return builder.toString();
    }

    public static char randomAlphanumeric() {
        int random = (int) (Math.random() * 62);
        if (random < 10) {
            return (char) (random + '0');
        } else if (random < 36) {
            return (char) (random - 10 + 'a');
        } else {
            return (char) (random - 36 + 'A');
        }
    }
}
