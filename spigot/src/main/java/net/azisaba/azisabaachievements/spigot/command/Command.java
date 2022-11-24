package net.azisaba.azisabaachievements.spigot.command;

import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Command {
    @Contract(pure = true)
    static @NotNull Stream<String> filter(@NotNull Stream<String> stream, @NotNull String str) {
        return stream.filter(s -> s.toLowerCase().startsWith(str.toLowerCase()));
    }

    @Contract(pure = true)
    static @NotNull List<String> suggestPlayer(@NotNull String str) {
        return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName), str).collect(Collectors.toList());
    }

    @Contract(pure = true)
    static @NotNull List<String> suggestAchievementKey(@NotNull AchievementDataCache cache, @NotNull String str) {
        return filter(cache.getAchievements().keySet().stream().map(String::valueOf), str).collect(Collectors.toList());
    }

    void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) throws Exception;

    default @NotNull List<String> getSuggestions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return Collections.emptyList();
    }

    @NotNull
    String getName();

    @NotNull
    String getDescription();

    @NotNull
    String getUsage();

    @NotNull
    default String getFullUsage() {
        return ("/azisabaachievements " + getName() + " " + getUsage()).trim();
    }
}
