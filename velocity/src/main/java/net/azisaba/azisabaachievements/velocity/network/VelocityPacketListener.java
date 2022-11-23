package net.azisaba.azisabaachievements.velocity.network;

import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderChanged;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderLeave;
import net.azisaba.azisabaachievements.velocity.plugin.VelocityPlugin;
import org.jetbrains.annotations.NotNull;

public class VelocityPacketListener implements ProxyPacketListener {
    private final VelocityPlugin plugin;

    public VelocityPacketListener(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull PacketCommonProxyLeaderLeave packet) {
        if (!packet.getServerId().equals(plugin.getServerIdProvider().getId())) {
            plugin.getRedisConnectionLeader().trySwitch();
        }
    }

    @Override
    public void handle(@NotNull PacketCommonProxyLeaderChanged packet) {
        if (!packet.getServerId().equals(plugin.getServerIdProvider().getId())) {
            plugin.getRedisConnectionLeader().trySwitch();
        }
    }
}
