package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyAddAchievementTranslation;
import net.azisaba.azisabaachievements.spigot.command.Command;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.InvalidArgumentException;
import xyz.acrylicstyle.util.StringReader;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CommandAddTranslation implements Command {
    private static final char[] ALLOWED_ESCAPES = new char[]{'\n', '\r', '\\', '"', '\t'};
    private final AchievementDataCache achievementDataCache;

    public CommandAddTranslation(@NotNull AchievementDataCache achievementDataCache) {
        this.achievementDataCache = achievementDataCache;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @Subst("minecraft") @NotNull String @NotNull [] args) throws InvalidArgumentException {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getFullUsage());
            return;
        }
        Key key = Key.key(args[0]);
        String lang = Locale.forLanguageTag(args[1]).getLanguage();
        if (lang.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Invalid language tag: " + args[1]);
            return;
        }
        String joined = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        StringReader reader = StringReader.create(joined);
        String name = reader.readQuotableString(ALLOWED_ESCAPES);
        reader.skipWhitespace();
        String description = reader.readQuotableString(ALLOWED_ESCAPES);
        AzisabaAchievementsProvider.get().getPacketSender()
                .sendPacket(new PacketProxyAddAchievementTranslation(key, lang, name, description));
        sender.sendMessage(ChatColor.GREEN + "Sent request to add translation for " + key + " (" + lang + ")");
    }

    @Override
    public @NotNull List<String> getSuggestions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Command.suggestNonCategoryAchievementKey(achievementDataCache, args[0]);
        }
        return Command.super.getSuggestions(sender, args);
    }

    @Override
    public @NotNull String getName() {
        return "addTranslation";
    }

    @Override
    public @NotNull String getDescription() {
        return "Updates the translation of an achievement.";
    }

    @Override
    public @NotNull String getUsage() {
        return "<achievement key> <lang> <name> <description>";
    }
}
