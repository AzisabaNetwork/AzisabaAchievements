package net.azisaba.azisabaachievements.velocity.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.azisabaachievements.common.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VMessages {
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .extractUrls()
                    .hexColors()
//                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();
    private static final Pattern COLOR_PATTERN = Pattern.compile("%%([A-Z]+?)%%");
    private static final Map<String, Character> COLOR_MAP;

    static {
        Map<String, Character> colorMap = new HashMap<>();
        colorMap.put("BLACK", '0');
        colorMap.put("DARK_BLUE", '1');
        colorMap.put("DARK_GREEN", '2');
        colorMap.put("DARK_AQUA", '3');
        colorMap.put("DARK_RED", '4');
        colorMap.put("DARK_PURPLE", '5');
        colorMap.put("GOLD", '6');
        colorMap.put("GRAY", '7');
        colorMap.put("DARK_GRAY", '8');
        colorMap.put("BLUE", '9');
        colorMap.put("GREEN", 'a');
        colorMap.put("AQUA", 'b');
        colorMap.put("RED", 'c');
        colorMap.put("LIGHT_PURPLE", 'd');
        colorMap.put("YELLOW", 'e');
        colorMap.put("WHITE", 'f');
        colorMap.put("BOLD", 'l');
        colorMap.put("ITALIC", 'o');
        colorMap.put("UNDERLINE", 'n');
        colorMap.put("STRIKETHROUGH", 'm');
        colorMap.put("OBFUSCATED", 'k');
        colorMap.put("RESET", 'r');
        COLOR_MAP = Collections.unmodifiableMap(colorMap);
    }

    @NotNull
    public static Component format(@NotNull String s, Object... args) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = COLOR_PATTERN.matcher(s);
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(sb, "&" + COLOR_MAP.getOrDefault(color, 'f').toString());
        }
        matcher.appendTail(sb);
        String formatted = String.format(Locale.ROOT, sb.toString(), args);
        return LEGACY_COMPONENT_SERIALIZER.deserialize(formatted);
    }

    public static void sendFormatted(@NotNull CommandSource source, @NotNull String key, Object @NotNull ... args) {
        Locale locale = Locale.ENGLISH;
        if (source instanceof Player) {
            locale = ((Player) source).getEffectiveLocale();
        }
        String rawMessage = Messages.getInstance(locale).get(key);
        Component formatted = format(rawMessage, args);
        source.sendMessage(formatted);
    }
}
