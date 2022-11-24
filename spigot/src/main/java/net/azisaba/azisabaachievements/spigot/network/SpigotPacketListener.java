package net.azisaba.azisabaachievements.spigot.network;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerFetchAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressAchievementCallback;
import net.azisaba.azisabaachievements.spigot.achievement.SpigotAchievementManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SpigotPacketListener implements ServerPacketListener {
    @Override
    public void handle(@NotNull PacketCommonAchievementUnlocked packet) {
        AzisabaAchievementsProvider.get()
                .getScheduler()
                .builder(() -> {
                    Player player = Bukkit.getPlayer(packet.getPlayerUniqueId());
                    if (player == null) {
                        return;
                    }
                    // TODO: hardcoded message
                    player.sendMessage(ChatColor.GREEN + "実績解除！ 「" + ChatColor.YELLOW + packet.getAchievement().getKey() + ChatColor.GREEN + "」");
                    Firework spawnedFirework = player.getWorld().spawn(player.getLocation(), Firework.class, firework -> {
                        FireworkMeta meta = firework.getFireworkMeta();
                        meta.addEffect(FireworkEffect.builder()
                                .withColor(Color.GREEN)
                                .withFade(Color.RED)
                                .with(FireworkEffect.Type.BALL)
                                .build());
                        meta.setPower(0);
                        firework.setFireworkMeta(meta);
                    });
                    AzisabaAchievementsProvider.get()
                            .getScheduler()
                            .builder(spawnedFirework::detonate)
                            .delay(500, TimeUnit.MILLISECONDS)
                            .sync()
                            .schedule();
                }).sync().schedule();
    }

    @Override
    public void handle(@NotNull PacketServerCreateAchievementCallback packet) {
        CompletableFuture<AchievementData> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .achievementDataCallback
                .remove(packet.getSeq());
        if (cb == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }

    @Override
    public void handle(@NotNull PacketServerFetchAchievementCallback packet) {
        CompletableFuture<Optional<AchievementData>> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .optionalAchievementDataCallback
                .remove(packet.getSeq());
        if (cb == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }

    @Override
    public void handle(@NotNull PacketServerProgressAchievementCallback packet) {
        CompletableFuture<Boolean> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .progressAchievementCallback
                .remove(packet.getSeq());
        if (cb == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }
}
