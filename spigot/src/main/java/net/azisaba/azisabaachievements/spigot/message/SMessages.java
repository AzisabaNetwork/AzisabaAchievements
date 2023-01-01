package net.azisaba.azisabaachievements.spigot.message;

import net.azisaba.azisabaachievements.common.message.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMessages {
    private static final Pattern COLOR_PATTERN = Pattern.compile("%%([A-Z]+?)%%");
    private static final Map<String, Character> COLOR_MAP;

    static {
        Map<String, Character> colorMap = new HashMap<>();
        for (ChatColor value : ChatColor.values()) {
            colorMap.put(value.name(), value.getChar());
        }
        COLOR_MAP = Collections.unmodifiableMap(colorMap);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static String format(@NotNull String s, Object... args) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = COLOR_PATTERN.matcher(s);
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(sb, "§" + COLOR_MAP.getOrDefault(color, 'f').toString());
        }
        matcher.appendTail(sb);
        String formatted = String.format(Locale.ROOT, sb.toString(), args);
        return ChatColor.translateAlternateColorCodes('&', formatted);
    }

    public static @NotNull String getFormattedMessage(@Nullable CommandSender sender, @NotNull String key, Object @NotNull ... args) {
        Locale locale = Locale.ENGLISH;
        if (sender instanceof Player) {
            locale = Locale.forLanguageTag(((Player) sender).getLocale().replaceAll("_.*", ""));
        }
        String rawMessage = Messages.getInstance(locale).get(key);
        return format(rawMessage, args);
    }

    public static void sendFormatted(@NotNull CommandSender sender, @NotNull String key, Object @NotNull ... args) {
        sender.sendMessage(getFormattedMessage(sender, key, args));
    }
}
