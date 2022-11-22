package net.azisaba.azisabaachievements.spigot.command;

import net.azisaba.azisabaachievements.spigot.commands.CommandDumpProtocol;
import net.azisaba.azisabaachievements.spigot.commands.CommandHelp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandManager {
    private CommandManager() { throw new AssertionError(); }

    private static final List<Command> COMMANDS = new ArrayList<>();

    public static void registerCommand(@NotNull Command command) {
        COMMANDS.add(Objects.requireNonNull(command, "command"));
    }

    @Contract(pure = true)
    public static @NotNull List<@NotNull Command> getCommands() {
        return COMMANDS;
    }

    public static @Nullable Command getCommand(@NotNull String name) {
        for (Command command : COMMANDS) {
            if (command.getName().equalsIgnoreCase(name)) return command;
        }
        return null;
    }

    static {
        registerCommand(new CommandHelp());
        registerCommand(new CommandDumpProtocol());
    }
}
