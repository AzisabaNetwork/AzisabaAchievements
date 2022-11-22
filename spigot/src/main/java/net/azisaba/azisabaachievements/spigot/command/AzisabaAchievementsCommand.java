package net.azisaba.azisabaachievements.spigot.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AzisabaAchievementsCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try {
            Command cmd = CommandManager.getCommand(args[0]);
            if (cmd == null) {
                sender.sendMessage(ChatColor.RED + "Unknown command: " + args[0]);
                return true;
            }
            cmd.execute(sender, Stream.of(args).skip(1).toArray(String[]::new));
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(CommandManager.getCommands().stream().map(Command::getName), args[0]).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static Stream<String> filter(@NotNull Stream<String> stream, @NotNull String str) {
        return stream.filter(s -> s.toLowerCase().startsWith(str.toLowerCase()));
    }
}
