package net.azisaba.azisabaachievements.spigot.reward;

import net.azisaba.azisabaachievements.api.network.packet.PacketProxyClaimRewards;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCompleteRewardDelivery;
import net.azisaba.azisabaachievements.api.reward.RewardDelivery;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class RewardDeliveryExecutor {
    private final SpigotPlugin plugin;

    public RewardDeliveryExecutor(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    public void request(@NotNull UUID playerId) {
        if (Bukkit.getPlayer(playerId) == null) {
            return;
        }
        plugin.getJedisBox().getPubSubHandler().sendPacket(
                new PacketProxyClaimRewards(playerId, plugin.getPluginConfig().rewardServerId));
    }

    public void execute(@NotNull UUID playerId, @NotNull RewardDelivery delivery) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                report(delivery.getId(), false, "Player went offline after reward was claimed");
                return;
            }

            String command = delivery.getCommand()
                    .replace("{player}", player.getName())
                    .replace("{uuid}", playerId.toString())
                    .replace("{achievement}", delivery.getAchievementKey().toString())
                    .replace("{server}", plugin.getPluginConfig().rewardServerId);
            try {
                boolean successful = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                report(delivery.getId(), successful, successful ? "" : "Command returned false");
            } catch (Throwable throwable) {
                String message = throwable.getMessage() == null
                        ? throwable.getClass().getSimpleName()
                        : throwable.getMessage();
                plugin.getLogger().severe("Failed to execute achievement reward #" + delivery.getId() + ": " + message);
                report(delivery.getId(), false, message);
            }
        });
    }

    private void report(long deliveryId, boolean successful, @NotNull String error) {
        plugin.getJedisBox().getPubSubHandler().sendPacket(
                new PacketProxyCompleteRewardDelivery(deliveryId, successful, error));
    }
}
