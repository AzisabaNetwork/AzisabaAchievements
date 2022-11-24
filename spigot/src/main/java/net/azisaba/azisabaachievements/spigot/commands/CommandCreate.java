package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.spigot.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandCreate implements Command {
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getFullUsage());
            return;
        }
        Key key = Key.key(args[0]);
        long count = Long.parseLong(args[1]);
        int point = Integer.parseInt(args[2]);
        AzisabaAchievementsProvider.get().getAchievementManager()
                .createAchievement(key, count, point)
                .whenComplete((data, throwable) -> {
                    if (throwable != null) {
                        sender.sendMessage(ChatColor.RED + "Failed to create achievement: " + throwable.getMessage());
                        return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Successfully created achievement: " + data.getKey());
                });
    }

    @Override
    public @NotNull String getName() {
        return "create";
    }

    @Override
    public @NotNull String getDescription() {
        return "Creates a new achievement.";
    }

    @Override
    public @NotNull String getUsage() {
        return "<key> <count> <point>";
    }
}
