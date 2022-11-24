package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.spigot.command.Command;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CommandProgress implements Command {
    private final AchievementDataCache achievementDataCache;

    public CommandProgress(@NotNull AchievementDataCache achievementDataCache) {
        this.achievementDataCache = achievementDataCache;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getFullUsage());
            return;
        }
        UUID uuid;
        Player player = Bukkit.getPlayerExact(args[0]);
        if (player != null) {
            uuid = player.getUniqueId();
        } else {
            try {
                uuid = UUID.fromString(args[0]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid player name or UUID: " + args[0]);
                return;
            }
        }
        Key key = Key.key(args[1]);
        long count = Long.parseLong(args[2]);
        AzisabaAchievementsProvider.get().getAchievementManager().progressAchievement(uuid, key, count)
                .whenComplete((data, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(ChatColor.RED + "Failed to progress achievement: " + throwable.getMessage());
                        return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Successfully progressed achievement: " + key);
                });
    }

    @Override
    public @NotNull List<String> getSuggestions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Command.suggestPlayer(args[0]);
        }
        if (args.length == 2) {
            return Command.suggestAchievementKey(achievementDataCache, args[1]);
        }
        return Command.super.getSuggestions(sender, args);
    }

    @Override
    public @NotNull String getName() {
        return "progress";
    }

    @Override
    public @NotNull String getDescription() {
        return "Updates the progress of an achievement.";
    }

    @Override
    public @NotNull String getUsage() {
        return "<player> <achievement key> <count>";
    }
}
