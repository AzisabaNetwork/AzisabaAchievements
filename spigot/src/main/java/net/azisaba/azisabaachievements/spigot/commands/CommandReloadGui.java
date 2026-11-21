package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.spigot.command.Command;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandReloadGui implements Command {
    private final SpigotPlugin plugin;

    public CommandReloadGui(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        try {
            plugin.reloadGuiConfig();
            sender.sendMessage(ChatColor.GREEN + "Reloaded gui.yml.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload gui.yml (previous layout is kept): " + e.getMessage());
        }
    }

    @Override
    public @NotNull String getName() {
        return "reloadgui";
    }

    @Override
    public @NotNull String getDescription() {
        return "Reload the menu layout from gui.yml.";
    }

    @Override
    public @NotNull String getUsage() {
        return "";
    }
}
