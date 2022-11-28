package net.azisaba.azisabaachievements.spigot.command;

import net.azisaba.azisabaachievements.spigot.gui.AchievementsMainScreen;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AzisabaAchievementsCommand implements TabExecutor {
    private final SpigotPlugin plugin;

    public AzisabaAchievementsCommand(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
        CommandManager.registerCommands(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
                return true;
            }
            Player player = (Player) sender;
            player.openInventory(new AchievementsMainScreen(
                    player,
                    plugin.getAchievementDataCache().getAchievementsAsList(),
                    plugin.getAchievementDataCache().getPlayerAchievements(player.getUniqueId())
            ).getInventory());
            return true;
        }
        try {
            Command cmd = CommandManager.getCommand(args[0]);
            if (cmd == null || !sender.hasPermission("azisabaachievements.command.azisabaachievements." + args[0])) {
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
            return Command.filter(
                    CommandManager.getCommands()
                            .stream()
                            .map(Command::getName)
                            .filter(name -> sender.hasPermission("azisabaachievements.command.azisabaachievements." + name)),
                    args[0]
            ).collect(Collectors.toList());
        }
        if (args.length >= 2) {
            Command cmd = CommandManager.getCommand(args[0]);
            if (cmd != null && sender.hasPermission("azisabaachievements.command.azisabaachievements." + args[0])) {
                return cmd.getSuggestions(sender, Stream.of(args).skip(1).toArray(String[]::new));
            }
        }
        return Collections.emptyList();
    }
}
