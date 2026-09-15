package net.azisaba.azisabaachievements.spigot.listener;

import net.azisaba.azisabaachievements.api.achievement.ProgressProvider;
import net.azisaba.azisabaachievements.spigot.event.ProgressUpdatedEvent;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayerJoinListener implements Listener {
    private static final Executor SKIN_FETCHER = Executors.newSingleThreadExecutor(r -> new Thread(r, "AzisabaAchievements Skin Fetcher"));
    private final Inventory inventory = Bukkit.createInventory(null, 9);
    private final SpigotPlugin plugin;

    public PlayerJoinListener(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        plugin.getProgressSynchronizer().sync(e.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        preloadPlayerSkin(e.getPlayer());
        plugin.getRewardDeliveryExecutor().request(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onProgressUpdated(@NotNull ProgressUpdatedEvent e) {
        plugin.getProgressSynchronizer().syncAsync(e.getPlayerId());
    }

    @EventHandler
    public void onServiceRegister(@NotNull ServiceRegisterEvent e) {
        if (e.getProvider().getService() != ProgressProvider.class) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getProgressSynchronizer().syncAsync(player.getUniqueId());
        }
    }

    private void preloadPlayerSkin(@NotNull Player player) {
        SKIN_FETCHER.execute(() -> {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(player);
            skull.setItemMeta(meta);
            inventory.setItem(0, skull);
        });
    }
}
