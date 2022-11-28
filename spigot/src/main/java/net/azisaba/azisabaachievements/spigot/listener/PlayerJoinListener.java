package net.azisaba.azisabaachievements.spigot.listener;

import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestPlayerData;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final SpigotPlugin plugin;

    public PlayerJoinListener(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        preloadPlayerData(e.getUniqueId());
    }

    private void preloadPlayerData(@NotNull UUID uuid) {
        plugin.getJedisBox().getPubSubHandler().sendPacket(new PacketProxyRequestPlayerData(uuid));
    }
}
