package net.azisaba.azisabaachievements.spigot.message;

import net.azisaba.azisabaachievements.common.message.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SMessages {
    @Contract("_, _ -> new")
    @NotNull
    public static String format(@NotNull String s, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', String.format(Locale.ROOT, s, args));
    }

    public static void sendFormatted(@NotNull CommandSender sender, @NotNull String key, Object @NotNull ... args) {
        Locale locale = Locale.ENGLISH;
        if (sender instanceof Player) {
            locale = Locale.forLanguageTag(((Player) sender).getLocale());
        }
        String rawMessage = Messages.getInstance(locale).get(key);
        String formatted = format(rawMessage, args);
        sender.sendMessage(formatted);
    }
}
