package net.azisaba.azisabaachievements.velocity.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.azisabaachievements.common.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class VMessages {
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .extractUrls()
                    .hexColors()
//                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    @NotNull
    public static Component format(@NotNull String s, Object... args) {
        return LEGACY_COMPONENT_SERIALIZER.deserialize(String.format(Locale.ROOT, s, args));
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
