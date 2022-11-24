package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.spigot.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandProgress implements Command {
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getFullUsage());
            return;
        }
        UUID player = UUID.fromString(args[0]);
        Key key = Key.key(args[1]);
        long count = Long.parseLong(args[2]);
        AzisabaAchievementsProvider.get().getAchievementManager().progressAchievement(player, key, count)
                .whenComplete((data, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(ChatColor.RED + "Failed to progress achievement: " + throwable.getMessage());
                        return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Successfully progressed achievement: " + key);
                });
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
