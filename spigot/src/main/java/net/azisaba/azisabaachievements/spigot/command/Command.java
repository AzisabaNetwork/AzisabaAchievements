package net.azisaba.azisabaachievements.spigot.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface Command {
    void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) throws Exception;

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
