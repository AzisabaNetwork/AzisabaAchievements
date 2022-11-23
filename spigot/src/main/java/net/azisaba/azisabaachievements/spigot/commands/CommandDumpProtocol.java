package net.azisaba.azisabaachievements.spigot.commands;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.spigot.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandDumpProtocol implements Command {
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        PacketRegistryPair pair = AzisabaAchievementsProvider.get().getPacketRegistryPair();
        PacketRegistry clientRegistry = pair.getClientRegistry();
        PacketRegistry serverRegistry = pair.getServerRegistry();
        dumpProtocol(sender, clientRegistry, "Client ");
        dumpProtocol(sender, serverRegistry, "Server ");
    }

    private void dumpProtocol(@NotNull CommandSender sender, @NotNull PacketRegistry registry, @NotNull String prefix) {
        int id = -1;
        Class<? extends Packet<?>> packet;
        do {
            packet = registry.getById(++id);
            if (packet != null) {
                String hex = Integer.toString(id, 16);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                sender.sendMessage(prefix + ChatColor.GREEN + id + " (0x" + hex + ") " + packet.getSimpleName());
            }
        } while (packet != null);
    }

    @Override
    public @NotNull String getName() {
        return "dumpProtocol";
    }

    @Override
    public @NotNull String getDescription() {
        return "Shows all registered packets.";
    }

    @Override
    public @NotNull String getUsage() {
        return "";
    }
}
