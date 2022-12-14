package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.spigot.command.Command;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandSync implements Command {
    private final AchievementDataCache achievementDataCache;

    public CommandSync(@NotNull AchievementDataCache achievementDataCache) {
        this.achievementDataCache = achievementDataCache;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (achievementDataCache.isPending()) {
            sender.sendMessage(ChatColor.RED + "The achievement data is already being synchronized.");
            return;
        }
        long start = System.nanoTime();
        sender.sendMessage(ChatColor.GOLD + "Syncing achievements, please wait...");
        achievementDataCache.requestRefresh().whenComplete((v, throwable) -> {
            double elapsedMillis = Math.round((System.nanoTime() - start) / 1000000.0 * 100.0) / 100.0;
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + "Sync failed in " + elapsedMillis + "ms. (" + throwable.getMessage() + ")");
                throwable.printStackTrace();
            } else {
                sender.sendMessage(ChatColor.GREEN + "Sync completed in " + elapsedMillis + "ms.");
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return "sync";
    }

    @Override
    public @NotNull String getDescription() {
        return "Force the achievement data cache to sync with the database.";
    }

    @Override
    public @NotNull String getUsage() {
        return "";
    }
}
