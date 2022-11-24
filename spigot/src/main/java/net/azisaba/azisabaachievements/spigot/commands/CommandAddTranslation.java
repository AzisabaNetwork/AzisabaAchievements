package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.spigot.command.Command;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class CommandAddTranslation implements Command {
    private final AchievementDataCache achievementDataCache;

    public CommandAddTranslation(@NotNull AchievementDataCache achievementDataCache) {
        this.achievementDataCache = achievementDataCache;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
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
        String name = args[2]; // TODO: quotable
        String description = args[3]; // TODO: quotable
        // TODO: implement
    }

    @Override
    public @NotNull List<String> getSuggestions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Command.suggestAchievementKey(achievementDataCache, args[0]);
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
