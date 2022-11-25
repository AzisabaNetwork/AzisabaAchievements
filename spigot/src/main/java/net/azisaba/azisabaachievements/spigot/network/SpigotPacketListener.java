package net.azisaba.azisabaachievements.spigot.network;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerDataResult;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerFetchAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressAchievementCallback;
import net.azisaba.azisabaachievements.spigot.achievement.SpigotAchievementManager;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SpigotPacketListener implements ServerPacketListener {
    private final SpigotPlugin plugin;

    public SpigotPacketListener(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull PacketCommonAchievementUnlocked packet) {
        AzisabaAchievementsProvider.get()
                .getScheduler()
                .builder(() -> {
                    Player player = Bukkit.getPlayer(packet.getPlayerUniqueId());
                    if (player == null) {
                        return;
                    }
                    AchievementDataCache cache = plugin.getAchievementDataCache();
                    TranslatedAchievement achievement = cache.getAchievement(packet.getAchievement().getKey());
                    AchievementTranslationData translation = achievement.getTranslationForLocale(player.getLocale());
                    String translatedName = translation == null ? packet.getAchievement().getKey().toString() : translation.getName();
                    // TODO: hardcoded message
                    player.sendMessage(ChatColor.GREEN + "実績解除！ 「" + ChatColor.YELLOW + translatedName + ChatColor.GREEN + "」");
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
    public void handle(@NotNull PacketServerDataResult packet) {
        AchievementDataCache cache = plugin.getAchievementDataCache();
        if (!cache.isPending()) {
            return;
        }
        cache.load(packet);
    }

    @Override
    public void handle(@NotNull PacketServerCreateAchievementCallback packet) {
        // add to cache
        if (packet.getResult().isRight()) {
            AchievementDataCache cache = plugin.getAchievementDataCache();
            cache.add(new TranslatedAchievement(packet.getResult().getRight(), new HashMap<>()));
        }

        // process callback
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

    @Override
    public void handle(@NotNull PacketServerAddAchievementTranslation packet) {
        AchievementDataCache cache = plugin.getAchievementDataCache();
        TranslatedAchievement achievement = cache.getAchievement(packet.getData().getAchievementKey());
        achievement.addTranslation(packet.getData());
    }
}
