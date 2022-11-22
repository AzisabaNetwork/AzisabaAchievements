package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.spigot.command.Command;
import net.azisaba.azisabaachievements.spigot.command.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandHelp implements Command {
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        for (Command command : CommandManager.getCommands()) {
            sender.sendMessage(ChatColor.AQUA + command.getFullUsage() + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE + command.getDescription());
        }
    }

    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Override
    public @NotNull String getDescription() {
        return "Displays the help message.";
    }

    @Override
    public @NotNull String getUsage() {
        return "";
    }
}
